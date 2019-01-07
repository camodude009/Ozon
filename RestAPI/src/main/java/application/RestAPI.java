package application;


import com.google.gson.Gson;
import io.influxdb.DBReader;
import io.influxdb.SummaryPoint;
import io.influxdb.TradePoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A {@link SpringBootApplication} that exposes a Rest API for querying InfluxDB.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class RestAPI {
    public static void main(String[] args) {
        // start spring application
        SpringApplication.run(RestAPI.class, args);
    }
}

/**
 * Exposed rest service for querying InfluxDB.
 */
@RestController
class ServiceInstanceRestController {

    private static Gson gson = new Gson();

    /**
     * Database connection.
     */
    private DBReader db = new DBReader("http://127.0.0.1:8086", "root", "root");

    /**
     * Request for a summary of a certain market (and time period).
     *
     * @param market market to be queried
     * @param from   timestamp (ms) - optional
     * @param to     timestamp (ms) - optional
     * @return JSONObject mapping of a {@link SummaryPoint}
     */
    @CrossOrigin
    @RequestMapping(
            value = "/summary/{market}",
            method = RequestMethod.GET,
            produces = "application/json"
    )
    public String summary(@PathVariable("market") String market,
                          @RequestParam(name = "from", required = false) Optional<Long> from,
                          @RequestParam(name = "to", required = false) Optional<Long> to
    ) {
        // query DB and return mapped result
        SummaryPoint result = db.summary(market, from, to);
        return gson.toJson(result);
    }

    /**
     * Request for a raw data of a certain market (and time period).
     *
     * @param market market to be queried
     * @param from   timestamp (ms) - optional
     * @param to     timestamp (ms) - optional
     * @return JSONArray of mapping of {@link Trade}
     */
    @CrossOrigin
    @RequestMapping(
            value = "/raw/{market}",
            method = RequestMethod.GET,
            produces = "application/json"
    )
    public String raw(@PathVariable("market") String market,
                      @RequestParam(name = "from", required = false) Optional<Long> from,
                      @RequestParam(name = "to", required = false) Optional<Long> to
    ) {
        // query database and return mapped result
        List<TradePoint> result = db.raw(market, from, to);
        return gson.toJson(result.stream()
                .map(TradePoint::toTrade)
                .collect(Collectors.toList())
        );
    }

    /**
     * Request for list of available markets.
     *
     * @return JSONArray of market names
     */
    @CrossOrigin
    @RequestMapping(
            value = "/markets",
            method = RequestMethod.GET,
            produces = "application/json"
    )
    public String markets() {
        List<String> result = db.markets();
        return gson.toJson(result);
    }

}
