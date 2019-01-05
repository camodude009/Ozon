package application;


import io.eureka.DatasourceDiscoveryClient;
import io.grpc.DataClient;
import io.grpc.collector.DataPoint;
import io.influxdb.DBConnector;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Collector {

    private static final Logger logger = Logger.getLogger(Collector.class.getName());
    private static final long SLEEP_DURATION = 10 * 1000;

    public static void main(String[] args) {
        // start eureka application
        SpringApplication.run(DatasourceDiscoveryClient.class, args);
        // establish database connection
        DBConnector db = new DBConnector("http://127.0.0.1:8086", "root", "root");
        while (true) {
            logger.info("Sleeping " + SLEEP_DURATION / 1000 + " seconds...");
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Interrupted while waiting for next cycle", e);
            }

            // iterate through registered datasources
            List<String> datasources = new LinkedList<>();
            try {
                datasources = DatasourceDiscoveryClient.getDatasources();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Unable to fetch datasources", e);
            }
            for (String uri : datasources) {
                String host = URI.create(uri).getHost();
                logger.info("Fetching data from " + host + ":50051");
                DataClient c = new DataClient(host, 50051);
                List<DataPoint> newData = c.fetchData();
                logger.info("Received " + newData.size() + " data points");
                db.handleData(newData);
                try {
                    c.shutdown();
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Interrupted connection to " + host + ":50051", e);
                }
            }

        }
    }
}
