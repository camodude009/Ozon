package io.influxdb;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

/**
 * Mapping of {@link DBReader}'s 'summary' query result.
 */
@Measurement(name = "trades")
public class SummaryPoint {

    @Column(name = "count")
    private int count;
    @Column(name = "max")
    private double max;
    @Column(name = "min")
    private double min;
    @Column(name = "sum")
    private double volume;

}
