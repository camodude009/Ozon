package io.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;

import java.util.List;
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

    public Summary summary(String market, Long from, Long to) {
        String queryString =
                "select * from trades where time >= "
                        + from + " and time <= " + to +
                        " and market = " + market;
        QueryResult r = influxDB.query(new Query(queryString, "prod"));
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

        List<TradePoint> tpList = resultMapper.toPOJO(r, TradePoint.class);

        double max = tpList.stream()
                .map(TradePoint::getPrice)
                .reduce(Double::max)
                .orElse(-1.0);
        double min = tpList.stream()
                .map(TradePoint::getPrice)
                .reduce(Double::min)
                .orElse(-1.0);
        double volume = tpList.stream()
                .map(TradePoint::getVolume)
                .reduce(Double::sum)
                .orElse(0.0);
        return new Summary(max, min, volume, tpList.size());
    }
}
