package application;

import com.google.gson.Gson;
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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * {@link SpringBootApplication} implementing parts of the Binance API.
 *
 * @see <a href="https://github.com/binance-exchange/binance-official-api-docs">https://github.com/binance-exchange/binance-official-api-docs</a>
 */
@SpringBootApplication
public class BinanceDatasource extends Datasource {

    private static final Logger logger = Logger.getLogger(BinanceDatasource.class.getName());
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        // start spring application
        SpringApplication.run(BinanceDatasource.class, args);
        // create datasource and grpc server
        Datasource ds = new BinanceDatasource();
        DataServer s = new DataServer(ds, 50051);
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
        // fetch all possible markets using rest call to Binance's Rest API
        logger.info("Fetching markets");
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.binance.com/api/v1/exchangeInfo",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<String>() {
                });
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.log(Level.SEVERE, "Unable to reach markets");
            return;
        }

        // creating url for websocket
        String url = "wss://stream.binance.com:9443/stream?streams=";
        String markets = Arrays.stream(gson.fromJson(response.getBody(), ExchangeInfo.class)
                .symbols).map(s -> s.symbol.toLowerCase() + "@trade")
                .collect(Collectors.joining("/"));
        URI serverURI = URI.create(url + markets);

        // create connection and connect using the BinanceWebSocketImpl to handle messages
        SimpleWebSocket ws = new SimpleWebSocket(serverURI, new BinanceWebSocketImpl());
        try {
            ws.setSocket(SSLSocketFactory
                    .getDefault()
                    .createSocket(serverURI.getHost(), serverURI.getPort())
            );
            ws.connect();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error creating websocket", e);
        }

    }

    /**
     * The exchange info object as defined by the binance API.
     */
    private class ExchangeInfo {
        Symbol[] symbols;

        private class Symbol {
            String symbol;
        }
    }

    /**
     * A stream event object as defined by the binance API.
     */
    private class StreamEvent {
        Trade data;

        private class Trade {
            String s; // Symbol
            Double p; // Price
            Double q; // Quantity
            long T; // Trade time
            boolean m; // Is the buyer the market maker?

            public DataPoint toDataPoint() {
                return DataPoint.newBuilder()
                        .setType(m ? DataPoint.Type.BUY : DataPoint.Type.SELL)
                        .setAmount(q)
                        .setPrice(p)
                        .setTimestamp(T)
                        .setMarket("binance-" + s)
                        .build();
            }
        }
    }

    /**
     * An implementation of a {@link SimpleWebSocketInterface} for handling messages
     * from the Binance API.
     */
    private class BinanceWebSocketImpl implements SimpleWebSocketInterface {
        @Override
        public void onMessage(String message) {
            // parse message and add DataPoint to internal data store
            StreamEvent e = gson.fromJson(message, StreamEvent.class);
            BinanceDatasource.this.addData(e.data.toDataPoint());
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            logger.info("Connected");
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
