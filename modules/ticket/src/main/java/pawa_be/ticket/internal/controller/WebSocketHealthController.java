package pawa_be.ticket.internal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pawa_be.ticket.internal.service.IWebSocketService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ws/health")
public class WebSocketHealthController {
    @Autowired
    private IWebSocketService webSocketService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("connected", webSocketService.isConnected());
        health.put("reconnectAttempts", webSocketService.getReconnectAttempts());
        return ResponseEntity.ok(health);
    }
} 