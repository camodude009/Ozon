# Ozon


Requirements:

1) Install InfluxDB - instructions at: https://docs.influxdata.com/influxdb/v1.4/introduction/installation/


Starting Services:

1) Start InfluxDB.
2) Run EurekaService.
3) Run all the other services (RestAPI, Collector, BinanceDatasource, BitFinexDatasource) in any order.
4) Open frontend.html to easily query the rest api.

The port configuration in the Datasources/src/main/resources/application.yml needs to be changed before launching BinanceDatasource (port=8082) and BitFinexDatasource (port=8083) respectively.