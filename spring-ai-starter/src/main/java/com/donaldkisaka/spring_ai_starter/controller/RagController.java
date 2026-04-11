package com.donaldkisaka.spring_ai_starter.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rag")
public class RagController {
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagController(VectorStore vectorStore, ChatClient.Builder builder) {
        this.vectorStore = vectorStore;
        this.chatClient = builder.build();
    }

    @PostMapping("/load")
    public String loadDocument(@RequestBody VectorStoreController.DocumentRequest request) {
        Document doc = new Document(request.content());
        vectorStore.add(List.of(doc));
        return "Document loaded";
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {

        // Step 1 — find relevant documents
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(3)
                        .build()
        );

        // Step 2 — build context from documents
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // Step 3 — send question + context to AI
        PromptTemplate template = new PromptTemplate("""
                  Use the following context to answer the question.
                  If the answer is not in the context, say "I don't know".

                  Context:
                  {context}

                  Question: {question}
                  """);

        Prompt prompt = template.create(Map.of(
                "context", context,
                "question", question
        ));

        return chatClient.prompt(prompt).call().content();
    }
}
