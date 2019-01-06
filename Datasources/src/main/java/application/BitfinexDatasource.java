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

@SpringBootApplication
public class BitfinexDatasource extends Datasource {

    private static final Logger logger = Logger.getLogger(BitfinexDatasource.class.getName());
    private static Gson gson = new Gson();
    private static SimpleWebSocket ws;

    public static void main(String[] args) {
        // start spring application
        SpringApplication.run(BitfinexDatasource.class, args);
        // create datasource and grpc server
        Datasource ds = new BitfinexDatasource();
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
        // endpoint
        String url = "wss://api.bitfinex.com/ws/";
        URI serverURI = URI.create(url);
        // create connection and connect
        ws = new SimpleWebSocket(serverURI, new BitfinexWebSocketInterface());
        try {
            ws.setSocket(SSLSocketFactory.getDefault().createSocket());
            ws.connect();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error creating websocket", e);
        }
    }

    private class BitfinexWebSocketInterface implements SimpleWebSocketInterface {

        @Override
        public void onMessage(String message) {
            try {
                List<String> l = gson.fromJson(message, new TypeToken<List<String>>() {
                }.getType());
                if (l.size() == 6 && l.get(1).equals("te")) {
                    String market = "bitfinex-" + l.get(2).split("-")[1];
                    Long time = Long.parseLong(l.get(3));
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
                    BitfinexDatasource.this.addData(dp);
                }
            } catch (NumberFormatException | NullPointerException | JsonSyntaxException e) {
                logger.log(Level.WARNING, "Unknown message type: " + message);
            }
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
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
            List<String> markets = gson.fromJson(response.getBody(), new TypeToken<List<String>>() {
            }.getType());
            System.out.println(markets.size());
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
