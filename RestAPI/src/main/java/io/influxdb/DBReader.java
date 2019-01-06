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

    public SummaryPoint summary(String market, Optional<Long> from, Optional<Long> to) {
        String queryString = "select count(market_buy), max(price), min(price), sum(volume) " +
                "from trades " +
                "where market = \'" + market + "\'" +
                (from.isPresent() ? (" and time >= " + from.get() * 1000000) : "") +
                (to.isPresent() ? (" and time <= " + to.get() * 1000000) : "");
        QueryResult r = influxDB.query(new Query(queryString, "prod"));
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

        return resultMapper.toPOJO(r, SummaryPoint.class).get(0);
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

