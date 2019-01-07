package io.influxdb;

import io.grpc.collector.DataPoint;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Provides abstracted write capabilities for writing gRPC @{@link DataPoint}s to InfluxDB.
 */
public class DBWriter {

    private static final Logger logger = Logger.getLogger(DBWriter.class.getName());
    private InfluxDB influxDB;

    /**
     * Creates a new DBWriter.
     *
     * @param url  url of the InfluxDB database (including port)
     * @param user username (not relevant for current configuration)
     * @param pass password (not relevant for current configuration)
     */
    public DBWriter(String url, String user, String pass) {
        // create DB connection
        influxDB = InfluxDBFactory.connect(url, user, pass);
        // create DB needed during writes if it does not already exist
        if (!influxDB.databaseExists("prod")) {
            logger.info("Creating database \"prod\"...");
            influxDB.createDatabase("prod");
        }
    }

    /**
     * Writes @{@link DataPoint}s to the InfluxDB database.
     * Most efficient when writing large amounts at once.
     *
     * @param dataPoints list of points to write
     */
    public void write(List<DataPoint> dataPoints) {
        // create and fill object used for collective writes
        BatchPoints bp = BatchPoints.database("prod").build();
        dataPoints.stream().map(dp -> Point.measurement("trades")
                .time(dp.getTimestamp(), TimeUnit.MILLISECONDS)
                .addField("price", dp.getPrice())
                .addField("volume", dp.getAmount())
                .addField("market_buy", dp.getType() == DataPoint.Type.BUY)
                .tag("market", dp.getMarket())
                .build())
                .forEach(bp::point);
        // DB write call
        influxDB.write(bp);

    }
}

