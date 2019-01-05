package application;

import com.google.gson.Gson;
import io.grpc.DataServer;
import io.grpc.collector.DataPoint;
import io.websockets.SimpleWebSocket;
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

@SpringBootApplication
public class BinanceDatasource extends Datasource {
    private static final Logger logger = Logger.getLogger(BinanceDatasource.class.getName());
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        // start spring application
        SpringApplication.run(BinanceDatasource.class, args);
        // create datasource and grpc server
        Datasource ds = new BinanceDatasource();
        DataServer s = new DataServer(ds);
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

        // creating stream url
        String url = "wss://stream.binance.com:9443/stream?streams=";
        String markets = Arrays.stream(gson.fromJson(response.getBody(), ExchangeInfo.class)
                .symbols).map(s -> s.symbol.toLowerCase() + "@trade")
                .collect(Collectors.joining("/"));
        URI serverURI = URI.create(url + markets);

        // message handler
        SimpleWebSocket ws = new SimpleWebSocket(serverURI, message -> {
            StreamEvent e = gson.fromJson(message, StreamEvent.class);
            this.addData(e.data.toDataPoint());
        });
        // create connection and connect
        try {
            ws.setSocket(SSLSocketFactory
                    .getDefault()
                    .createSocket(serverURI.getHost(), serverURI.getPort())
            );
            ws.connect();
        } catch (
                IOException e) {
            logger.log(Level.WARNING, "Error creating websocket", e);
        }

    }

    // a stream event object as defined by the binance API
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

    private class ExchangeInfo {
        Symbol[] symbols;

        private class Symbol {
            String symbol;
        }
    }
}
