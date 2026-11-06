#!/usr/bin/env python3
"""
DoraAI CLI — Command-line interface for the DoraAI autonomous coding agent.

Usage:
    python -m dora_ai_openhands.cli [OPTIONS] MESSAGE
    
Examples:
    # Run in code mode (default)
    python -m dora_ai_openhands.cli "Add logging to the OllamaService class"
    
    # Run in chat mode (read-only)
    python -m dora_ai_openhands.cli --mode chat "Explain the project structure"
    
    # Run in advanced chat mode
    python -m dora_ai_openhands.cli --mode advanced_chat "Find all TODO comments"
    
    # Specify workspace
    python -m dora_ai_openhands.cli --workspace ../src "Refactor the AI module"
    
    # Interactive mode
    python -m dora_ai_openhands.cli --interactive
"""

import argparse
import os
import sys
from pathlib import Path

from dora_ai_openhands.agent import create_agent, DoraAgent
from dora_ai_openhands.conversation_manager import ConversationManager
from dora_ai_openhands.tools import CodeSearchTool, ProjectStructureTool


def main():
    parser = argparse.ArgumentParser(
        prog="dora-ai",
        description="DoraAI — Autonomous coding agent for DoraMCU",
    )
    parser.add_argument(
        "message",
        nargs="?",
        default=None,
        help="Task or question for the agent",
    )
    parser.add_argument(
        "--mode",
        choices=["chat", "code", "fast_code", "advanced_chat", "run_verify", "setup"],
        default="code",
        help="Interaction mode (default: code)",
    )
    parser.add_argument(
        "--workspace", "-w",
        default=None,
        help="Working directory (default: current directory)",
    )
    parser.add_argument(
        "--model", "-m",
        default=None,
        help="LLM model (default: from LLM_MODEL env var)",
    )
    parser.add_argument(
        "--interactive", "-i",
        action="store_true",
        help="Run in interactive mode (continuous chat)",
    )
    parser.add_argument(
        "--analyze",
        action="store_true",
        help="Analyze project structure and exit",
    )
    parser.add_argument(
        "--search",
        type=str,
        default=None,
        help="Search for a regex pattern in the codebase",
    )
    parser.add_argument(
        "--list-files",
        type=str,
        default=None,
        help="List files matching a glob pattern",
    )
    
    args = parser.parse_args()
    
    # Handle non-agent commands
    if args.analyze:
        _analyze_project(args.workspace)
        return
    
    if args.search:
        _search_code(args.search, args.workspace)
        return
    
    if args.list_files:
        _list_files(args.list_files, args.workspace)
        return
    
    # Interactive mode
    if args.interactive:
        _run_interactive(args.mode, args.workspace, args.model)
        return
    
    # Single-task mode
    if args.message:
        _run_task(args.message, args.mode, args.workspace, args.model)
    else:
        parser.print_help()
        sys.exit(1)


def _run_task(message: str, mode: str, workspace: str | None, model: str | None) -> None:
    """Run a single task."""
    agent = create_agent(
        mode=mode,
        workspace=workspace,
        model=model,
    )
    agent.run(message)


def _run_interactive(mode: str, workspace: str | None, model: str | None) -> None:
    """Run in interactive mode."""
    print("DoraAI Interactive Mode")
    print(f"Mode: {mode}")
    print("Type 'quit' or 'exit' to stop.")
    print("-" * 50)
    
    agent = create_agent(
        mode=mode,
        workspace=workspace,
        model=model,
    )
    
    conv_manager = ConversationManager()
    session_id = conv_manager.create_session()
    print(f"Session: {session_id}")
    print("-" * 50)
    
    while True:
        try:
            user_input = input("\nYou: ").strip()
        except (EOFError, KeyboardInterrupt):
            print("\nGoodbye!")
            break
        
        if not user_input:
            continue
        
        if user_input.lower() in ("quit", "exit", "q"):
            print("Goodbye!")
            break
        
        # Save user message
        conv_manager.add_message(session_id, "user", user_input)
        
        # Run agent
        print("\nDoraAI is thinking...")
        agent.run(user_input)


def _analyze_project(workspace: str | None) -> None:
    """Analyze project structure."""
    path = workspace or "."
    info = ProjectStructureTool.analyze_project(path)
    
    print(f"Project: {info['path']}")
    print(f"Build system: {info['build_system'] or 'Unknown'}")
    print(f"Total files: {info['total_files']}")
    print(f"Total lines: {info['total_lines']}")
    print("\nFile types:")
    
    # Sort by count, descending
    for ext, count in sorted(info["languages"].items(), key=lambda x: x[1], reverse=True):
        print(f"  {ext}: {count}")


def _search_code(pattern: str, workspace: str | None) -> None:
    """Search for a pattern in the codebase."""
    path = workspace or "."
    results = CodeSearchTool.search(pattern, path)
    
    if not results:
        print(f"No matches found for: {pattern}")
        return
    
    print(f"Found {len(results)} matches for: {pattern}")
    print("-" * 50)
    
    for result in results:
        if "error" in result:
            print(f"Error: {result['error']}")
            continue
        print(f"{result['file']}:{result['line']}")
        print(f"  {result['text']}")
        print()


def _list_files(pattern: str, workspace: str | None) -> None:
    """List files matching a pattern."""
    path = workspace or "."
    results = ProjectStructureTool.list_files(path, pattern)
    
    if not results:
        print(f"No files matching: {pattern}")
        return
    
    print(f"Found {len(results)} files matching: {pattern}")
    for result in results:
        if "error" in result:
            print(f"Error: {result['error']}")
            continue
        size_kb = result["size"] / 1024
        print(f"  {result['path']} ({size_kb:.1f} KB)")


if __name__ == "__main__":
    main()
