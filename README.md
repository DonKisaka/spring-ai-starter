# Spring AI Starter

A hands-on learning project exploring the core concepts of [Spring AI](https://docs.spring.io/spring-ai/reference/) — the Spring framework for building AI-powered Java applications. Each controller in this project demonstrates a distinct concept, making it easy to study them in isolation.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.5 |
| AI Framework | Spring AI 2.0.0-M4 |
| LLM Backend | [Ollama](https://ollama.com) (local, `llama3.2`) |
| Embedding Model | `nomic-embed-text` via Ollama |
| Vector Store | `SimpleVectorStore` (in-memory) |
| Build Tool | Maven |

---

## Prerequisites

1. **Java 25+** installed
2. **Maven** (or use the included `./mvnw` wrapper)
3. **Ollama** running locally on `http://localhost:11434` with the following models pulled:

```bash
ollama pull llama3.2
ollama pull nomic-embed-text
```

---

## Running the Application

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

---

## Core Concepts Covered

### 1. Chat Client — Basic Prompting & Prompt Templates

> **`GET /api/chat?message=...`**

The most fundamental Spring AI concept. The `ChatClient` is the main interface for sending messages to a language model and receiving a response.

```java
chatClient.prompt()
    .user(message)
    .call()
    .content();
```

`PromptTemplate` lets you define reusable prompts with named placeholders (`{variable}`) that are filled in at runtime — keeping your prompts clean and composable.

```bash
# Basic chat
GET http://localhost:8080/api/chat?message=Explain recursion in one sentence

# Code review using a PromptTemplate
POST http://localhost:8080/api/chat/review
Content-Type: application/json

{ "language": "Java", "code": "public int add(int a, int b) { return a + b; }" }
```

---

### 2. Structured Output — `BeanOutputConverter`

> **`POST /api/chat/review/structured`**

Instead of receiving raw text, Spring AI can parse the model's response directly into a Java record or class. The `BeanOutputConverter` generates the JSON schema, injects it into the prompt, and deserialises the response automatically.

```java
BeanOutputConverter<CodeReview> converter = new BeanOutputConverter<>(CodeReview.class);
// converter.getFormat() → injects JSON schema instructions into the prompt
String response = chatClient.prompt(prompt).call().content();
CodeReview review = converter.convert(response); // typed Java object
```

```bash
POST http://localhost:8080/api/chat/review/structured
Content-Type: application/json

{ "language": "Python", "code": "def add(a,b): return a+b" }

# Response: { "summary": "...", "improvements": "...", "rating": "Good" }
```

---

### 3. Embeddings — Vector Representations of Text

> **`GET /embeddings?text=...`**
> **`GET /embeddings/similarity?text1=...&text2=...`**

Embeddings convert text into a numerical vector (array of floats). Semantically similar texts produce vectors that are close together in vector space. This is the foundation of semantic search and RAG.

The similarity endpoint demonstrates **cosine similarity** — a standard way to measure how related two pieces of text are, returning a score between -1 (opposite) and 1 (identical meaning).

```bash
# Get the embedding vector for a piece of text
GET http://localhost:8080/embeddings?text=Spring Boot is a Java framework

# Measure semantic similarity between two sentences
GET http://localhost:8080/embeddings/similarity?text1=I love cats&text2=I adore felines
# → 0.94 (high similarity)
```

---

### 4. Vector Store — Storing & Searching Documents

> **`POST /vectorstore/add`**
> **`GET /vectorstore/search?query=...`**

A `VectorStore` stores documents as embeddings and lets you search them by semantic meaning rather than exact keyword match. This project uses `SimpleVectorStore`, an in-memory implementation backed by a `HashMap` — ideal for learning without needing a database.

```bash
# Add a document
POST http://localhost:8080/vectorstore/add
Content-Type: application/json

{ "content": "Spring AI simplifies building AI applications in Java." }

# Semantic search
GET http://localhost:8080/vectorstore/search?query=What makes Java AI development easier?
```

---

### 5. RAG — Retrieval Augmented Generation

> **`POST /rag/load`**
> **`GET /rag/ask?question=...`**

RAG is the pattern of grounding a language model's answer in your own documents, preventing hallucination and keeping responses factual. The three-step flow is:

1. **Retrieve** — embed the user's question and find the most semantically similar documents in the vector store
2. **Augment** — inject those documents as context into the prompt
3. **Generate** — the LLM answers using only the provided context

```bash
# 1. Load your knowledge into the vector store
POST http://localhost:8080/rag/load
Content-Type: application/json

{ "content": "Donald Kisaka is a software engineer based in Nairobi, Kenya." }

# 2. Ask a question — the answer is grounded in what you loaded
GET http://localhost:8080/rag/ask?question=Where is Donald based?
# → "Donald Kisaka is based in Nairobi, Kenya."
```

---

### 6. Tool Calling (Function Calling)

> **`GET /tools/weather?question=...`**

Tool calling lets the LLM decide to invoke a Java method to fetch real data, rather than guessing. You annotate a service method with `@Tool` and pass the service instance to `.tools(...)` on the `ChatClient`. Spring AI handles the function description, invocation, and response injection automatically.

```java
@Tool(description = "Get the current weather for a given city")
public String getWeather(String city) { ... }

// The model decides when to call this based on the user's question
chatClient.prompt().user(question).tools(weatherService).call().content();
```

```bash
GET http://localhost:8080/tools/weather?question=What is the weather like in Nairobi?
# → "The current weather in Nairobi is 24°C and partly cloudy."
```

---

## Project Structure

```
src/main/java/com/donaldkisaka/spring_ai_starter/
├── SpringAiStarterApplication.java
├── config/
│   └── VectorStoreConfig.java       # In-memory SimpleVectorStore bean
├── controller/
│   ├── ChatController.java          # Chat, PromptTemplate, structured output
│   ├── EmbeddingController.java     # Embeddings & cosine similarity
│   ├── VectorStoreController.java   # Add & search documents
│   ├── RagController.java           # Full RAG pipeline
│   └── FunctionCallingController.java # Tool/function calling
└── service/
    └── WeatherService.java          # @Tool-annotated service
```

---

## Configuration

`src/main/resources/application.properties`:

```properties
# Active: Ollama (local)
spring.ai.ollama.chat.model=llama3.2
spring.ai.ollama.embedding.options.model=nomic-embed-text
spring.ai.ollama.base-url=http://localhost:11434

# Commented out: Anthropic Claude (swap in by setting ANTHROPIC_API_KEY)
# spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
# spring.ai.anthropic.chat.model=claude-3-5-haiku-20241022
```

To switch from Ollama to Anthropic, uncomment the Anthropic properties, add the `spring-ai-starter-model-anthropic` dependency in `pom.xml`, and comment out the Ollama dependency.

---

## Key Spring AI Concepts — Quick Reference

| Concept | Interface / Class | What it does |
|---|---|---|
| Chat | `ChatClient` | Sends prompts to an LLM, receives text back |
| Prompt Templates | `PromptTemplate` | Reusable prompts with `{variable}` placeholders |
| Structured Output | `BeanOutputConverter<T>` | Parses model output into a typed Java object |
| Embeddings | `EmbeddingModel` | Converts text to float vectors |
| Vector Store | `VectorStore` | Stores & searches documents by semantic similarity |
| RAG | `VectorStore` + `ChatClient` | Grounds LLM answers in your own documents |
| Tool Calling | `@Tool` + `.tools(...)` | Lets the LLM invoke Java methods for real data |
