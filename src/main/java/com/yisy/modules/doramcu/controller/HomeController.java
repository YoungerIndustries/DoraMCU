package com.yisy.modules.doramcu.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/health")
    public Map<String, String> health() {
        log.debug("Health check requested");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        log.debug("Info endpoint requested");
        Map<String, String> response = new HashMap<>();
        response.put("application", "DoraMCU");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}
