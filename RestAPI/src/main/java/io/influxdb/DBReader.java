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

/**
 * Provides abstracted read capabilities for querying data from InfluxDB.
 */
public class DBReader {

    private static final Logger logger = Logger.getLogger(DBReader.class.getName());
    private InfluxDB influxDB;

    /**
     * Creates a new DBReader.
     *
     * @param url  url of the InfluxDB database (including port)
     * @param user username (not relevant for current configuration)
     * @param pass password (not relevant for current configuration)
     */
    public DBReader(String url, String user, String pass) {
        // create DB connection
        influxDB = InfluxDBFactory.connect(url, user, pass);
        // create DB needed during reads if it does not already exist
        if (!influxDB.databaseExists("prod")) {
            logger.info("Creating database \"prod\"...");
            influxDB.createDatabase("prod");
        }
    }

    /**
     * Queries the the database for a summary of a given market.
     *
     * @param market market to be queried
     * @param from   timestamp (ms)
     * @param to     timestamp (ms)
     * @return query result
     */
    public SummaryPoint summary(String market, Optional<Long> from, Optional<Long> to) {
        // construct query string
        String queryString = "select count(market_buy), max(price), min(price), sum(volume) " +
                "from trades " +
                "where market = \'" + market + "\'" +
                (from.isPresent() ? (" and time >= " + from.get() * 1000000) : "") +
                (to.isPresent() ? (" and time <= " + to.get() * 1000000) : "");
        // query DB
        QueryResult r = influxDB.query(new Query(queryString, "prod"));
        // map and return result
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        return resultMapper.toPOJO(r, SummaryPoint.class).get(0);
    }

    /**
     * Queries the the database for raw data of a given market.
     *
     * @param market market to be queried
     * @param from   timestamp (ms)
     * @param to     timestamp (ms)
     * @return query result
     */
    public List<TradePoint> raw(String market, Optional<Long> from, Optional<Long> to) {
        // construct query string
        String queryString = "select * from trades where market = \'" + market + "\'" +
                (from.isPresent() ? (" and time >= " + from.get() * 1000000) : "") +
                (to.isPresent() ? (" and time <= " + to.get() * 1000000) : "");
        // query DB
        QueryResult r = influxDB.query(new Query(queryString, "prod"));
        // map and return result
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        return resultMapper.toPOJO(r, TradePoint.class);
    }

    /**
     * Queries the the database for unique market names.
     *
     * @return query result
     */
    public List<String> markets() {
        // construct query string
        String queryString = "show tag values from trades with key = market";
        // query DB
        QueryResult r = influxDB.query(new Query(queryString, "prod"));
        // map and return result
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        return resultMapper.toPOJO(r, TagPoint.class).stream()
                .map(TagPoint::getValue)
                .collect(Collectors.toList());
    }
}

