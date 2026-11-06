"""
DoraAI OpenHands — A Junie-like autonomous coding agent built on OpenHands SDK.

Provides multi-step task execution, code analysis, file editing, and terminal access
with a DoraMCU-aware system prompt.
"""

from .agent import DoraAgent, create_agent
from .tools import CodeSearchTool, ProjectStructureTool
from .conversation_manager import ConversationManager

__version__ = "0.1.0"
__all__ = [
    "DoraAgent",
    "create_agent",
    "CodeSearchTool",
    "ProjectStructureTool",
    "ConversationManager",
]
