import { isUndefined } from 'util';

export class Metric {
    name: string;
    type: string;
    cat: string;
    label: string;
    // additional dynamic properties
}

export const nameLabelMap = [
    { name: 'JSoupHtmlParser.data.parse', label: 'Parse document' },
    { name: 'JSoupHtmlParser.data.reuse', label: 'Reuse document data' },
    { name: 'LocatorSeeder.locator.parsed', label: 'Parsed from links' },
    { name: 'LocatorSeeder.locator.provided', label: 'Provided by user' },
    { name: 'LocatorSeeder.locator.seeded', label: 'Pushed to queue' },
    { name: 'DataAppender.task', label: 'Data appender' },
    { name: 'DataConverter.task', label: 'Data converter' },
    { name: 'DataFilter.task', label: 'Data filter' },
    { name: 'JSoupHtmlParser.task', label: 'Parser' },
    { name: 'LocatorCreator.task', label: 'Locator creator' },
    { name: 'LocatorSeeder.task', label: 'Locator seeder' },
    { name: 'URLLoader.task', label: 'Document fetch and load' },
    { name: 'uptime', label: 'up time' },
    { name: 'systemLoad', label: 'Load average' },
    { name: 'totalMemory', label: 'Total memory' },
    { name: 'maxMemory', label: 'Max Memory' },
    { name: 'freeMemory', label: 'Free Memory' },
    { name: 'AppenderPoolService.pool.appender', label: 'Appender AppenderPool' },
    { name: 'TaskPoolService.pool.appender', label: 'Appender TaskPool' },
    { name: 'TaskPoolService.pool.converter', label: 'Converter TaskPool' },
    { name: 'TaskPoolService.pool.loader', label: 'Loader TaskPool' },
    { name: 'TaskPoolService.pool.parser', label: 'Parser TaskPool' },
    { name: 'TaskPoolService.pool.process', label: 'Process TaskPool' },
    { name: 'TaskPoolService.pool.seeder', label: 'Seeder TaskPool' },
];

export class MetricDataConverter {

    // convert metrics to metric data model
    convertMetrics(inMetrics: any): Array<Metric> {
        const metrics = Array<Metric>();
        Array.prototype.push.apply(metrics, this.convert('counter', inMetrics.counters));
        Array.prototype.push.apply(metrics, this.convert('meter', inMetrics.meters));
        Array.prototype.push.apply(metrics, this.convert('timer', inMetrics.timers));
        Array.prototype.push.apply(metrics, this.convert('histogram', inMetrics.histograms));
        Array.prototype.push.apply(metrics, this.convertGauges(inMetrics.gauges));
        return metrics;
    }

    convert(type: string, inMetrics: Array<any>): Array<Metric> {
        const metrics = Array<Metric>();
        Object.entries(inMetrics).forEach(inMetric => {
            const metricName = inMetric[0];
            const [clz, metricCat, ...others] = metricName.split('.');
            // create metric
            const metric: Metric = {
                name: metricName,
                type: type,
                cat: metricCat,
                label: this.getLabel(metricName),
            };
            // add metric data as dynamic properties
            Object.entries(inMetric[1]).forEach(item => {
                metric[item[0]] = item[1];
            });
            metrics.push(metric);
        });
        return metrics;
    }

    /* metric gauge structure is different from others, so separate
       method to convert them */
    convertGauges(inMetrics: Array<any>): Array<Metric> {
        const metrics = Array<Metric>();

        Object.entries(inMetrics).forEach(inMetric => {
            const metricName = inMetric[0];
            const [clz, metricCat, ...others] = metricName.split('.');
            Object.entries(inMetric[1]).forEach(value => {
                const items = value[1];
                // create metric
                const metric: Metric = {
                    name: metricName,
                    type: 'gauge',
                    cat: metricCat,
                    label: this.getLabel(metricName),
                };
                // create and add data
                Object.entries(items).forEach(item => {
                    metric[item[0]] = item[1];
                });
                metrics.push(metric);
            });
        });
        return metrics;
    }

    getLabel(name: string): string {
        const item = nameLabelMap.find(ii => {
            if (ii.name === name) {
                return true;
            }
        });
        if (isUndefined(item)) {
            return name;
        } else {
            return item.label;
        }
    }
}
