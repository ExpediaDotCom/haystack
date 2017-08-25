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
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
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

    // TODO Add EnvironmentVariablesConfigurationSource object to handle env variables from apply-compose.sh et al
    private static ConfigFilesProvider cfp = () -> Collections.singletonList(Paths.get("base.yaml"));
    private static ClasspathConfigurationSource ccs = new ClasspathConfigurationSource(cfp);
    private static ConfigurationProvider cp = new ConfigurationProviderBuilder().withConfigurationSource(ccs).build();

    // will be mocked out in unit tests
    static GraphiteConfig graphiteConfig = cp.bind("haystack.graphite", GraphiteConfig.class);

    private final Factory factory;

    public MetricPublishing() {
        this(new Factory());
    }

    public MetricPublishing(Factory factory) {
        this.factory = factory;
    }

    public void start() {
        final PollScheduler pollScheduler = PollScheduler.getInstance();
        pollScheduler.start();
        final MetricPoller monitorRegistryMetricPoller = factory.createMonitorRegistryMetricPoller();
        final List<MetricObserver> observers = Collections.singletonList(createGraphiteObserver());
        final PollRunnable task = factory.createTask(monitorRegistryMetricPoller, observers);
        pollScheduler.addPoller(task, graphiteConfig.pollIntervalSeconds(), TimeUnit.SECONDS);
    }

    MetricObserver createGraphiteObserver() {
        final String address = graphiteConfig.address() + ":" + graphiteConfig.port();
        return rateTransform(async(factory.createGraphiteMetricObserver(ASYNC_METRIC_OBSERVER_NAME, address)));
    }

    MetricObserver rateTransform(MetricObserver observer) {
        final long heartbeat = POLL_INTERVAL_SECONDS_TO_HEARTBEAT_MULTIPLIER * graphiteConfig.pollIntervalSeconds();
        return factory.createCounterToRateMetricTransform(observer, heartbeat, TimeUnit.SECONDS);
    }

    MetricObserver async(MetricObserver observer) {
        final long expireTime = POLL_INTERVAL_SECONDS_TO_EXPIRE_TIME_MULTIPLIER * graphiteConfig.pollIntervalSeconds();
        final int queueSize = graphiteConfig.queueSize();
        return factory.createAsyncMetricObserver(observer, queueSize, expireTime);
    }

    static class Factory {
        MetricObserver createAsyncMetricObserver(MetricObserver observer, int queueSize, long expireTime) {
            return new AsyncMetricObserver(ASYNC_METRIC_OBSERVER_NAME, observer, queueSize, expireTime);
        }

        MetricObserver createCounterToRateMetricTransform(
                MetricObserver observer, long heartbeat, TimeUnit timeUnit) {
            return new CounterToRateMetricTransform(observer, heartbeat, timeUnit);
        }

        MetricObserver createGraphiteMetricObserver(String prefix, String address) {
            String hostName;
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                // There's no way to test this catch without introducing excessive ugliness into the code
                hostName = HOST_NAME_UNKNOWN_HOST_EXCEPTION;
            }
            return new GraphiteMetricObserver(prefix, address, new HaystackGraphiteNamingConvention(hostName));
        }

        PollRunnable createTask(MetricPoller poller, Collection<MetricObserver> observers) {
            return new PollRunnable(poller, BasicMetricFilter.MATCH_ALL, true, observers);
        }

        MetricPoller createMonitorRegistryMetricPoller() {
            return new MonitorRegistryMetricPoller();
        }
    }
}
