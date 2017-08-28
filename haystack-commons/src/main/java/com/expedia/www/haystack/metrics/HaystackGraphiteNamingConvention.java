package com.expedia.www.haystack.metrics;

import com.netflix.servo.Metric;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.publish.graphite.GraphiteNamingConvention;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.TagList;

import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_CLASS;
import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_SUBSYSTEM;

public class HaystackGraphiteNamingConvention implements GraphiteNamingConvention {
    static final String MISSING_TAG = "MISSING_TAG_%s";
    static final String METRIC_FORMAT = "%s.%s.%s.%s_%s";

    private final String hostName;

    HaystackGraphiteNamingConvention(String hostName) {
        this.hostName = cleanup(hostName);
    }

    @Override
    public String getName(Metric metric) {
        final MonitorConfig config = metric.getConfig();
        final TagList tags = config.getTags();
        final String subsystem = cleanup(tags, TAG_KEY_SUBSYSTEM);
        final String klass = cleanup(tags, TAG_KEY_CLASS);
        final String configName = config.getName(); // Servo disallows null for name, no need to cleanup
        final String type = cleanup(tags, DataSourceType.KEY);
        return String.format(METRIC_FORMAT, subsystem, hostName, klass, configName, type);
    }

    private static String cleanup(TagList tags, String name) {
        return cleanup(tags.getTag(name), name);
    }

    private static String cleanup(Tag tag, String name) {
        if(tag == null) {
            return String.format(MISSING_TAG, name);
        }
        return cleanup(tag.getValue());
    }

    private static String cleanup(String value) {
        // Servo disallows null or "" in tag value, so there's no need to check for that here;
        // just handle spaces and periods, replacing each such illegal character with an underscore.
        return value.replace(" ", "_").replace(".", "_");
    }
}
