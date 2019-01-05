package application;

import com.google.gson.Gson;
import io.grpc.DataServer;
import io.grpc.collector.DataPoint;
import io.websockets.SimpleWebSocket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class BinanceDatasource extends Datasource {
    private static final Logger logger = Logger.getLogger(BinanceDatasource.class.getName());
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        SpringApplication.run(BinanceDatasource.class, args);
        Datasource ds = new BinanceDatasource();
        DataServer s = new DataServer(ds);
        try {
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
        String url = "wss://stream.binance.com:9443/ws/btcusdt@trade";
        URI serverURI = URI.create(url);
        SimpleWebSocket ws = new SimpleWebSocket(serverURI, message -> {
            Trade t = gson.fromJson(message, Trade.class);
            this.addData(t.toDataPoint());
        });
        try {
            ws.setSocket(SSLSocketFactory.getDefault().createSocket(serverURI.getHost(), serverURI.getPort()));
            ws.connect();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error creating websocket", e);
        }

    }

    private class Trade {
        String e; // Event type
        long E; // Event time
        String s; // Symbol
        int t; // Trade ID
        String p; // Price
        String q; // Quantity
        String b; // Buyer order ID
        String a; // Seller order ID
        long T; // Trade time
        boolean m; // Is the buyer the market maker?
        String M; // Ignore

        public DataPoint toDataPoint() {
            return DataPoint.newBuilder()
                    .setType(m ? DataPoint.Type.BUY : DataPoint.Type.SELL)
                    .setAmount(q)
                    .setPrice(p)
                    .setTimestamp(T)
                    .build();
        }
    }
}
