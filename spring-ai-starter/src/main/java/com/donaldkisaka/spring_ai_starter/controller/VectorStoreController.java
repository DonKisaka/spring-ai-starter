package com.donaldkisaka.spring_ai_starter.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/vectorstore")
public class VectorStoreController {

    private final VectorStore vectorStore;

    public VectorStoreController(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @PostMapping("/add")
    public String addDocument(@RequestBody DocumentRequest request) {
        Document doc = new Document(request.content());
        vectorStore.add(List.of(doc));
        return "Document added";
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam String query) {
        return vectorStore.similaritySearch(query);
    }

    public record DocumentRequest(String content) {}
}
