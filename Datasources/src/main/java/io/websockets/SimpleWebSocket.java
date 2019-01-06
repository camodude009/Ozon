package io.websockets;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * An adapter class allowing the implementation of WebSocketClients only specifying onMessage().
 * Documentation: see {@link WebSocketClient}
 */
public class SimpleWebSocket extends WebSocketClient {

    private SimpleWebSocketInterface web_socket_interface;

    public SimpleWebSocket(URI serverURI, SimpleWebSocketInterface web_socket_interface) {
        super(serverURI);
        this.web_socket_interface = web_socket_interface;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        web_socket_interface.onOpen(handshakedata);
    }

    @Override
    public void onMessage(String message) {
        web_socket_interface.onMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        web_socket_interface.onClose(code, reason, remote);
    }

    @Override
    public void onError(Exception ex) {
        web_socket_interface.onError(ex);
    }

}
