package io.websockets;

import org.java_websocket.handshake.ServerHandshake;


/**
 * An interface providing the functions required for creating a {@link org.java_websocket.client.WebSocketClient}.
 * Used by the {@link SimpleWebSocket}.
 */
public interface SimpleWebSocketInterface {

    void onMessage(String message);

    void onOpen(ServerHandshake handshakedata);

    void onClose(int code, String reason, boolean remote);

    void onError(Exception ex);

}
