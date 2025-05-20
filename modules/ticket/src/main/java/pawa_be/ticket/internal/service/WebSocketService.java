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
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WebSocketService extends TextWebSocketHandler {

    private final WebSocketClient webSocketClient;
    private final WebSocketHttpHeaders headers;
    private WebSocketSession opwaSession;
    private volatile boolean isConnecting = false;
    private volatile boolean isConnected = false;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final long INITIAL_RECONNECT_DELAY = 1000; // 1 second
    private static final long MAX_RECONNECT_DELAY = 30000; // 30 seconds
    private ScheduledExecutorService scheduler;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${opwa.websocket.url}")
    private String opwaWebSocketUrl;

    @Value("${cors.pawa_frontend_url}")
    private String pawaFrontendUrl;

    public WebSocketService() {
        this.webSocketClient = new StandardWebSocketClient();
        this.headers = new WebSocketHttpHeaders();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @PostConstruct
    public void init() {
        try {
            connectToOPWA();
        } catch (Exception e) {
            log.error("Initial connection to OPWA failed, will retry: {}", e.getMessage());
            scheduleReconnect();
        }
    }

    @PreDestroy
    public void cleanup() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    private void connectToOPWA() {
        if (isConnecting) {
            log.debug("Already attempting to connect to OPWA");
            return;
        }

        isConnecting = true;
        try {
            URI uri = new URI(opwaWebSocketUrl);
            log.info("Connecting to OPWA WebSocket at: {}", uri);
            webSocketClient.doHandshake(this, headers, uri).get();
            log.info("Successfully connected to OPWA WebSocket");
            isConnected = true;
            reconnectAttempts = 0; // Reset reconnect attempts on successful connection
        } catch (Exception e) {
            log.error("Failed to connect to OPWA WebSocket: {}", e.getMessage());
            isConnected = false;
            scheduleReconnect();
        } finally {
            isConnecting = false;
        }
    }

    private void scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            log.error("Max reconnection attempts reached. Manual intervention required.");
            return;
        }

        long delay = Math.min(INITIAL_RECONNECT_DELAY * (long) Math.pow(2, reconnectAttempts), MAX_RECONNECT_DELAY);
        reconnectAttempts++;

        log.info("Scheduling reconnection attempt {} in {} ms", reconnectAttempts, delay);
        scheduler.schedule(this::connectToOPWA, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        String remoteAddress = session.getRemoteAddress().toString();
        log.info("New WebSocket connection established - Session ID: {}, Remote Address: {}", sessionId, remoteAddress);
        
        // Check if this is a frontend connection by looking at the origin header
        String origin = session.getHandshakeHeaders().getFirst("Origin");
        if (origin != null && (origin.contains(pawaFrontendUrl) || origin.contains("127.0.0.1:3000"))) {
            log.info("Frontend session registered");
        } else {
            opwaSession = session;
            isConnected = true;
            reconnectAttempts = 0;
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
            isConnected = false;
            log.info("OPWA WebSocket connection closed - Session ID: {}, Remote Address: {}, Status: {}", 
                    sessionId, remoteAddress, status);
            scheduleReconnect();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String sessionId = session.getId();
        String remoteAddress = session.getRemoteAddress().toString();
        if (session == opwaSession) {
            log.error("OPWA WebSocket transport error - Session ID: {}, Remote Address: {}, Error: {}", 
                    sessionId, remoteAddress, exception.getMessage());
            isConnected = false;
            scheduleReconnect();
        }
    }

    public boolean isConnected() {
        return isConnected && opwaSession != null && opwaSession.isOpen();
    }

    public int getReconnectAttempts() {
        return reconnectAttempts;
    }
} 