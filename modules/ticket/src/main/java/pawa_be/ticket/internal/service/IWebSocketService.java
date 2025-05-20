package pawa_be.ticket.internal.service;

public interface IWebSocketService {
    boolean isConnected();
    int getReconnectAttempts();
}