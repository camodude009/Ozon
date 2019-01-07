package application;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import io.grpc.DataServer;
import io.grpc.collector.DataPoint;
import io.websockets.SimpleWebSocket;
import io.websockets.SimpleWebSocketInterface;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link SpringBootApplication} implementing parts of the BitFinex API (v1).
 *
 * @see <a href="https://docs.bitfinex.com/v1/docs">https://docs.bitfinex.com/v1/docs</a>
 */
@SpringBootApplication
public class BitFinexDatasource extends Datasource {

    private static final Logger logger = Logger.getLogger(BitFinexDatasource.class.getName());
    private static Gson gson = new Gson();

    /**
     * Simple websocket needed for onOpen() calls in the {@link BitFinexWebSocketImpl}.
     */
    private static SimpleWebSocket ws;

    public static void main(String[] args) {
        // start spring application
        SpringApplication.run(BitFinexDatasource.class, args);
        // create datasource and grpc server
        Datasource ds = new BitFinexDatasource();
        DataServer s = new DataServer(ds, 50052);
        try {
            // start datasource and grpc server
            new Thread(ds).start();
            s.start();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to start GRPC server", e);
        }
        try {
            s.blockUntilShutdown();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted while waiting for shutdown", e);
        }
    }

    @Override
    public void run() {
        // url for websocket
        String url = "wss://api.bitfinex.com/ws/";
        URI serverURI = URI.create(url);
        // create connection and connect using the BitFinexWebSocketImpl to handle messages
        ws = new SimpleWebSocket(serverURI, new BitFinexWebSocketImpl());
        try {
            ws.setSocket(SSLSocketFactory.getDefault().createSocket());
            ws.connect();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error creating websocket", e);
        }
    }

    private class BitFinexWebSocketImpl implements SimpleWebSocketInterface {

        @Override
        public void onMessage(String message) {
            // parse message and add DataPoint to internal data store
            // example message: "[ 5, 'te', '1234-BTCUSD', 1443659698, 236.42, 0.49064538 ]"
            try {
                List<String> l = gson.fromJson(message, new TypeToken<List<String>>() {
                }.getType());
                if (l.size() == 6 && l.get(1).equals("te")) {
                    String market = "bitfinex-" + l.get(2).split("-")[1];
                    Long time = Long.parseLong(l.get(3)) * 1000;
                    Double price = Double.parseDouble(l.get(4));
                    Double signedAmount = Double.parseDouble(l.get(5));
                    Double amount = Math.abs(signedAmount);
                    DataPoint.Type type = signedAmount > 0 ? DataPoint.Type.BUY : DataPoint.Type.SELL;
                    DataPoint dp = DataPoint.newBuilder()
                            .setMarket(market)
                            .setTimestamp(time)
                            .setPrice(price)
                            .setAmount(amount)
                            .setType(type)
                            .build();
                    BitFinexDatasource.this.addData(dp);
                }
            } catch (NumberFormatException | NullPointerException | JsonSyntaxException e) {
                logger.log(Level.WARNING, "Unknown message type: " + message);
            }
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            // fetch all possible markets using rest call to BitFinex' Rest API
            logger.info("Fetching markets");
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.bitfinex.com/v1/symbols",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<String>() {
                    });
            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.log(Level.SEVERE, "Unable to reach markets");
                return;
            }
            // subscribe to trade stream for each market
            List<String> markets = gson.fromJson(response.getBody(), new TypeToken<List<String>>() {
            }.getType());
            markets.forEach(i -> {
                ws.send("{\"event\": \"subscribe\",\"channel\":\"trades\",\"pair\": \"" + i + "\"}");
            });
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            logger.log(Level.WARNING, "Connection closed: " + code + "," + reason + "," + remote);
        }

        @Override
        public void onError(Exception ex) {
            logger.log(Level.WARNING, "Socket exception", ex);
        }
    }
}
