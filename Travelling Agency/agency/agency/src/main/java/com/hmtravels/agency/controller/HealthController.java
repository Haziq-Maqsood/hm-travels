package com.hmtravels.agency.controller; // Adjust to your actual package

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Void> pingServer() {
        // Returns a fast, empty HTTP 200 OK response
        return ResponseEntity.ok().build();
    }
}