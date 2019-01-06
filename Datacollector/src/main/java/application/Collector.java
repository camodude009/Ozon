package application;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.grpc.DataClient;
import io.grpc.collector.DataPoint;
import io.influxdb.DBWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableDiscoveryClient
public class Collector {

    private static final Logger logger = Logger.getLogger(Collector.class.getName());
    private static final long SLEEP_DURATION = 10 * 1000;
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        // start spring application
        SpringApplication.run(Collector.class, args);
        // establish database connection
        DBWriter db = new DBWriter("http://127.0.0.1:8086", "root", "root");
        while (true) {
            // iterate through registered datasources
            List<String> datasources = Collector.getDatasources();
            for (String uri : datasources) {
                db.write(fetchData(URI.create(uri)));
            }
            // sleep
            logger.info("Sleeping " + SLEEP_DURATION / 1000 + " seconds...");
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Interrupted while waiting for next cycle", e);
            }

        }
    }

    private static List<DataPoint> fetchData(URI uri) {
        String host = uri.getHost();
        int serviceNumber = uri.getPort() - 8081;
        int port = 50050 + serviceNumber;
        logger.info("Fetching data from " + host + "(SN: " + serviceNumber + "):" + port);
        DataClient c = new DataClient(host, port);
        List<DataPoint> newData = c.fetchData();
        logger.info("Received " + newData.size() + " data points");
        try {
            c.shutdown();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrupted connection to " + host + ":" + port, e);
        }
        return newData;
    }

    private static List<String> getDatasources() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8081/service-instances/datasource/",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<String>() {
                });
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.log(Level.WARNING, "Unable to fetchData datasources");
            return new LinkedList<>();
        }
        return gson.fromJson(response.getBody(), new TypeToken<List<String>>() {
        }.getType());
    }
}

@RestController
class ServiceInstanceRestController {
    private static Gson gson = new Gson();

    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping(
            value = "/service-instances/{applicationName}",
            produces = "application/json"
    )
    public String serviceInstancesByApplicationName(
            @PathVariable String applicationName) {
        List<String> serviceURIs = this.discoveryClient.getInstances(applicationName).stream()
                .map(si -> si.getUri().toString())
                .collect(Collectors.toList());
        return gson.toJson(serviceURIs);
    }

}
