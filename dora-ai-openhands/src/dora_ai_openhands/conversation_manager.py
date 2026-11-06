"""
Conversation manager for DoraAI.

Provides persistent conversation history and session management,
allowing the agent to maintain context across multiple interactions.
"""

import json
import os
from datetime import datetime
from pathlib import Path
from typing import Optional


class ConversationManager:
    """
    Manages conversation sessions with persistence.
    
    Stores conversation history in JSON files, allowing the agent
    to resume previous conversations and maintain context.
    """
    
    def __init__(self, storage_dir: Optional[str] = None):
        """
        Initialize the conversation manager.
        
        Args:
            storage_dir: Directory to store conversation files
        """
        self.storage_dir = Path(storage_dir or os.path.join(os.getcwd(), ".doraai", "conversations"))
        self.storage_dir.mkdir(parents=True, exist_ok=True)
        self._sessions: dict[str, dict] = {}
    
    def create_session(self, session_id: Optional[str] = None) -> str:
        """
        Create a new conversation session.
        
        Args:
            session_id: Optional session ID (auto-generated if not provided)
            
        Returns:
            The session ID
        """
        if session_id is None:
            session_id = datetime.now().strftime("%Y%m%d-%H%M%S")
        
        session = {
            "id": session_id,
            "created_at": datetime.now().isoformat(),
            "updated_at": datetime.now().isoformat(),
            "messages": [],
            "mode": "code",
            "workspace": os.getcwd(),
        }
        
        self._sessions[session_id] = session
        self._save_session(session_id)
        return session_id
    
    def add_message(self, session_id: str, role: str, content: str) -> bool:
        """
        Add a message to a conversation session.
        
        Args:
            session_id: The session ID
            role: Message role ("user", "assistant", "system")
            content: Message content
            
        Returns:
            True if message was added successfully
        """
        if session_id not in self._sessions:
            return False
        
        session = self._sessions[session_id]
        session["messages"].append({
            "role": role,
            "content": content,
            "timestamp": datetime.now().isoformat(),
        })
        session["updated_at"] = datetime.now().isoformat()
        
        self._save_session(session_id)
        return True
    
    def get_messages(self, session_id: str, last_n: Optional[int] = None) -> list[dict]:
        """
        Get messages from a conversation session.
        
        Args:
            session_id: The session ID
            last_n: Optional limit to return only the last N messages
            
        Returns:
            List of message dictionaries
        """
        if session_id not in self._sessions:
            return []
        
        messages = self._sessions[session_id]["messages"]
        if last_n:
            messages = messages[-last_n:]
        return messages
    
    def get_session(self, session_id: str) -> Optional[dict]:
        """
        Get a session by ID.
        
        Args:
            session_id: The session ID
            
        Returns:
            Session dictionary or None if not found
        """
        if session_id in self._sessions:
            return self._sessions[session_id]
        
        # Try to load from disk
        session_file = self.storage_dir / f"{session_id}.json"
        if session_file.exists():
            try:
                with open(session_file, "r", encoding="utf-8") as f:
                    session = json.load(f)
                self._sessions[session_id] = session
                return session
            except (json.JSONDecodeError, IOError):
                return None
        
        return None
    
    def list_sessions(self) -> list[dict]:
        """
        List all conversation sessions.
        
        Returns:
            List of session summaries
        """
        sessions = []
        
        # Load from disk if memory is empty
        if not self._sessions:
            for session_file in self.storage_dir.glob("*.json"):
                try:
                    with open(session_file, "r", encoding="utf-8") as f:
                        session = json.load(f)
                    self._sessions[session["id"]] = session
                except (json.JSONDecodeError, IOError):
                    continue
        
        for session in self._sessions.values():
            sessions.append({
                "id": session["id"],
                "created_at": session["created_at"],
                "updated_at": session["updated_at"],
                "message_count": len(session["messages"]),
                "mode": session.get("mode", "code"),
            })
        
        # Sort by updated_at, newest first
        sessions.sort(key=lambda s: s["updated_at"], reverse=True)
        return sessions
    
    def delete_session(self, session_id: str) -> bool:
        """
        Delete a conversation session.
        
        Args:
            session_id: The session ID
            
        Returns:
            True if session was deleted
        """
        if session_id in self._sessions:
            del self._sessions[session_id]
        
        session_file = self.storage_dir / f"{session_id}.json"
        if session_file.exists():
            session_file.unlink()
        return True
    
    def _save_session(self, session_id: str) -> None:
        """Save a session to disk."""
        session_file = self.storage_dir / f"{session_id}.json"
        try:
            with open(session_file, "w", encoding="utf-8") as f:
                json.dump(self._sessions[session_id], f, indent=2, ensure_ascii=False)
        except IOError as e:
            print(f"Warning: Failed to save session {session_id}: {e}")
