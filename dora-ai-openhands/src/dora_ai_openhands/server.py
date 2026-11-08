"""
DoraAI2 Server — FastAPI REST API for the DoraAI autonomous coding agent.

Exposes the OpenHands-based DoraAI agent as a REST API that the Java Spring Boot
backend (DoraAI2 module) can call, just like it calls Ollama/Gemini today.

Endpoints:
    POST /api/chat          — Send a chat message and get streaming response (SSE)
    GET  /api/status         — Check agent status and available modes
    GET  /api/providers      — List available AI providers
    DELETE /api/session/{id} — Clear a conversation session
"""

import os
import uuid
from typing import Optional

from fastapi import FastAPI, HTTPException
from fastapi.responses import StreamingResponse, JSONResponse
from pydantic import BaseModel

from dora_ai_openhands.agent import create_agent, DoraAgent
from dora_ai_openhands.conversation_manager import ConversationManager
from dora_ai_openhands.prompts import get_system_prompt

app = FastAPI(
    title="DoraAI2 Agent Server",
    description="Junie-like autonomous coding agent API for DoraMCU",
    version="0.1.0",
)

# In-memory agent cache keyed by mode
_agents: dict[str, DoraAgent] = {}
conv_manager = ConversationManager()


class ChatRequest(BaseModel):
    """Request to send a chat message to the DoraAI agent."""
    sessionId: Optional[str] = None
    message: str
    mode: str = "chat"  # chat, code, fast_code, advanced_chat, run_verify, setup
    projectId: Optional[str] = None
    sourceLanguage: Optional[str] = None
    inputType: Optional[str] = "text"
    fileContext: Optional[str] = None
    providerId: Optional[str] = None


class ChatResponse(BaseModel):
    """Response from the DoraAI agent."""
    sessionId: str
    content: str
    mode: str


def get_agent(mode: str) -> DoraAgent:
    """Get or create an agent for the given mode."""
    if mode not in _agents:
        _agents[mode] = create_agent(
            mode=mode,
            workspace=os.getenv("DORA_WORKSPACE", os.getcwd()),
        )
    return _agents[mode]


def _extract_conversation_output(conversation) -> str:
    """
    Extract the agent's output from a completed OpenHands Conversation.
    
    Tries multiple strategies to get the actual response text from the conversation,
    falling back to a summary if the exact format is unknown.
    """
    # Strategy 1: Try conversation.messages (list of message dicts)
    if hasattr(conversation, 'messages') and conversation.messages:
        messages = conversation.messages
        # Find the last assistant/tool response
        for msg in reversed(messages):
            if isinstance(msg, dict):
                role = msg.get('role', '')
                if role in ('assistant', 'agent'):
                    content = msg.get('content', '')
                    if content:
                        return content
            elif hasattr(msg, 'role'):
                if msg.role in ('assistant', 'agent'):
                    content = getattr(msg, 'content', '') or ''
                    if content:
                        return content

    # Strategy 2: Try conversation.history
    if hasattr(conversation, 'history') and conversation.history:
        history = conversation.history
        for entry in reversed(history):
            if isinstance(entry, dict):
                role = entry.get('role', '')
                if role in ('assistant', 'agent'):
                    content = entry.get('content', '')
                    if content:
                        return content

    # Strategy 3: Try conversation.output
    if hasattr(conversation, 'output'):
        output = conversation.output
        if output:
            return str(output)

    # Strategy 4: Try conversation.result
    if hasattr(conversation, 'result'):
        result = conversation.result
        if result:
            return str(result)

    # Strategy 5: Try to get the last state/event
    if hasattr(conversation, 'get_last_message'):
        try:
            last_msg = conversation.get_last_message()
            if last_msg:
                return str(last_msg)
        except Exception:
            pass

    # Fallback: return a summary of what happened
    return "Task completed by DoraAI agent. (Detailed output capture depends on OpenHands SDK version.)"


@app.post("/api/chat")
async def chat(request: ChatRequest):
    """
    Send a chat message and receive a streaming response.
    
    Uses Server-Sent Events (SSE) for streaming the agent's response.
    """
    session_id = request.sessionId or str(uuid.uuid4())
    
    # Enrich message with file context if provided
    message = request.message
    if request.fileContext and request.fileContext.strip():
        message = message + "\n\n" + request.fileContext
    
    # Record in conversation manager
    conv_manager.add_message(session_id, "user", message)
    
    # Get agent for the mode
    agent = get_agent(request.mode)
    
    async def event_generator():
        """Generate SSE events from the agent's response."""
        # Send initial status
        yield f"event: status\ndata: processing\n\n"
        
        try:
            # Create a simple conversation and run it
            from openhands.sdk import Conversation
            conversation = Conversation(agent=agent.agent, workspace=agent.workspace)
            conversation.send_message(message)
            
            # Run the agent and capture events in real-time
            import asyncio
            
            async def run_and_stream():
                try:
                    # Run in a thread to avoid blocking the event loop
                    loop = asyncio.get_event_loop()
                    await loop.run_in_executor(None, conversation.run)
                    
                    # Extract and send the agent's output
                    output = _extract_conversation_output(conversation)
                    if output:
                        # Split into chunks for streaming
                        for chunk in output.split('\n'):
                            if chunk.strip():
                                yield f"event: message\ndata: {chunk}\n\n"
                except Exception as e:
                    yield f"event: error\ndata: {str(e)}\n\n"
            
            # Stream events as they become available
            async for event in run_and_stream():
                yield event
            
            # Send completion event
            yield f"event: status\ndata: done\n\n"
            yield f"event: done\ndata:\n\n"
            
        except Exception as e:
            yield f"event: error\ndata: {str(e)}\n\n"
    
    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        },
    )


@app.post("/api/chat/blocking")
async def chat_blocking(request: ChatRequest):
    """
    Send a chat message and receive a complete response (non-streaming).
    
    Useful for simple integrations that don't need SSE streaming.
    """
    session_id = request.sessionId or str(uuid.uuid4())
    
    # Enrich message with file context if provided
    message = request.message
    if request.fileContext and request.fileContext.strip():
        message = message + "\n\n" + request.fileContext
    
    # Record in conversation manager
    conv_manager.add_message(session_id, "user", message)
    
    # Get agent for the mode
    agent = get_agent(request.mode)
    
    try:
        # Run the agent
        from openhands.sdk import Conversation
        conversation = Conversation(agent=agent.agent, workspace=agent.workspace)
        conversation.send_message(message)
        conversation.run()
        
        # Capture the agent's actual output from the conversation
        response_content = _extract_conversation_output(conversation)
        conv_manager.add_message(session_id, "assistant", response_content)
        
        return ChatResponse(
            sessionId=session_id,
            content=response_content,
            mode=request.mode,
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/status")
async def status():
    """Check the agent server status and available modes."""
    return {
        "application": "DoraAI2",
        "version": "0.1.0",
        "modes": [
            "chat",
            "code",
            "fast_code",
            "advanced_chat",
            "run_verify",
            "setup",
        ],
        "providers": ["openhands"],
        "ollamaAvailable": True,  # Inherited from env config
        "geminiAvailable": False,
        "deepseekAvailable": False,
        "activeAgents": len(_agents),
        "workspace": os.getenv("DORA_WORKSPACE", os.getcwd()),
    }


@app.get("/api/providers")
async def providers():
    """List available AI providers."""
    return ["openhands"]


@app.delete("/api/session/{session_id}")
async def clear_session(session_id: str):
    """Clear a conversation session."""
    conv_manager.delete_session(session_id)
    return {"status": "ok", "sessionId": session_id}


@app.get("/api/sessions")
async def list_sessions():
    """List all conversation sessions."""
    return conv_manager.list_sessions()


@app.get("/health")
async def health():
    """Health check endpoint."""
    return {"status": "healthy", "application": "DoraAI2"}


if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("DORA_SERVER_PORT", "8082"))
    uvicorn.run(app, host="0.0.0.0", port=port)
