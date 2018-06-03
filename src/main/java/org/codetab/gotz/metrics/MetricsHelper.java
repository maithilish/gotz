package org.codetab.gotz.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

public class MetricsHelper {

    static final MetricRegistry METRICS =
            SharedMetricRegistries.getOrCreate("gotz");

    public Context getTimer(final Object clz, final String... names) {
        Timer timer = METRICS.timer(getName(clz, names));
        return timer.time();
    }

    public Meter getMeter(final Object clz, final String... names) {
        return METRICS.meter(getName(clz, names));
    }

    public Counter getCounter(final Object clz, final String... names) {
        return METRICS.counter(getName(clz, names));
    }

    public <T> void registerGuage(final T value, final Object clz,
            final String... names) {
        METRICS.register(getName(clz, names), new Gauge<T>() {
            @Override
            public T getValue() {
                return value;
            }
        });
    }

    public void clearGuages() {
        for (String key : METRICS.getGauges().keySet()) {
            METRICS.remove(key);
        }
    }

    private String getName(final Object clz, final String... names) {
        return name(clz.getClass().getSimpleName(), names);
    }

}
