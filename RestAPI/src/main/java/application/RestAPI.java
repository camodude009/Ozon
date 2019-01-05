package application;


import com.google.gson.Gson;
import io.influxdb.DBReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping("/service-instances/{applicationName}")
    public String serviceInstancesByApplicationName(
            @PathVariable String applicationName) {
        List<String> serviceURIs = this.discoveryClient.getInstances(applicationName).stream()
                .map(si -> si.getUri().toString())
                .collect(Collectors.toList());
        return gson.toJson(serviceURIs);
    }
}
