package com.donaldkisaka.spring_ai_starter.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping
    public String chat(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @PostMapping("/review")
    public String reviewCode(
            @RequestBody ReviewRequest request) {

        PromptTemplate template = new PromptTemplate("""
              You are an expert {language} developer.
              Review the following code and give concise feedback:

              {code}
              """);

        Prompt prompt = template.create(Map.of(
                "language", request.language(),
                "code", request.code()
        ));

        return chatClient.prompt(prompt).call().content();
    }

    public record ReviewRequest(String language, String code) {}

    public record CodeReview(
            String summary,
            String improvements,
            String rating  // e.g. "Good", "Needs Work", "Excellent"
    ) {}

    @PostMapping("/review/structured")
    public CodeReview structuredReview(@RequestBody ReviewRequest request) {

        BeanOutputConverter<CodeReview> converter = new BeanOutputConverter<>(CodeReview.class);

        PromptTemplate template = new PromptTemplate("""
              You are an expert {language} developer.
              Review the following code:

              {code}

              {format}
              """);

        Prompt prompt = template.create(Map.of(
                "language", request.language(),
                "code", request.code(),
                "format", converter.getFormat()
        ));

        String response = chatClient.prompt(prompt).call().content();
        return converter.convert(response);
    }


}
