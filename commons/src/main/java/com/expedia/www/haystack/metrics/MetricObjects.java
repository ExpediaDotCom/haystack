package com.expedia.www.haystack.metrics;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.MonitorRegistry;
import com.netflix.servo.monitor.BasicTimer;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Monitors;
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

    /**
     * Create a new instance of MetricObjects; intended to be used by non-unit-test code.
     */
    public MetricObjects() {
        this(new Factory());
    }

    /**
     * Create a new instance of MetricObjects with a user-specified Factory; intended to be used by unit-test code
     * so that the Factory can be mocked.
     *
     * @param factory The Factory to use to obtain a MonitorRegistry
     */
    MetricObjects(Factory factory) {
        this.factory = factory;
    }

    /**
     * Creates a new Counter; you should only call this method once for each Counter in your code.
     *
     * @param subsystem   the subsystem, typically something like "pipes" or "trends".
     * @param klass       the metric class, frequently (but not necessarily) the class containing the Counter.
     * @param counterName the name of the Counter, usually the name of the variable holding the Counter instance;
     *                    using upper case for counterName is recommended.
     * @return a new Counter that this method registers in the DefaultMonitorRegistry before returning it.
     */
    public Counter createAndRegisterCounter(String subsystem, String klass, String counterName) {
        final TaggingContext taggingContext = () -> getTags(subsystem, klass);
        final Counter counter = Monitors.newCounter(counterName, taggingContext);
        factory.getMonitorRegistry().register(counter);
        return counter;
    }

    /**
     * Creates a new BasicTimer; you should only call this method once for each BasicTimer in your code.
     *
     * @param subsystem the subsystem, typically something like "pipes" or "trends".
     * @param klass     the metric class, frequently (but not necessarily) the class containing the Timer.
     * @param timerName the name of the Timer, usually the name of the variable holding the Timer instance
     *                  using upper case for timerName is recommended.
     * @param timeUnit  desired precision, typically TimeUnit.MILLISECONDS; use TimeUnit.NANOSECONDS (rare) or
     *                  TimeUnit.MICROSECONDS for more precision.
     * @return a new Timer that this method registers in the DefaultMonitorRegistry before returning it.
     */
    public BasicTimer createAndRegisterBasicTimer(String subsystem, String klass, String timerName, TimeUnit timeUnit) {
        final TaggingContext taggingContext = () -> getTags(subsystem, klass);
        final MonitorConfig.Builder builder = MonitorConfig.builder(timerName).withTags(taggingContext.getTags());
        final BasicTimer basicTimer = new BasicTimer(builder.build(), timeUnit);
        factory.getMonitorRegistry().register(basicTimer);
        return basicTimer;
    }

    private static TagList getTags(String subsystem, String klass) {
        final SmallTagMap.Builder builder = new SmallTagMap.Builder(2);
        builder.add(Tags.newTag(TAG_KEY_SUBSYSTEM, subsystem));
        builder.add(Tags.newTag(TAG_KEY_CLASS, klass));
        return new BasicTagList(builder.result());
    }

    /**
     * Factory to wrap static or final methods; this Factory facilitates unit testing.
     */
    public static class Factory {
        /**
         * The default (and only) constructor.
         */
        public Factory() {
            // default constructor
        }

        /**
         * Returns the MonitorRegistry that keeps track of objects with
         * {@link com.netflix.servo.annotations.Monitor} annotations.
         *
         * @return the MonitorRegistry to use.
         */
        public MonitorRegistry getMonitorRegistry() {
            return DefaultMonitorRegistry.getInstance();
        }
    }
}

