package application;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.influxdb.DBReader;
import io.influxdb.Summary;
import io.influxdb.TradePoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

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
    @RequestMapping(
            value = "/summary/{market}",
            method = RequestMethod.GET,
            produces = "application/json"
    )
    public String summary(@PathVariable("market") String market,
                          @RequestParam(name = "from", required = false) Optional<Long> from,
                          @RequestParam(name = "to", required = false) Optional<Long> to
    ) {
        long f = from.orElse(0l) * 1000000;
        long t = to.isPresent() ? to.get() * 1000000 : Long.MAX_VALUE;
        Summary result = db.summary(market, f, t);
        return gson.toJson(result, Summary.class);
    }

    // summary request with optional 'from' and 'to' parameters
    @RequestMapping(
            value = "/raw/{market}",
            method = RequestMethod.GET,
            produces = "application/json"
    )
    public String raw(@PathVariable("market") String market,
                      @RequestParam(name = "from", required = false) Optional<Long> from,
                      @RequestParam(name = "to", required = false) Optional<Long> to
    ) {
        long f = from.orElse(0l) * 1000000;
        long t = to.isPresent() ? to.get() * 1000000 : Long.MAX_VALUE;
        List<TradePoint> result = db.raw(market, f, t);
        return gson.toJson(result, new TypeToken<List<TradePoint>>() {
        }.getType());
    }

}
