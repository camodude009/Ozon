package io.influxdb;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

@Measurement(name = "trades")
public class TradePoint {
    @Column(name = "time")
    private Instant time;
    @Column(name = "price")
    private Double price;
    @Column(name = "volume")
    private Double volume;
    @Column(name = "market_buy")
    private Boolean market_buy;

    @Override
    public String toString() {
        return "TradePoint{" +
                "time=" + time +
                ", price=" + price +
                ", volume=" + volume +
                ", market_buy=" + market_buy +
                '}';
    }
}