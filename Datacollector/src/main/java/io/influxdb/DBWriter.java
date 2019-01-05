package io.influxdb;

import io.grpc.collector.DataPoint;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class DBWriter {

    private static final Logger logger = Logger.getLogger(DBWriter.class.getName());
    private InfluxDB influxDB;

    public DBWriter(String url, String user, String pass) {
        influxDB = InfluxDBFactory.connect(url, user, pass);
        if (!influxDB.databaseExists("prod")) {
            logger.info("Creating database \"prod\"...");
            influxDB.createDatabase("prod");
        }
    }

    public void write(List<DataPoint> dataPoints) {
        BatchPoints bp = BatchPoints.database("prod").build();

        dataPoints.stream().map(dp -> Point.measurement("trades")
                .time(dp.getTimestamp(), TimeUnit.MILLISECONDS)
                .addField("price", dp.getPrice())
                .addField("volume", dp.getAmount())
                .addField("market_buy", dp.getType() == DataPoint.Type.BUY)
                .build())
                .forEach(bp::point);

        influxDB.write(bp);

    }
}

