package com.SmartDine.controller;

import com.SmartDine.dto.AiRequest;
import com.SmartDine.model.Restaurant;
import com.SmartDine.service.AiService;
import com.SmartDine.service.AiService.AiSuggestionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin("*")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/extract-restaurant")
    public ResponseEntity<?> extractRestaurant(@RequestBody AiRequest request) {

        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body("Message cannot be empty.");
        }

        try {
            Restaurant restaurant = aiService.extractRestaurant(request.getMessage());
            log.info("Extracted restaurant filter: {}", restaurant);
            return ResponseEntity.ok(restaurant);

        } catch (Exception e) {
            log.error("Error extracting restaurant: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error extracting restaurant: " + e.getMessage());
        }
    }


    @PostMapping("/suggest")
    public ResponseEntity<?> suggest(@RequestBody AiRequest request) {

        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body("Message cannot be empty.");
        }

        try {
            AiSuggestionResponse suggestions =
                    aiService.suggestRestaurants(request.getMessage());

            log.info("Suggestions returned: {}", suggestions.getRestaurants().size());

            return ResponseEntity.ok(suggestions);

        } catch (Exception e) {
            log.error("Error suggesting restaurants: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error suggesting restaurants: " + e.getMessage());
        }
    }
}
