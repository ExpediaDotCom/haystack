package com.expedia.www.haystack.metrics;

import com.netflix.servo.publish.AsyncMetricObserver;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.CounterToRateMetricTransform;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MetricPoller;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;
import com.netflix.servo.publish.graphite.GraphiteMetricObserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Publishes metrics to InfluxDb on a regular interval. The frequency of publishing is controlled by configuration.
 * Each application that uses this class must call MetricPublishing.start() in its main() method.
 */
@SuppressWarnings("WeakerAccess")
public class MetricPublishing {
    static final String ASYNC_METRIC_OBSERVER_NAME = "haystack";
    static final int POLL_INTERVAL_SECONDS_TO_EXPIRE_TIME_MULTIPLIER = 2000;
    static final int POLL_INTERVAL_SECONDS_TO_HEARTBEAT_MULTIPLIER = 2;
    static final String HOST_NAME_UNKNOWN_HOST_EXCEPTION = "HostName-UnknownHostException";

    private final Factory factory;

    /**
     * Creates a new instance of MetricPublishing; intended to be used by non-unit-test code.
     */
    public MetricPublishing() {
        this(new Factory());
    }

    /**
     * Creates a new instance of MetricPublishing with a user-specified Factory; intended to be used by unit-test code
     * so that the Factory can be mocked.
     *
     * @param factory The factory to use.
     */
    MetricPublishing(Factory factory) {
        this.factory = factory;
    }

    /**
     * Starts the polling that will publish metrics at regular intervals. This start() method should be called by
     * the main() method of the application.
     */
    public void start(GraphiteConfig graphiteConfig) {
        final PollScheduler pollScheduler = PollScheduler.getInstance();
        pollScheduler.start();
        final MetricPoller monitorRegistryMetricPoller = factory.createMonitorRegistryMetricPoller();
        final List<MetricObserver> observers = Collections.singletonList(createGraphiteObserver(graphiteConfig));
        final PollRunnable task = factory.createTask(monitorRegistryMetricPoller, observers);
        pollScheduler.addPoller(task, graphiteConfig.pollIntervalSeconds(), TimeUnit.SECONDS);
    }

    MetricObserver createGraphiteObserver(GraphiteConfig graphiteConfig) {
        final String address = graphiteConfig.address() + ":" + graphiteConfig.port();
        return rateTransform(graphiteConfig,
                async(graphiteConfig, factory.createGraphiteMetricObserver(ASYNC_METRIC_OBSERVER_NAME, address)));
    }

    MetricObserver rateTransform(GraphiteConfig graphiteConfig, MetricObserver observer) {
        final long heartbeat = POLL_INTERVAL_SECONDS_TO_HEARTBEAT_MULTIPLIER * graphiteConfig.pollIntervalSeconds();
        return factory.createCounterToRateMetricTransform(observer, heartbeat, TimeUnit.SECONDS);
    }

    MetricObserver async(GraphiteConfig graphiteConfig, MetricObserver observer) {
        final long expireTime = POLL_INTERVAL_SECONDS_TO_EXPIRE_TIME_MULTIPLIER * graphiteConfig.pollIntervalSeconds();
        final int queueSize = graphiteConfig.queueSize();
        return factory.createAsyncMetricObserver(observer, queueSize, expireTime);
    }

    /**
     * Factory to wrap static or final methods; this Factory facilitates unit testing
     */
    static class Factory {
        Factory() {
            // default constructor
        }

        static String getLocalHostName(Factory factory) {
            try {
                return factory.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                return HOST_NAME_UNKNOWN_HOST_EXCEPTION;
            }
        }

        MetricObserver createAsyncMetricObserver(MetricObserver observer, int queueSize, long expireTime) {
            return new AsyncMetricObserver(ASYNC_METRIC_OBSERVER_NAME, observer, queueSize, expireTime);
        }

        MetricObserver createCounterToRateMetricTransform(
                MetricObserver observer, long heartbeat, TimeUnit timeUnit) {
            return new CounterToRateMetricTransform(observer, heartbeat, timeUnit);
        }

        InetAddress getLocalHost() throws UnknownHostException {
            return InetAddress.getLocalHost();
        }

        MetricObserver createGraphiteMetricObserver(String prefix, String address) {
            final String hostName = Factory.getLocalHostName(this);
            return new GraphiteMetricObserver(prefix, address,
                    new ServoToInfluxDbViaGraphiteNamingConvention(hostName));
        }

        PollRunnable createTask(MetricPoller poller, Collection<MetricObserver> observers) {
            return new PollRunnable(poller, BasicMetricFilter.MATCH_ALL, true, observers);
        }

        MetricPoller createMonitorRegistryMetricPoller() {
            return new MonitorRegistryMetricPoller();
        }
    }
}
