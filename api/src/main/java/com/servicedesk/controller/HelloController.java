package com.servicedesk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HelloController {
    @GetMapping("/api/hello")
    public Map<String, Object> hello() {
        return Map.of("status", "ok", "service", "ServiceDesk API");
    }
}
