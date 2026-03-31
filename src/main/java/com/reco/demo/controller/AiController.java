package com.reco.demo.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:3000")
public class AiController {

    private final ChatClient chatClient;

    // Spring Boot will automatically inject the ChatClient.Builder
    // using the API key from your application.properties
    public AiController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/explain")
    public ResponseEntity<?> getAiRecommendation(@RequestParam String topic) {
        try {
            // 1. Validation
            if (topic == null || topic.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Please provide a topic."));
            }

            // 2. Real AI Integration Call
            // We use a specific prompt to ensure the AI gives a concise recommendation
            String aiResponse = chatClient.prompt()
                    .user("I am looking for 3 recommendations on: " + topic +
                            ". Please provide a 3-sentence expert insight on why this is recommended "
                            )
                    .call()
                    .content();

            // 3. Return the real AI content to the React frontend
            return ResponseEntity.ok(Map.of("explanation", aiResponse));

        } catch (Exception e) {
            // Print the full stack trace in IntelliJ console so we can debug API issues
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "message", "AI Assistant is currently unavailable.",
                    "details", e.getMessage()
            ));
        }
    }
}