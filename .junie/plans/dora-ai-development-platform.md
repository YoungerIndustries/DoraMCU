---
sessionId: session-261103-030852-hrcp
---

# Requirements

### Overview & Goals
Build **DoraAI** — a software development AI platform as a separate Spring Boot module within the DoraMCU project. DoraAI will serve as a local, private AI assistant that can understand project code, generate code, detect bugs, and interact via a chat interface — similar to how Junie (the planning agent) assists developers.

### User Stories
- As a developer, I want to chat with an AI about my codebase so that I can get explanations, suggestions, and answers without leaving my environment.
- As a developer, I want the AI to analyze my project structure and understand class relationships so that it can provide accurate code suggestions.
- As a developer, I want the AI to detect potential bugs in my code by analyzing logs, patterns, and common anti-patterns.
- As a developer, I want the AI to generate code snippets and complete methods based on my descriptions.
- As a developer, I want all AI processing to happen locally via Ollama so that my code stays private.

### Scope
**In Scope:**
- Separate Spring Boot module `dora-ai` running on a different port (e.g., 8081)
- Static HTML/CSS/JS chat frontend (consistent with DoraMCU's existing pattern)
- Ollama LLM integration for all AI capabilities
- Project file system scanning with lightweight code indexing (classes, methods, imports)
- REST APIs for chat, code analysis, code generation, and bug detection
- WebSocket/SSE support for streaming AI responses
- JWT authentication shared with DoraMCU's security model

**Out of Scope:**
- Full AST-based parsing (using lightweight index instead)
- Multi-provider LLM support (Ollama only for now)
- IDE plugin integration
- Training custom models

### Functional Requirements
1. **Chat Interface**: Web-based chat where users can ask questions about code, request code generation, or ask for bug analysis.
2. **Code Understanding**: The AI can scan a project directory, build an index of symbols (classes, methods, interfaces), and answer questions about the codebase structure.
3. **Code Generation**: Given a description or context, the AI generates code snippets, methods, or entire classes.
4. **Bug Detection**: The AI analyzes code patterns, logs, or stack traces to identify potential bugs and suggest fixes.
5. **Project Indexing**: Users can register project paths; the system scans and indexes the source files for AI queries.

### Non-Functional Requirements
- **Privacy**: All AI processing happens locally via Ollama — no code leaves the machine.
- **Performance**: Streaming responses via SSE for large AI outputs.
- **Extensibility**: Plugin-like architecture for adding new AI capabilities.
- **Compatibility**: Java 27, Spring Boot 4.1.2 (same as DoraMCU).

# Technical Design

### Current Implementation
DoraMCU is a Spring Boot 4.1.2 / Java 27 project with:
- REST controllers in `com.yisy.modules.doramcu.controller`
- Services in `com.yisy.modules.doramcu.service`
- Static frontend in `src/main/resources/static/` (game.html, index.html)
- JWT auth via `JwtAuthenticationFilter` and `SecurityConfig`
- SSE support via `SseEmitter` in `GameSessionService`
- Ollama is not yet integrated; the existing `AiPlayerService` uses heuristic-based game AI

### Key Decisions
- **Separate module**: `dora-ai` as a standalone Spring Boot app (port 8081), not a multi-module Gradle subproject — keeps it simple and independently deployable.
- **Ollama as LLM backend**: Local LLM via HTTP API (`http://localhost:11434/api/generate` and `/api/chat`). Supports models like Llama 3, Codellama, and Mistral.
- **Static frontend**: HTML/CSS/JS in `src/main/resources/static/ai/` — consistent with DoraMCU's pattern (game.html, index.html).
- **Lightweight code index**: Scan `.java` files, extract class/method/import signatures via regex and simple parsing. Store in an in-memory index (ConcurrentHashMap). No external parser library needed.
- **SSE for streaming**: Use `SseEmitter` (already used in DoraMCU) to stream AI responses token-by-token.

### Proposed Changes

#### New Module: `dora-ai`
A new Spring Boot application at `C:\Users\kenba\IdeaProjects\DoraMCU\dora-ai\` with its own `build.gradle`, `src/` structure, and static resources.

#### Core Components

**1. Ollama Service** (`com.yisy.modules.doraai.service.OllamaService`)
- HTTP client to communicate with Ollama API
- Methods: `chat(String model, List<Message> messages)`, `generate(String model, String prompt)`, `streamChat(...)` returning `Flux<String>` or `SseEmitter`
- Configurable model name, temperature, and context window via `application.properties`

**2. Project Scanner** (`com.yisy.modules.doraai.service.ProjectScanner`)
- Scans a given directory for source files (`.java`, `.kt`, `.py`, etc.)
- Extracts file content and builds a `CodeIndex` with:
  - File paths and content
  - Class/interface/enum definitions with signatures
  - Method definitions with signatures and line ranges
  - Import statements
- Stores index in memory; supports incremental updates

**3. Code Index** (`com.yisy.modules.doraai.model.CodeIndex`)
- In-memory data structure holding project symbols
- Methods: `addFile()`, `removeFile()`, `searchSymbols(query)`, `getFileContent(path)`, `getProjectOverview()`
- Thread-safe with `ConcurrentHashMap`

**4. AI Chat Service** (`com.yisy.modules.doraai.service.AiChatService`)
- Orchestrates chat interactions between user, code index, and Ollama
- Builds system prompts with project context
- Maintains conversation history per session
- Routes queries to appropriate AI modes (chat, code gen, bug detection)

**5. REST Controllers**
- `AiChatController` — `/api/ai/chat` (POST for messages, SSE for streaming)
- `ProjectController` — `/api/ai/projects` (register, scan, list projects)
- `CodeAnalysisController` — `/api/ai/analyze` (bug detection, code review)
- `CodeGenerationController` — `/api/ai/generate` (code generation requests)

**6. Static Frontend**
- `ai.html` — main chat interface with sidebar for project navigation
- `css/ai.css` — styling consistent with DoraMCU's game.css
- `js/ai-chat.js` — chat logic, SSE connection, message handling
- `js/ai-api.js` — REST API client for AI endpoints

### Data Models / Contracts

**Ollama API Messages:**
```
POST http://localhost:11434/api/chat
{
  "model": "llama3",
  "messages": [
    {"role": "system", "content": "You are a code assistant..."},
    {"role": "user", "content": "Explain the Game class..."}
  ],
  "stream": true
}
```

**Chat Request/Response:**
```
POST /api/ai/chat
{
  "sessionId": "uuid",
  "message": "Explain the Game class structure",
  "mode": "chat"  // chat | code-gen | bug-detect
}
```
SSE Response: streaming tokens via `SseEmitter`

**Project Registration:**
```
POST /api/ai/projects
{
  "name": "DoraMCU",
  "path": "C:\\Users\\kenba\\IdeaProjects\\DoraMCU\\src"
}
```

**Code Analysis Request:**
```
POST /api/ai/analyze/bugs
{
  "projectId": "uuid",
  "filePaths": ["Game.java", "AiPlayerService.java"],
  "description": "Check for thread safety issues"
}
```

### File Structure
```
dora-ai/
├── build.gradle
├── settings.gradle
└── src/
    ├── main/
    │   ├── java/com/yisy/modules/doraai/
    │   │   ├── DoraAiApplication.java
    │   │   ├── config/
    │   │   │   └── OllamaConfig.java
    │   │   ├── controller/
    │   │   │   ├── AiChatController.java
    │   │   │   ├── ProjectController.java
    │   │   │   ├── CodeAnalysisController.java
    │   │   │   └── CodeGenerationController.java
    │   │   ├── dto/
    │   │   │   ├── ChatRequest.java
    │   │   │   ├── ChatResponse.java
    │   │   │   ├── ProjectRequest.java
    │   │   │   ├── AnalysisRequest.java
    │   │   │   └── GenerationRequest.java
    │   │   ├── model/
    │   │   │   ├── CodeIndex.java
    │   │   │   ├── CodeFile.java
    │   │   │   ├── CodeSymbol.java
    │   │   │   └── Conversation.java
    │   │   └── service/
    │   │       ├── OllamaService.java
    │   │       ├── ProjectScanner.java
    │   │       ├── AiChatService.java
    │   │       ├── CodeAnalysisService.java
    │   │       └── CodeGenerationService.java
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           ├── ai.html
    │           ├── css/ai.css
    │           └── js/
    │               ├── ai-chat.js
    │               └── ai-api.js
    └── test/java/...
```

### Architecture Diagram

``` mermaid
graph LR
    U[User Browser] -->|HTTP/SSE| C[Controllers]
    C -->|chat| CS[AiChatService]
    C -->|scan| PS[ProjectScanner]
    C -->|analyze| AS[CodeAnalysisService]
    C -->|generate| GS[CodeGenerationService]
    CS -->|context| CI[CodeIndex]
    AS -->|context| CI
    GS -->|context| CI
    PS -->|build| CI
    CS -->|prompts| OS[OllamaService]
    AS -->|prompts| OS
    GS -->|prompts| OS
    OS -->|HTTP| O[Ollama LLM]
    style U fill:#e1f5fe
    style C fill:#fff3e0
    style CS fill:#e8f5e9
    style OS fill:#f3e5f5
    style O fill:#fce4ec
    style CI fill:#fff9c4
```

### Risks
- **Ollama availability**: The platform requires Ollama to be running locally. Need to handle connection failures gracefully and show clear error messages.
- **Large projects**: Scanning very large projects may consume significant memory. Consider limiting file sizes or adding pagination.
- **LLM context limits**: Large codebases may exceed the LLM's context window. Need to implement smart context selection (only include relevant files).

# Testing

### Validation Approach
Validate each component layer independently, then integrate:
1. Unit tests for service logic (code indexing, prompt building)
2. Integration tests with mocked Ollama responses
3. Manual verification of the chat UI with a real Ollama instance

### Key Scenarios
- **Project scanning**: Register a project path, verify that files are indexed and symbols are extracted correctly
- **Chat interaction**: Send a message, verify that the response streams back via SSE and includes relevant code context
- **Code generation**: Request a new class/method, verify that generated code is syntactically valid
- **Bug detection**: Submit code with known issues, verify that the AI identifies them
- **Ollama offline**: Verify graceful error handling when Ollama is not running

### Edge Cases
- Empty project directories
- Very large files (e.g., generated code)
- Non-Java files in the project
- Ollama API timeouts or partial responses
- Concurrent project scans
- Special characters in file paths

### Test Changes
- Add unit tests for `ProjectScanner` with sample Java files
- Add unit tests for `CodeIndex` search functionality
- Add integration tests for `AiChatController` with mocked `OllamaService`
- Add unit tests for prompt building logic in `AiChatService`

# Deep Learning Extension — Game Strategy Learning

### Overview
Add deep learning capabilities to DoraAI using **Deeplearning4j** so the game AI can learn optimal strategies through neural networks instead of relying solely on heuristics. The neural network will observe game states (resources, fleet positions, enemy positions) and predict the best action (build, move, attack, wait).

### Key Decisions
- **Deeplearning4j**: Pure Java deep learning library, compatible with Java 27, no Python dependency
- **Supervised learning first**: Train on self-play game data where the neural network learns from outcomes (win/loss/reward)
- **Feature-based state representation**: Convert game state into numerical feature vectors for the neural network
- **Action prediction**: Neural network outputs action probabilities; highest probability action is selected
- **Integration with existing AI**: Neural network augments the heuristic AI — when enabled, the network's prediction is combined with heuristic scores

### Neural Network Architecture

**Input Features** (32-dimensional vector):
- Player resources: minerals, energy, food, research, influence (5 features, normalized)
- Player assets: planet count, fleet count, total ship count (3 features)
- Fleet positions: nearest fleet x, y, z coordinates (3 features, normalized)
- Enemy info: nearest enemy distance, enemy fleet count, enemy total ships (3 features)
- Game state: current turn (normalized), number of players, player rank by resources (3 features)
- Relative strength: player power vs average enemy power, player power vs nearest enemy (2 features)
- Resource trends: minerals delta (last turn), energy delta (last turn) (2 features)
- Strategic context: has adjacent enemy, has buildable resources, has idle fleet (3 features)
- Difficulty bias: easy=0.0, medium=0.5, hard=1.0 (1 feature)
- Turn phase: early (0-10), mid (11-30), late (31+) encoded as 2 features (2 features)
- Total: 28 features, padded to 32 with zeros for future expansion

**Network Structure**:
- Input layer: 32 neurons
- Hidden layer 1: 64 neurons, ReLU activation, Dropout(0.3)
- Hidden layer 2: 128 neurons, ReLU activation, Dropout(0.3)
- Hidden layer 3: 64 neurons, ReLU activation
- Output layer: 5 neurons, Softmax activation (one per action type)

**Output Actions**:
1. BUILD_SHIP — Build a ship (with ship type determined by heuristic)
2. MOVE_FLEET — Move a fleet toward a target
3. ENGAGE_COMBAT — Attack an enemy fleet
4. EXPAND — Move to unexplored territory
5. WAIT — Do nothing this turn (defensive posture)

### Training Pipeline

**Data Collection**:
- Self-play games: AI plays against itself at different difficulty levels
- Each turn records: state features, action taken, reward received
- Reward signal: +10 for winning combat, +5 for building ships, +1 for moving toward enemy, -10 for losing fleet, -1 for waiting

**Training Process**:
- Collect 1000+ game episodes (each episode = one full game)
- Flatten episodes into state-action-reward triples
- Normalize features using min-max scaling
- Train using Adam optimizer, learning rate 0.001, batch size 32
- Evaluate on held-out test set (20% of data)

### Core Components

**1. Game State Feature Extractor** (`com.yisy.modules.doraai.dl.GameStateFeatureExtractor`)
- Converts `GameStateResponse` and `Game` objects into feature vectors
- Normalizes resource values, positions, and counts
- Thread-safe for concurrent game sessions

**2. Strategy Neural Network** (`com.yisy.modules.doraai.dl.StrategyNetwork`)
- Builds and manages the neural network using Deeplearning4j
- Methods: `predict(action)`, `train(data)`, `save(modelPath)`, `load(modelPath)`
- Supports model persistence for trained models

**3. Training Data Collector** (`com.yisy.modules.doraai.dl.TrainingDataCollector`)
- Records game states, actions, and rewards during self-play
- Stores data in memory and exports to CSV for offline training
- Manages episode boundaries (game start/end)

**4. Self-Play Engine** (`com.yisy.modules.doraai.dl.SelfPlayEngine`)
- Runs automated games where AI plays against AI
- Configurable number of episodes, difficulty levels
- Collects training data from each game

**5. DL Game AI Service** (`com.yisy.modules.doraai.dl.DlGameAiService`)
- Combines neural network predictions with heuristic scores
- Configurable blend factor (0.0 = pure heuristic, 1.0 = pure neural network)
- REST API for triggering training, evaluating models, and monitoring performance

**6. REST Controllers**
- `DlTrainingController` — `/api/ai/dl/training` (start training, monitor progress, export data)
- `DlModelController` — `/api/ai/dl/model` (save, load, evaluate models)
- `DlSelfPlayController` — `/api/ai/dl/selfplay` (run self-play episodes)

### File Structure
```
dora-ai/src/main/java/com/yisy/modules/doraai/dl/
├── config/
│   └── DlConfig.java                    # Neural network hyperparameters
├── model/
│   ├── GameStateFeatures.java           # Feature vector representation
│   └── ActionReward.java               # Training data point
├── network/
│   ├── StrategyNetwork.java             # Neural network builder and manager
│   └── GameStateFeatureExtractor.java   # Game state to feature vector
├── training/
│   ├── TrainingDataCollector.java       # Collects state-action-reward data
│   ├── SelfPlayEngine.java             # Runs self-play games
│   └── TrainingManager.java            # Orchestrates training pipeline
├── service/
│   └── DlGameAiService.java            # Combines DL predictions with heuristics
└── controller/
    ├── DlTrainingController.java        # Training REST API
    ├── DlModelController.java          # Model management REST API
    └── DlSelfPlayController.java       # Self-play REST API
```

### Architecture Diagram

``` mermaid
graph LR
    SP[Self-Play Engine] -->|game states| TDC[Training Data Collector]
    TDC -->|CSV data| TM[Training Manager]
    TM -->|train| SN[Strategy Network]
    SN -->|predict action| DLG[DlGameAiService]
    DLG -->|blend with heuristics| AI[AiPlayerService]
    AI -->|actions| GS[Game Session]
    GS -->|results| TDC
    style SP fill:#e1f5fe
    style TDC fill:#fff3e0
    style SN fill:#f3e5f5
    style DLG fill:#e8f5e9
    style AI fill:#fff9c4
    style GS fill:#fce4ec
```

### Risks
- **Deeplearning4j dependencies**: Large dependency footprint (~50MB+). Need to manage build size.
- **Training time**: Initial training may take hours on CPU. Consider providing pre-trained model or using GPU acceleration.
- **Model quality**: Neural network may not outperform heuristics initially. Need careful reward design and hyperparameter tuning.
- **Game state complexity**: 28+ features may not capture all strategic nuances. May need iterative feature engineering.

# Testing (Deep Learning)

### Validation Approach
1. Unit tests for feature extraction (deterministic)
2. Unit tests for neural network building and prediction
3. Integration tests for self-play engine
4. Manual evaluation: compare DL AI win rate vs heuristic AI win rate

### Key Scenarios
- **Feature extraction**: Verify that game states are correctly converted to feature vectors
- **Network prediction**: Verify that the network outputs valid action probabilities
- **Self-play**: Run 10 self-play episodes and verify data collection
- **Training**: Train on collected data and verify loss decreases
- **Model persistence**: Save and load model, verify predictions are consistent

### Delivery Steps (Deep Learning)

### ✓ Step 7: Add Deeplearning4j dependencies and configuration
Set up the deep learning infrastructure in the dora-ai module.
- Add Deeplearning4j dependencies to `dora-ai/build.gradle` (dl4j-core, nd4j-native-platform)
- Create `DlConfig.java` with neural network hyperparameters (learning rate, layers, batch size)
- Create `GameStateFeatures.java` model — 32-dimensional feature vector
- Create `ActionReward.java` model — training data point (state, action, reward)
- Create `GameStateFeatureExtractor.java` — converts game state to feature vectors
- Add unit tests for feature extraction with sample game states

### ✓ Step 8: Implement strategy neural network
Build the neural network for action prediction.
- Create `StrategyNetwork.java` — builds, trains, and manages the neural network
- Implement network architecture: 32 → 64 → 128 → 64 → 5 (with ReLU, Dropout, Softmax)
- Implement prediction method that returns action probabilities
- Implement model save/load using Deeplearning4j's ModelSerializer
- Add unit tests for network building, prediction, and model persistence

### ✓ Step 9: Implement training data collection and self-play
Build the data pipeline for training the neural network.
- Create `TrainingDataCollector.java` — records state-action-reward triples during games
- Create `SelfPlayEngine.java` — runs automated AI vs AI games
- Create `TrainingManager.java` — orchestrates training: data loading, normalization, model training
- Implement CSV export/import for training data
- Add unit tests for data collection and self-play engine

### ✓ Step 10: Integrate deep learning AI with game session
Connect the neural network to the game AI system.
- Create `DlGameAiService.java` — combines neural network predictions with heuristic scores
- Implement configurable blend factor for heuristic vs neural network decisions
- Create REST controllers: `DlTrainingController`, `DlModelController`, `DlSelfPlayController`
- Add endpoints for starting training, monitoring progress, and evaluating models
- Create static frontend page for monitoring training progress (`dl-training.html`)
- Add unit tests for DL AI service and integration with game sessions

---

# Voice & Translation Extension

### Overview
Add **voice recognition** (speech-to-text chat input) and **chat translation** to DoraAI. Users speak into their microphone; their voice is transcribed into text chat messages. The AI detects the language and responds in the user's language — all via Ollama locally.

### User Stories
- As a developer, I want to speak my chat messages instead of typing so that I can interact hands-free.
- As a developer, I want to chat in my native language and have the AI respond in the same language.
- As a developer, I want automatic language detection so I don't need to select a language manually.

### Key Decisions
- **Ollama Whisper for STT**: Browser records audio via Web Audio API, encodes to 16kHz mono WAV base64, sends to Ollama `/api/generate` with audio in the `images` field (Ollama's audio input mechanism for Whisper).
- **Multilingual LLM for translation**: Llama 3 supports multiple languages. The system prompt instructs the model to respond in the detected language.
- **Browser MediaRecorder**: Captures microphone audio client-side; no server-side audio library needed.
- **Language auto-detection**: Whisper returns detected language with transcription; for text input, the LLM detects from context.

### Proposed Changes

#### 1. Extend OllamaService with Transcription
Add `transcribe(String model, String audioBase64, String languageHint)` to `OllamaService`.
- Sends audio as base64 WAV in the `images` field to Ollama `/api/generate` with the Whisper model
- Parses response for transcribed text and detected language

#### 2. Voice Service (`com.yisy.modules.doraai.voice.VoiceService`)
- Validates audio format and size (max 60 seconds, max 10MB)
- Calls `OllamaService.transcribe()` with the configured Whisper model
- Returns transcribed text and detected language
- Configurable via `ollama.whisper-model=whisper` in `application.properties`

#### 3. Translation Service (`com.yisy.modules.doraai.voice.TranslationService`)
- Uses Whisper's detected language or asks the LLM to detect for text input
- Builds language-aware system prompts for `AiChatService`
- Method: `buildLanguageAwarePrompt(String userLanguage, String targetLanguage, String originalPrompt)`

#### 4. Voice Controller (`com.yisy.modules.doraai.voice.VoiceController`)
- `POST /api/ai/voice/transcribe` — transcribe audio to text
- `POST /api/ai/voice/translate` — translate text between languages
- `GET /api/ai/voice/languages` — list supported languages

#### 5. Updated Chat Service
- Extend `AiChatService` to support language-aware system prompts
- Update `ChatRequest` DTO with optional `sourceLanguage` and `inputType` fields
- When `sourceLanguage` is present, instruct the model to respond in that language

#### 6. Frontend Voice Integration
- Microphone button in `ai.html` chat input area
- `js/ai-voice.js` — recording via `MediaRecorder`, WAV encoding, send to backend
- Language indicator in sidebar
- Translation mode toggle in sidebar

### Data Models / Contracts

**Transcribe Request:**
```
POST /api/ai/voice/transcribe
{"audioBase64": "UklGR...", "language": "en"}
```

**Transcribe Response:**
```
{"text": "Explain the Game class", "language": "en", "confidence": 0.95}
```

**Translate Request:**
```
POST /api/ai/voice/translate
{"text": "Hello", "sourceLanguage": "en", "targetLanguage": "zh"}
```

**Translate Response:**
```
{"translatedText": "你好", "sourceLanguage": "en", "targetLanguage": "zh"}
```

**Updated ChatRequest:**
```
POST /api/ai/chat
{"sessionId": "uuid", "message": "...", "mode": "chat", "sourceLanguage": "en", "inputType": "voice"}
```

### File Structure
```
dora-ai/src/main/java/com/yisy/modules/doraai/voice/
├── VoiceService.java
├── TranslationService.java
├── VoiceController.java
└── dto/
    ├── TranscribeRequest.java
    ├── TranscribeResponse.java
    ├── TranslateRequest.java
    └── TranslateResponse.java

dora-ai/src/main/resources/static/js/
└── ai-voice.js
```

### Architecture Diagram

``` mermaid
graph LR
    U[User Browser] -->|record audio| VR[ai-voice.js]
    VR -->|base64 WAV| VC[VoiceController]
    VC -->|audio| VSV[VoiceService]
    VSV -->|Whisper transcribe| OS[OllamaService]
    OS -->|/api/generate| OW[Ollama Whisper]
    OS -->|chat| OL[Ollama LLM]
    VSV -->|text + lang| VC
    VC -->|transcript| U
    TS[TranslationService] -->|lang prompt| ACS[AiChatService]
    ACS -->|chat w/ lang| OS
    U -->|voice msg| ACS
    style U fill:#e1f5fe
    style VR fill:#fff9c4
    style VC fill:#fff3e0
    style VSV fill:#e8f5e9
    style OS fill:#f3e5f5
    style OW fill:#fce4ec
    style OL fill:#fce4ec
    style TS fill:#e1f5fe
    style ACS fill:#e8f5e9
```

### Risks
- **Ollama Whisper model**: User must run `ollama pull whisper`. Need to check availability and show clear errors.
- **Browser microphone permissions**: Handle denial gracefully.
- **Audio quality**: Background noise affects transcription accuracy.
- **WAV encoding in browser**: `MediaRecorder` records WebM/Opus; manual PCM-to-WAV encoding needed in JavaScript.
- **Large audio**: Limit recording to 60 seconds max to avoid large base64 payloads.
- **LLM language support**: Llama 3 has good multilingual support; smaller models may struggle.

### Testing (Voice & Translation)

### Validation Approach
1. Unit tests for audio validation and WAV format checking
2. Unit tests for transcription request/response parsing
3. Integration tests for VoiceController with mocked OllamaService
4. Manual verification of voice recording and transcription in the browser

### Key Scenarios
- **Voice recording**: Record audio in browser, verify WAV encoding
- **Transcription**: Send audio to backend, verify Whisper transcribes correctly
- **Language detection**: Speak in different languages, verify correct detection
- **Translation chat**: Send a message in one language, verify AI responds in the same language
- **Microphone unavailable**: Verify graceful error handling

### Edge Cases
- No microphone permission
- Very short recordings (< 1 second)
- Very long recordings (> 60 seconds)
- Silent audio
- Ollama Whisper model not available
- Mixed-language input

### Delivery Steps (Voice & Translation)

### ✓ Step 11: Extend OllamaService and add voice backend
Add speech-to-text transcription support to OllamaService and create voice backend services.
- Add `transcribe(String model, String audioBase64, String languageHint)` method to `OllamaService`
- The method sends audio as base64 WAV in the `images` field to Ollama `/api/generate` with the Whisper model
- Create `VoiceService.java` — validates audio, calls `OllamaService.transcribe()`, returns transcription result
- Create `TranslationService.java` — builds language-aware system prompts, detects language
- Create DTOs: `TranscribeRequest.java`, `TranscribeResponse.java`, `TranslateRequest.java`, `TranslateResponse.java`
- Create `VoiceController.java` with endpoints: `POST /api/ai/voice/transcribe`, `POST /api/ai/voice/translate`, `GET /api/ai/voice/languages`
- Add `ollama.whisper-model=whisper` to `application.properties`
- Update `ChatRequest.java` to include optional `sourceLanguage` and `inputType` fields
- Update `AiChatService` to build language-aware system prompts when `sourceLanguage` is present
- Add unit tests for `VoiceService`, `TranslationService`, and `OllamaService.transcribe()` with mocked responses

### ✓ Step 12: Build frontend voice recording and translation UI
Add microphone recording, transcription integration, and language indicators to the chat frontend.
- Create `js/ai-voice.js` — handles microphone access, records audio, encodes to WAV, sends to backend
- Add microphone button, recording indicator, and language badge to `ai.html`
- Update `ai-chat.js` with voice integration and language tracking
- Update `ai-api.js` with `transcribeAudio()`, `translateText()`, `getLanguages()` methods
- Style new UI elements in `css/ai.css`

### ✓ Step 13: Create voice & translation monitoring page
Build a dedicated monitoring page for voice and translation features.
- Create `voice-monitor.html` — dashboard showing transcription stats, language support, translation testing, and voice recording demo
- Add "Voice Monitor" link in `ai.html` sidebar under a new "Voice & Translation" section
- The page provides real-time transcription testing, language detection demo, translation between languages, and recording controls
- Style matches the existing dark theme from `ai.css`

# Delivery Steps

### ✓ Step 1: Set up dora-ai module structure and Ollama integration
Create the new `dora-ai` Spring Boot module with its build configuration, application entry point, and Ollama service.
- Create `dora-ai/build.gradle` with Spring Boot 4.1.2, Java 27, web starter, and SSE support
- Create `dora-ai/settings.gradle` with project name `dora-ai`
- Create `DoraAiApplication.java` as the main entry point
- Create `OllamaConfig.java` with configurable model name, base URL, and timeout
- Create `OllamaService.java` with methods for chat, generate, and streaming chat via Ollama HTTP API
- Create `application.properties` with Ollama defaults (localhost:11434, llama3 model)
- Add unit tests for `OllamaService` with mocked HTTP responses

### ✓ Step 2: Implement project scanner and code index
Build the code analysis foundation that scans projects and indexes symbols for AI context.
- Create `CodeFile.java`, `CodeSymbol.java`, and `CodeIndex.java` models
- Create `ProjectScanner.java` that scans directories for source files and extracts class/method/import signatures
- Implement symbol search in `CodeIndex` (by name, by type, by file)
- Create `ProjectController.java` with endpoints to register, scan, and list projects
- Add unit tests for `ProjectScanner` with sample Java files from the DoraMCU project
- Add unit tests for `CodeIndex` search functionality

### ✓ Step 3: Implement AI chat service with streaming
Build the core chat capability that connects user messages, code context, and Ollama responses.
- Create `Conversation.java` model to track message history per session
- Create `ChatRequest.java` and `ChatResponse.java` DTOs
- Create `AiChatService.java` that builds system prompts with project context, maintains conversation history, and streams responses
- Create `AiChatController.java` with POST `/api/ai/chat` endpoint and SSE streaming support
- Implement smart context selection: only include relevant files from the code index based on the query
- Add integration tests for `AiChatController` with mocked `OllamaService`

### ✓ Step 4: Implement code analysis and generation services
Add specialized AI capabilities for bug detection and code generation.
- Create `CodeAnalysisService.java` that builds prompts for bug detection and code review
- Create `CodeGenerationService.java` that builds prompts for code generation with context from the code index
- Create `CodeAnalysisController.java` with `/api/ai/analyze/bugs` and `/api/ai/analyze/review` endpoints
- Create `CodeGenerationController.java` with `/api/ai/generate` endpoint
- Create request/response DTOs: `AnalysisRequest.java`, `GenerationRequest.java`
- Add unit tests for prompt building logic in both services

### ✓ Step 5: Build static chat frontend
Create the web-based chat interface for interacting with the AI.
- Create `ai.html` with a chat layout: message area, input field, project selector sidebar, and mode selector (chat/code-gen/analyze)
- Create `css/ai.css` with styling consistent with DoraMCU's `game.css` design language
- Create `js/ai-api.js` with REST API client functions for chat, project management, analysis, and generation
- Create `js/ai-chat.js` with chat logic: message sending, SSE connection for streaming responses, message rendering, and project selection
- Support markdown rendering for code blocks in AI responses
- Test the frontend manually with a running Ollama instance

### ✓ Step 6: Add unit tests
Add comprehensive unit tests for all core components of the dora-ai module.
- Create `CodeIndexTest.java` — test add/remove files, symbol search by name and type, project overview, and edge cases
- Create `ProjectScannerTest.java` — test symbol extraction from sample Java code, directory scanning, and edge cases
- Create `ConversationTest.java` — test message history, session management, and clear functionality
- Create `OllamaServiceTest.java` — test chat, generate, and isAvailable with mocked HTTP responses
- Create `AiChatServiceTest.java` — test system prompt building, conversation management, and context selection
- Create `CodeAnalysisServiceTest.java` — test bug detection and code review prompt building
- Create `CodeGenerationServiceTest.java` — test code generation prompt building
- Verify all tests pass with `gradlew test`

---