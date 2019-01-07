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

/**
 * A {@link SpringBootApplication} for collecting data from datasources and storing it in an InfluxDB database.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class Collector {

    /**
     * The duration between rounds of data collections in ms.
     */
    private static final long SLEEP_DURATION = 10 * 1000;

    private static final Logger logger = Logger.getLogger(Collector.class.getName());
    private static Gson gson = new Gson();

    /**
     * Main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        // start spring application
        SpringApplication.run(Collector.class, args);
        // establish database connection
        DBWriter db = new DBWriter("http://127.0.0.1:8086", "root", "root");

        // main loop
        while (true) {
            // fetch datasources registered on EUREKA
            List<URI> datasources = Collector.getDatasources();
            // iterate through registered datasources
            for (URI uri : datasources) {
                db.write(fetchData(uri));
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

    /**
     * Fetches data from datasource using gRPC.
     *
     * @param uri uri of the datasource.
     * @return List of {@link DataPoint}s recorded since last call to datasources
     */
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

    /**
     * Fetches registered datasources through a Rest call to its locally exposed Rest service.
     *
     * @return list of URIs
     */
    private static List<URI> getDatasources() {
        // Rest call
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8081/service-instances/datasource/",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<String>() {
                });
        // return empty list if HTTPGet is unsuccessful
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.log(Level.WARNING, "Unable to fetchData datasources");
            return new LinkedList<>();
        }
        // parse list of strings from response body
        List<String> uriStrings = gson.fromJson(response.getBody(), new TypeToken<List<String>>() {
        }.getType());
        // return the list mapped to URIs
        return uriStrings.stream()
                .map(URI::create)
                .collect(Collectors.toList());
    }
}

/**
 * Exposed rest service for querying the discovery client.
 */
@RestController
class ServiceInstanceRestController {

    private static Gson gson = new Gson();
    /**
     * The automatically instantiated {@link DiscoveryClient}
     */
    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * {@link RequestMapping} for a call to /service-instances/{applicationName}
     *
     * @param applicationName name of service
     * @return JSONArray of the {@link URI}s of instances registered as "applicationName" on EUREKA
     */
    @RequestMapping(
            value = "/service-instances/{applicationName}",
            produces = "application/json"
    )
    public String serviceInstancesByApplicationName(
            @PathVariable String applicationName) {
        // query discovery client and return the URIs of relevant ServiceInstances
        List<String> serviceURIs = this.discoveryClient.getInstances(applicationName).stream()
                .map(si -> si.getUri().toString())
                .collect(Collectors.toList());
        return gson.toJson(serviceURIs);
    }

}
