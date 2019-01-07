package io.influxdb;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

/**
 * Mapping of {@link DBReader}'s 'raw' query result.
 */
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
    @Column(name = "market", tag = true)
    private String market;

    public Trade toTrade() {
        return new Trade(this);
    }

    public class Trade {
        double price, volume;
        boolean market_buy;
        String market;
        long time;

        public Trade(TradePoint p) {
            price = p.price;
            volume = p.volume;
            market = p.market;
            market_buy = p.market_buy;
            time = p.time.toEpochMilli();
        }
    }
}