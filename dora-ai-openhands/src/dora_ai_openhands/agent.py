"""
DoraAI Agent — the core autonomous coding agent.

Wraps OpenHands SDK's Agent and Conversation with DoraAI-specific
system prompts, custom tools, and mode selection.
"""

import os
from pathlib import Path
from typing import Optional

from openhands.sdk import LLM, Agent, Conversation, Tool
from openhands.tools.terminal import TerminalTool
from openhands.tools.file_editor import FileEditorTool
from openhands.tools.task_tracker import TaskTrackerTool

from .prompts import get_system_prompt


class DoraAgent:
    """
    DoraAI autonomous coding agent built on OpenHands SDK.
    
    Supports multiple interaction modes (similar to Junie):
    - chat: General Q&A and code explanation (read-only)
    - code: Multi-step implementation with tests
    - fast_code: Simple trivial changes in 1-3 steps
    - advanced_chat: Deep codebase analysis without changes
    - run_verify: Execute tests and commands to collect evidence
    - setup: Build, install, configure infrastructure
    """
    
    def __init__(
        self,
        model: Optional[str] = None,
        api_key: Optional[str] = None,
        base_url: Optional[str] = None,
        mode: str = "code",
        workspace: Optional[str] = None,
        reasoning_effort: str = "none",
    ):
        """
        Initialize the DoraAI agent.
        
        Args:
            model: LLM model identifier (e.g., "ollama/qwen2.5:14b")
            api_key: API key for the LLM provider
            base_url: Base URL for local models (e.g., Ollama)
            mode: Interaction mode (chat, code, fast_code, etc.)
            workspace: Working directory for the agent
            reasoning_effort: Reasoning effort level ("none" for models without thinking mode)
        """
        self.mode = mode
        self.workspace = workspace or os.getcwd()
        
        # Configure LLM from environment or parameters
        self.llm = LLM(
            model=model or os.getenv("LLM_MODEL", "ollama/qwen2.5:14b"),
            api_key=api_key or os.getenv("LLM_API_KEY", "ollama"),
            base_url=base_url or os.getenv("LLM_BASE_URL", "http://localhost:11434"),
            reasoning_effort=reasoning_effort,
        )
        
        # Build system prompt for the selected mode
        self.system_prompt = get_system_prompt(mode)
        
        # Determine tools based on mode
        self.tools = self._build_tools()
        
        # Create the OpenHands agent
        self.agent = Agent(
            llm=self.llm,
            tools=self.tools,
            system_prompt=self.system_prompt,
        )
        
        self.conversation: Optional[Conversation] = None
    
    def _build_tools(self) -> list[Tool]:
        """Build the tool list based on the current mode."""
        # Read-only modes: only file editor (read) and task tracker
        read_only_modes = {"chat", "advanced_chat", "run_verify"}
        
        if self.mode in read_only_modes:
            return [
                Tool(name=FileEditorTool.name),
                Tool(name=TaskTrackerTool.name),
            ]
        else:
            # Full-featured modes: terminal, file editor, task tracker
            return [
                Tool(name=TerminalTool.name),
                Tool(name=FileEditorTool.name),
                Tool(name=TaskTrackerTool.name),
            ]
    
    def run(self, message: str) -> Conversation:
        """
        Run the agent with a task message.
        
        Args:
            message: The task or question for the agent
            
        Returns:
            The Conversation object with the results
        """
        print(f"DoraAI v0.1.0 — Mode: {self.mode}")
        print(f"Workspace: {self.workspace}")
        print(f"Model: {self.llm.model}")
        print("-" * 50)
        
        self.conversation = Conversation(
            agent=self.agent,
            workspace=self.workspace,
        )
        self.conversation.send_message(message)
        self.conversation.run()
        
        print("-" * 50)
        print("Task completed!")
        return self.conversation
    
    def run_async(self, message: str) -> Conversation:
        """
        Start the agent asynchronously (non-blocking).
        
        Args:
            message: The task or question for the agent
            
        Returns:
            The Conversation object (runs in background)
        """
        self.conversation = Conversation(
            agent=self.agent,
            workspace=self.workspace,
        )
        self.conversation.send_message(message)
        # Run in background
        import threading
        thread = threading.Thread(target=self.conversation.run, daemon=True)
        thread.start()
        return self.conversation


def create_agent(
    mode: str = "code",
    workspace: Optional[str] = None,
    **llm_kwargs
) -> DoraAgent:
    """
    Factory function to create a DoraAI agent.
    
    Args:
        mode: Interaction mode
        workspace: Working directory
        **llm_kwargs: Additional LLM configuration (model, api_key, base_url)
        
    Returns:
        Configured DoraAgent instance
    """
    return DoraAgent(mode=mode, workspace=workspace, **llm_kwargs)
