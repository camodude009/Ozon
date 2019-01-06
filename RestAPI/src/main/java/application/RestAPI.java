package application;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.influxdb.DBReader;
import io.influxdb.SummaryPoint;
import io.influxdb.TradePoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableDiscoveryClient
public class RestAPI {

    private static final Logger logger = Logger.getLogger(RestAPI.class.getName());
    private static final long SLEEP_DURATION = 10 * 1000;
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        // start spring application
        SpringApplication.run(RestAPI.class, args);
    }
}

@RestController
class ServiceInstanceRestController {
    private static Gson gson = new Gson();
    // database connection
    private DBReader db = new DBReader("http://127.0.0.1:8086", "root", "root");

    // summary request with optional 'from' and 'to' parameters
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
        SummaryPoint result = db.summary(market, from, to);
        return gson.toJson(result, SummaryPoint.class);
    }

    // summary request with optional 'from' and 'to' parameters
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
        List<Trade> result = db.raw(market, from, to).stream()
                .map(Trade::new)
                .collect(Collectors.toList());
        return gson.toJson(result, new TypeToken<List<Trade>>() {
        }.getType());
    }

    // market request
    @CrossOrigin
    @RequestMapping(
            value = "/markets",
            method = RequestMethod.GET,
            produces = "application/json"
    )
    public String markets() {
        List<String> result = db.markets();
        return gson.toJson(result, new TypeToken<List<Trade>>() {
        }.getType());
    }

    private class Trade {
        double price, volume;
        boolean market_buy;
        String market;
        long time;

        public Trade(TradePoint p) {
            price = p.getPrice();
            volume = p.getVolume();
            market = p.getMarket();
            market_buy = p.getMarket_buy();
            time = p.getTime().toEpochMilli();
        }
    }

}
