package com.donaldkisaka.spring_ai_starter.controller;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/embeddings")
public class EmbeddingController {
    private final EmbeddingModel embeddingModel;

    public EmbeddingController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @GetMapping
    public float[] embed(@RequestParam String text) {
        return embeddingModel.embed(text);
    }

    @GetMapping("/similarity")
    public double similarity(
            @RequestParam String text1,
            @RequestParam String text2) {

        float[] v1 = embeddingModel.embed(text1);
        float[] v2 = embeddingModel.embed(text2);

        return cosineSimilarity(v1, v2);
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
