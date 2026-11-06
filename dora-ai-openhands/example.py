import os
from openhands.sdk import LLM, Agent, Conversation, Tool
from openhands.tools.terminal import TerminalTool
from openhands.tools.file_editor import FileEditorTool
from openhands.tools.task_tracker import TaskTrackerTool


def main():
    # Configure your LLM (requires an API key)
    # Supported models: "anthropic/claude-sonnet-4-5-20250929", "gpt-4o", etc.
    llm = LLM(
        model=os.getenv("LLM_MODEL", "anthropic/claude-sonnet-4-5-20250929"),
        api_key=os.getenv("LLM_API_KEY"),
        base_url=os.getenv("LLM_BASE_URL"),  # Optional: for local models like Ollama
        reasoning_effort="none",  # Disable thinking mode for models that don't support it
    )

    # Create agent with tools
    agent = Agent(
        llm=llm,
        tools=[
            Tool(name=TerminalTool.name),
            Tool(name=FileEditorTool.name),
            Tool(name=TaskTrackerTool.name),
        ],
    )

    # Start a conversation
    conversation = Conversation(agent=agent, workspace=os.getcwd())
    conversation.send_message(
        "Explore the project structure and summarize what you find."
    )
    conversation.run()
    print("Task completed!")


if __name__ == "__main__":
    main()
