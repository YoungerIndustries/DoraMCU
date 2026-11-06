# DoraAI — Autonomous Coding Agent

A **Junie-like autonomous coding agent** built on the [OpenHands SDK](https://github.com/OpenHands/software-agent-sdk) for the DoraMCU space strategy game project.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  DoraMCU Web UI (Java/Spring Boot)                         │
│  └── dora-ai module: Chat API (Ollama, Gemini, DeepSeek)   │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP/SSE
┌──────────────────────────▼──────────────────────────────────┐
│  DoraAI2 Agent Server (Python/FastAPI)                      │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  DoraAI Agent (OpenHands SDK)                         │  │
│  │  ├── Mode-based system prompts (chat, code, etc.)     │  │
│  │  ├── Custom tools (code search, project explorer)     │  │
│  │  ├── Terminal, File Editor, Task Tracker              │  │
│  │  └── Conversation manager with persistence            │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                              │
│  Connects to: Ollama (local), Anthropic, OpenAI, etc.       │
└─────────────────────────────────────────────────────────────┘
```

## Components

| Component | Description |
|-----------|-------------|
| `agent.py` | Core DoraAI agent with mode selection |
| `prompts.py` | System prompts for each interaction mode |
| `tools.py` | Custom tools: code search, project structure |
| `conversation_manager.py` | Persistent conversation sessions |
| `cli.py` | Command-line interface |
| `server.py` | FastAPI REST API server |

## Interaction Modes

| Mode | Description | Tools |
|------|-------------|-------|
| `chat` | General Q&A, read-only | File Editor, Task Tracker |
| `code` | Multi-step implementation | Terminal, File Editor, Task Tracker |
| `fast_code` | Simple 1-3 step changes | Terminal, File Editor, Task Tracker |
| `advanced_chat` | Deep codebase analysis | File Editor, Task Tracker |
| `run_verify` | Run tests and commands | Terminal, File Editor, Task Tracker |
| `setup` | Build and configure | Terminal, File Editor, Task Tracker |

## Quick Start

### 1. Install Dependencies

```powershell
cd C:\Users\kenba\IdeaProjects\DoraMCU\dora-ai-openhands
py -m uv sync
```

### 2. Configure LLM

For local Ollama (recommended for development):
```powershell
$env:LLM_API_KEY = "ollama"
$env:LLM_MODEL = "ollama/qwen2.5:14b"
$env:LLM_BASE_URL = "http://localhost:11434"
```

### 3. Run CLI Commands

```powershell
# Analyze project structure
py -m uv run python -m dora_ai_openhands.cli --analyze --workspace ..

# Search for patterns
py -m uv run python -m dora_ai_openhands.cli --search "@Service" --workspace ../src

# List files
py -m uv run python -m dora_ai_openhands.cli --list-files "*.java" --workspace ../src

# Run a task
py -m uv run python -m dora_ai_openhands.cli "Summarize the project architecture"

# Interactive mode
py -m uv run python -m dora_ai_openhands.cli --interactive
```

### 4. Run REST API Server

```powershell
# Start the server on port 8082
py -m uv run python -m dora_ai_openhands.server
```

The server exposes:
- `POST /api/chat` — Streaming chat (SSE)
- `POST /api/chat/blocking` — Non-streaming chat
- `GET /api/status` — Server status
- `GET /api/sessions` — List sessions
- `DELETE /api/session/{id}` — Clear session
- `GET /health` — Health check

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `LLM_API_KEY` | `ollama` | API key for LLM provider |
| `LLM_MODEL` | `ollama/qwen2.5:14b` | Model identifier |
| `LLM_BASE_URL` | `http://localhost:11434` | Base URL for local models |
| `DORA_WORKSPACE` | Current dir | Working directory for agent |
| `DORA_SERVER_PORT` | `8082` | REST API server port |

## Useful Links

- **OpenHands SDK**: https://github.com/OpenHands/software-agent-sdk
- **Documentation**: https://docs.openhands.dev/sdk/getting-started
