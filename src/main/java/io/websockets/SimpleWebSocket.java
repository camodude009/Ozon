package io.websockets;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An adapter class allowing the implementation of WebSocketClients only specifying onMessage().
 * Documentation: see {@link WebSocketClient}
 */
public class SimpleWebSocket extends WebSocketClient {

    private static final Logger logger = Logger.getLogger(SimpleWebSocket.class.getName());

    private SimpleWebSocketInterface web_socket_interface;

    public SimpleWebSocket(URI serverURI, SimpleWebSocketInterface web_socket_interface) {
        super(serverURI);
        this.web_socket_interface = web_socket_interface;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("Websocket opened: (" + handshakedata.getHttpStatus() + "," + handshakedata.getHttpStatusMessage() + ")");
    }

    @Override
    public void onMessage(String message) {
        web_socket_interface.onMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("Websocket closed: (" + code + "," + reason + "," + remote + ")");
    }

    @Override
    public void onError(Exception ex) {
        logger.log(Level.WARNING, "Websocket error", ex);
    }

}
