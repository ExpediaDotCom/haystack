package com.expedia.www.haystack.metrics;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.MonitorRegistry;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.Monitors;
import com.netflix.servo.monitor.Timer;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.SmallTagMap;
import com.netflix.servo.tag.TagList;
import com.netflix.servo.tag.TaggingContext;
import com.netflix.servo.tag.Tags;

import java.util.concurrent.TimeUnit;

/**
 * Creates Servo's Counter and Timer objects, registering them with the default monitor registry when creating them.
 */
@SuppressWarnings("WeakerAccess")
public class MetricObjects {
    static final String TAG_KEY_SUBSYSTEM = "subsystem";
    static final String TAG_KEY_CLASS = "class";

    private final Factory factory;

    public MetricObjects() {
        this(new Factory());
    }

    public MetricObjects(Factory factory) {
        this.factory = factory;
    }

    /**
     * Creates a new Counter; you should only call this method once for each Counter in your code.
     *
     * @param subsystem   the subsystem, typically something like "pipes" or "trends"
     * @param klass       the metric class, frequently (but not necessarily) the class containing the counter the module
     * @param counterName the name of the counter, usually the name of the variable holding the Counter instance
     * @return a new Counter that this method registers in the DefaultMonitorRegistry before returning it.
     */
    public Counter createAndRegisterCounter(String subsystem, String klass, String counterName) {
        final TaggingContext taggingContext = () -> getTags(subsystem, klass);
        final Counter counter = Monitors.newCounter(counterName, taggingContext);
        factory.getMonitorRegistry().register(counter);
        return counter;
    }

    /**
     * Creates a new Timer; you should only call this method once for each Timer in your code
     *
     * @param subsystem the subsystem, typically something like "pipes" or "trends"
     * @param klass     the metric class, frequently (but not necessarily) the class containing the counter the module
     * @param timerName the name of the timer, usually the name of the variable holding the Timer instance
     * @param timeUnit  desired precision, typically TimeUnit.MILLISECONDS
     * @return a new Timer that this method registers in the DefaultMonitorRegistry before returning it.
     */
    public Timer createAndRegisterTimer(String subsystem, String klass, String timerName, TimeUnit timeUnit) {
        final TaggingContext taggingContext = () -> getTags(subsystem, klass);
        final Timer timer = Monitors.newTimer(timerName, timeUnit, taggingContext);
        factory.getMonitorRegistry().register(timer);
        return timer;
    }

    private static TagList getTags(String subsystem, String klass) {
        final SmallTagMap.Builder builder = new SmallTagMap.Builder(2);
        builder.add(Tags.newTag(TAG_KEY_SUBSYSTEM, subsystem));
        builder.add(Tags.newTag(TAG_KEY_CLASS, klass));
        return new BasicTagList(builder.result());
    }

    static class Factory {
        MonitorRegistry getMonitorRegistry() {
            return DefaultMonitorRegistry.getInstance();
        }
    }
}

