package io.influxdb;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name = "trades")
public class TagPoint {
    @Column(name = "key")
    private String key;
    @Column(name = "value")
    private String value;

    public String getValue() {
        return value;
    }
}
