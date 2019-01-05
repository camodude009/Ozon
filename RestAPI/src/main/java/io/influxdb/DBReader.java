package io.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import java.util.logging.Logger;

public class DBReader {

    private static final Logger logger = Logger.getLogger(DBReader.class.getName());
    private InfluxDB influxDB;

    public DBReader(String url, String user, String pass) {
        influxDB = InfluxDBFactory.connect(url, user, pass);
        if (!influxDB.databaseExists("prod")) {
            logger.info("Creating database \"prod\"...");
            influxDB.createDatabase("prod");
        }
    }

}

