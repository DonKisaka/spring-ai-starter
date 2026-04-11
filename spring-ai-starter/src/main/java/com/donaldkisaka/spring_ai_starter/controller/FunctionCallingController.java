package com.donaldkisaka.spring_ai_starter.controller;

import com.donaldkisaka.spring_ai_starter.service.WeatherService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tools")
public class FunctionCallingController {

    private final ChatClient chatClient;
    private final WeatherService weatherService;

    public FunctionCallingController(ChatClient.Builder builder, WeatherService weatherService) {
        this.chatClient = builder.build();
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public String askAboutWeather(@RequestParam String question) {
        return chatClient.prompt()
                .user(question)
                .tools(weatherService)
                .call()
                .content();
    }
}