package com.example.kafkatracking.controller;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrackingNumberController {
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    public TrackingNumberController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @GetMapping("/generate-tracking-number")
    public String generateTrackingNumber(@RequestParam String customerId) {
        String trackingNumber = "TRK-" + customerId + "-" + System.currentTimeMillis();
        kafkaTemplate.send("tracking-events", trackingNumber);
        return trackingNumber;
    }
}
