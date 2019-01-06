package io.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    public Summary summary(String market, Optional<Long> from, Optional<Long> to) {
        List<TradePoint> tpList = raw(market, from, to);

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

    public List<TradePoint> raw(String market, Optional<Long> from, Optional<Long> to) {
        String queryString = "select * from trades where market = \'" + market + "\'" +
                (from.isPresent() ? (" and time >= " + from.get() * 1000000) : "") +
                (to.isPresent() ? (" and time <= " + to.get() * 1000000) : "");
        QueryResult r = influxDB.query(new Query(queryString, "prod"));
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

        return resultMapper.toPOJO(r, TradePoint.class);
    }

    public List<String> markets() {
        String queryString = "show tag values from trades with key = market";
        QueryResult r = influxDB.query(new Query(queryString, "prod"));
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        return resultMapper.toPOJO(r, TagPoint.class).stream()
                .map(TagPoint::getValue)
                .collect(Collectors.toList());
    }
}

