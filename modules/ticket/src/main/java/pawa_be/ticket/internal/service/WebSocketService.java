package pawa_be.ticket.internal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class WebSocketService extends TextWebSocketHandler {

    private final WebSocketClient webSocketClient;
    private final WebSocketHttpHeaders headers;
    private WebSocketSession opwaSession;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${opwa.websocket.url:ws://localhost:8081/api/ws}")
    private String opwaWebSocketUrl;

    public WebSocketService() {
        this.webSocketClient = new StandardWebSocketClient();
        this.headers = new WebSocketHttpHeaders();
    }

    @PostConstruct
    public void connectToOPWA() {
        try {
            URI uri = new URI(opwaWebSocketUrl);
            log.info("Connecting to OPWA WebSocket at: {}", uri);
            webSocketClient.doHandshake(this, headers, uri).get();
            log.info("Successfully connected to OPWA WebSocket");
        } catch (InterruptedException | ExecutionException | java.net.URISyntaxException e) {
            log.error("Failed to connect to OPWA WebSocket: {}", e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        String remoteAddress = session.getRemoteAddress().toString();
        log.info("New WebSocket connection established - Session ID: {}, Remote Address: {}", sessionId, remoteAddress);
        
        // Check if this is a frontend connection by looking at the origin header
        String origin = session.getHandshakeHeaders().getFirst("Origin");
        if (origin != null && (origin.contains("localhost:3000") || origin.contains("127.0.0.1:3000"))) {
            log.info("Frontend session registered");
        } else {
            opwaSession = session;
            log.info("OPWA session registered");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            String sessionId = session.getId();
            String remoteAddress = session.getRemoteAddress().toString();
            log.info("Received message from {} (Session ID: {}): {}", remoteAddress, sessionId, payload);

            // If message is from OPWA
            if (session == opwaSession) {
                // Forward to all frontend sessions using STOMP
                log.info("Forwarding message to frontend clients via STOMP");
                messagingTemplate.convertAndSend("/topic/suspensions", payload);
                log.info("Successfully forwarded message to frontend clients");

                // Send confirmation back to OPWA
                String confirmation = "{\"type\": \"CONFIRM_SUSPENSION\"}";
                session.sendMessage(new TextMessage(confirmation));
                log.info("Sent confirmation message to OPWA");
            }
        } catch (IOException e) {
            log.error("Error handling message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        String sessionId = session.getId();
        String remoteAddress = session.getRemoteAddress().toString();
        if (session == opwaSession) {
            opwaSession = null;
            log.info("OPWA WebSocket connection closed - Session ID: {}, Remote Address: {}, Status: {}", 
                    sessionId, remoteAddress, status);
            connectToOPWA();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String sessionId = session.getId();
        String remoteAddress = session.getRemoteAddress().toString();
        if (session == opwaSession) {
            log.error("OPWA WebSocket transport error - Session ID: {}, Remote Address: {}, Error: {}", 
                    sessionId, remoteAddress, exception.getMessage());
            connectToOPWA();
        }
    }
} 