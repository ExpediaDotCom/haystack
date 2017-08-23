package com.expedia.www.haystack.metrics;

import com.netflix.servo.Metric;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.publish.graphite.GraphiteNamingConvention;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.TagList;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_CLASS;
import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_SUBSYSTEM;

public class HaystackGraphiteNamingConvention implements GraphiteNamingConvention {
    static final String HOST_NAME_UNKNOWN_HOST_EXCEPTION = "HostName-UnknownHostException";
    static final String MISSING_TAG = "MISSING_TAG_%s";
    static final String MISSING_VALUE = "MISSING_VALUE_%s";
    static final String METRIC_FORMAT = "%s.%s.%s.%s_%s";

    static Factory factory = new Factory(); // will be mocked out in unit tests
    static final String LOCAL_HOST_NAME = getLocalHost();

    static String getLocalHost() {
        try {
            return cleanup(factory.getLocalHost().getHostName(), "");
        } catch (UnknownHostException e) {
            return HOST_NAME_UNKNOWN_HOST_EXCEPTION;
        }
    }

    @Override
    public String getName(Metric metric) {
        final MonitorConfig config = metric.getConfig();
        final TagList tags = config.getTags();
        final String subsystem = cleanup(tags, TAG_KEY_SUBSYSTEM);
        final String klass = cleanup(tags, TAG_KEY_CLASS);
        final String configName = config.getName(); // Servo disallows null for name, no need to cleanup
        final String type = cleanup(tags, DataSourceType.KEY);
        return String.format(METRIC_FORMAT, LOCAL_HOST_NAME, subsystem, klass, configName, type);
    }

    private static String cleanup(TagList tags, String name) {
        return cleanup(tags.getTag(name), name);
    }

    private static String cleanup(Tag tag, String name) {
        if(tag == null) {
            return String.format(MISSING_TAG, name);
        }
        return cleanup(tag.getValue(), name);
    }

    private static String cleanup(String value, String name) {
        if(value == null) {
            return String.format(MISSING_VALUE, name);
        }
        return value.replace(" ", "_").replace(".", "_");
    }

    static class Factory {
        InetAddress getLocalHost() throws UnknownHostException {
            return InetAddress.getLocalHost();
        }
    }
}
