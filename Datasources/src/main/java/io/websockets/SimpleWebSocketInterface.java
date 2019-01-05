package io.websockets;

/**
 * An interface providing the functions required when creating a {@link org.java_websocket.client.WebSocketClient}.
 * Used by the {@link SimpleWebSocket}.
 */
public interface SimpleWebSocketInterface {

    void onMessage(String message);
}
