package io.influxdb;

public class Summary {
    double max;
    double min;
    double volume;
    int count;

    public Summary(double max, double min, double volume, int count) {
        this.max = max;
        this.min = min;
        this.volume = volume;
        this.count = count;
    }

    @Override
    public String toString() {
        return "Summary{" +
                "max=" + max +
                ", min=" + min +
                ", volume=" + volume +
                ", count=" + count +
                '}';
    }
}
