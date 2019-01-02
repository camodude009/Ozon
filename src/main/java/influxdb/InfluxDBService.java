package influxdb;

import io.grpc.DataClient;
import io.grpc.collector.DataPoint;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InfluxDBService {

    private static final Logger logger = Logger.getLogger(DataClient.class.getName());
    private static final long SLEEP_DURATION = 10 * 1000;

    /**
     * Greet RestletServer. If provided, the first element of {@code args} is the name to use in the
     * greeting.
     */
    public static void main(String[] args) {
        while (true) {
            logger.info("Sleeping " + SLEEP_DURATION / 1000 + " seconds...");
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Interrupted while waiting for next cycle", e);
            }


            //TODO add EUREKA call
            List<String> dataCollectors = new LinkedList<>();
            dataCollectors.add("localhost"); //for debug only, remove after adding eureka

            for (String ip : dataCollectors) {
                logger.info("Fetching data from " + ip + ":50051");
                DataClient c = new DataClient(ip, 50051);
                handleData(c.fetchData());
                try {
                    c.shutdown();
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Interrupted while closing connection to " + ip + ":50051", e);
                }
            }
        }
    }

    private static void handleData(List<DataPoint> dataPoints) {
        dataPoints.forEach(dp -> logger.info(dp.toString()));
        logger.info("Received " + dataPoints.size() + " data points");
        // TODO add INFLUX database interaction
    }
}
