package com.donaldkisaka.spring_ai_starter.service;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;


@Service
public class WeatherService {
    @Tool(description = "Get the current weather for a given city")
    public String getWeather(String city) {
        return switch (city.toLowerCase()) {
            case "nairobi" -> "Nairobi: 24°C, Partly Cloudy";
            case "london" -> "London: 12°C, Rainy";
            case "new york" -> "New York: 18°C, Sunny";
            default -> city + ": Unknown weather";
        };
    }
}
