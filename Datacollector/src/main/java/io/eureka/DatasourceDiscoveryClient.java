package io.eureka;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableDiscoveryClient
public class DatasourceDiscoveryClient {

    public static Gson gson = new Gson();

    public static List<String> getDatasources() throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:8081/service-instances/datasource/",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<String>() {
                });
        if(!response.getStatusCode().is2xxSuccessful()){
            throw new IOException(response.getStatusCode().toString());
        }
        return gson.fromJson(response.getBody(), new TypeToken<List<String>>() {
        }.getType());
    }
}

@RestController
class ServiceInstanceRestController {

    public static Gson gson = new Gson();

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

