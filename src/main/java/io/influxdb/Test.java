package io.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) {
        InfluxDB influxDB = InfluxDBFactory.connect("http://127.0.0.1:8086", "user", "");
        influxDB.deleteDatabase("test");
        influxDB.createDatabase("test");
        BatchPoints bp = BatchPoints.database("test").build();

        Point p = Point.measurement("trades")
                .time(1000, TimeUnit.MILLISECONDS)
                .addField("price", 100.1)
                .addField("volume", 1000.1)
                .addField("market_buy", false)
                .build();

        bp.point(p);

        influxDB.write(bp);
        QueryResult r = influxDB.query(new Query("select * from trades", "test"));

        System.out.println(r.getResults().get(0).getSeries().get(0).getValues().get(0).get(1).toString());

        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

        List<TradePoint> tpList = resultMapper.toPOJO(r, TradePoint.class);

        tpList.forEach(System.out::println);
    }
}

