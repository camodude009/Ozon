# Ozon

### Building services:
1) Download and install [InfluxDB](https://docs.influxdata.com/influxdb/v1.4/introduction/installation/ "InfluxDB installation instructions")
2) Start InfluxDB as a background service (shown here for Linux)
    ```
    $ sudo service influxdb start
    ```
3) Clone the repository
    ```
    $ git clone https://github.com/camodude009/Ozon.git
    ```
4) Switch to the directory and build the project
    ```
    $ cd Ozon
    $ ./gradlew build
    $ ./gradlew bootJar
    ```
    This creates the Spring-Boot applications `eureka-service-1.0.0.jar`,
    `restapi-service-1.0.0.jar`, `datacollector-service-1.0.0.jar`,
    `binance-datasource-service-1.0.0.jar`, `bitfinex-datasource-service-1.0.0.jar`
     in their respective modules.

These services can simply be launched using: 
```
$ java -jar ./<module-name>/build/libs/<service-name>-1.0.0.jar 
```
Beware, that the Eureka-Service (`eureka-service-1.0.0.jar`) needs to be launched before any other service.

That's it!
Now you can start all services and visit the provided sample page `frontend.html` to see the system in action.