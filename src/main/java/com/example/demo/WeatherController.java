package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "${cors.allowed.origin}") // configured in application.properties
public class WeatherController {

    @Value("${weatherapi.base.url}")
    private String baseUrl;

    @Value("${weatherapi.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WeatherController(RestTemplateBuilder builder, ObjectMapper objectMapper) {
        this.restTemplate = builder.build();
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<?> getWeather(@RequestParam String city) {
        try {
            // Encode city for URL
            String q = URLEncoder.encode(city, StandardCharsets.UTF_8);

            // WeatherAPI endpoint for current weather
            String url = String.format("%s/current.json?key=%s&q=%s", baseUrl, apiKey, q);

            // Fetch response as String
            String body = restTemplate.getForObject(url, String.class);

            // Parse JSON
            JsonNode root = objectMapper.readTree(body);
            JsonNode location = root.path("location");
            JsonNode current = root.path("current");

            // Prepare simplified response
            Map<String, Object> out = new HashMap<>();
            out.put("city", location.path("name").asText());
            out.put("region", location.path("region").asText());
            out.put("country", location.path("country").asText());
            out.put("temp", current.path("temp_c").asDouble());
            out.put("feels_like", current.path("feelslike_c").asDouble());
            out.put("humidity", current.path("humidity").asInt());
            out.put("pressure", current.path("pressure_mb").asInt());
            out.put("wind_speed", current.path("wind_kph").asDouble());
            out.put("condition", current.path("condition").path("text").asText());
            out.put("icon", current.path("condition").path("icon").asText());

            return ResponseEntity.ok(out);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to fetch weather", "details", e.getMessage()));
        }
    }
}
