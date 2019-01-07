package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * {@link SpringBootApplication} that is the EUREKA Server.
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaService {
    public static void main(String[] args) {
        // start spring application
        SpringApplication.run(EurekaService.class, args);
    }
}