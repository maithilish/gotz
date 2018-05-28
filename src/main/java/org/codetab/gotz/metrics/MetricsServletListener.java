package org.codetab.gotz.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.servlets.MetricsServlet;

public class MetricsServletListener extends MetricsServlet.ContextListener {

    static final MetricRegistry METRIC_REGISTRY =
            SharedMetricRegistries.getOrCreate("gotz");

    @Override
    protected MetricRegistry getMetricRegistry() {
        return METRIC_REGISTRY;
    }

}
