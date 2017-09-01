package com.expedia.www.haystack.metrics;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.MonitorRegistry;
import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.BasicTimer;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Timer;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.SmallTagMap;
import com.netflix.servo.tag.TagList;
import com.netflix.servo.tag.TaggingContext;
import com.netflix.servo.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Creates Servo's Counter and Timer objects, registering them with the default monitor registry when creating them.
 */
@SuppressWarnings("WeakerAccess")
public class MetricObjects {
    static final String TAG_KEY_SUBSYSTEM = "subsystem";
    static final String TAG_KEY_APPLICATION = "application";
    static final String TAG_KEY_CLASS = "class";
    static final String COUNTER_ALREADY_REGISTERED = "The Counter %s has already been registered";
    static final String TIMER_ALREADY_REGISTERED = "The Timer %s has already been registered";
    static final ConcurrentMap<MonitorConfig, Counter> COUNTERS = new ConcurrentHashMap<>();
    static final ConcurrentMap<MonitorConfig, Timer> TIMERS = new ConcurrentHashMap<>();

    private final Factory factory;
    private final Logger logger;

    /**
     * Create a new instance of MetricObjects; intended to be used by non-unit-test code.
     */
    public MetricObjects() {
        this(new Factory(), LoggerFactory.getLogger(MetricObjects.class));
    }

    /**
     * Create a new instance of MetricObjects with a user-specified Factory; intended to be used by unit-test code
     * so that the Factory can be mocked.
     *
     * @param factory The Factory to use to obtain a MonitorRegistry
     */
    MetricObjects(Factory factory, Logger logger) {
        this.logger = logger;
        this.factory = factory;
    }

    /**
     * Creates a new Counter; you should only call this method once for each Counter in your code. This method is
     * thread-safe, because both of Servo's implementations of the MonitorRegistry interface use thread-safe
     * implementations to hold the registered monitors: com.netflix.servo.jmx.JmxMonitorRegistry uses a ConcurrentMap,
     * and com.netflix.servo.BasicMonitorRegistry uses Collections.synchronizedSet().
     * If you call the method twice with the same arguments, the Counter created during the first call will be returned
     * by the second call.
     *
     * @param subsystem   the subsystem, typically something like "pipes" or "trends".
     * @param application the application in the subsystem
     * @param klass       the metric class, frequently (but not necessarily) the class containing the Counter.
     * @param counterName the name of the Counter, usually the name of the variable holding the Counter instance;
     *                    using upper case for counterName is recommended.
     * @return a new Counter that this method registers in the DefaultMonitorRegistry before returning it.
     */
    public Counter createAndRegisterCounter(String subsystem, String application, String klass, String counterName) {
        final MonitorConfig monitorConfig = buildMonitorConfig(subsystem, application, klass, counterName);
        final Counter counter = new BasicCounter(monitorConfig);
        final Counter existingCounter = COUNTERS.putIfAbsent(monitorConfig, counter);
        if (existingCounter != null) {
            logger.warn(String.format(COUNTER_ALREADY_REGISTERED, existingCounter.toString()));
            return existingCounter;
        }
        factory.getMonitorRegistry().register(counter);
        return counter;
    }

    /**
     * Creates a new BasicTimer; you should only call this method once for each BasicTimer in your code.
     * This method is thread-safe; see the comments in {@link #createAndRegisterCounter}.
     * If you call the method twice with the same arguments, the Timer created during the first call will be returned
     * by the second call.
     *
     * @param subsystem   the subsystem, typically something like "pipes" or "trends".
     * @param application the application in the subsystem
     * @param klass       the metric class, frequently (but not necessarily) the class containing the Timer.
     * @param timerName   the name of the Timer, usually the name of the variable holding the Timer instance
     *                    using upper case for timerName is recommended.
     * @param timeUnit    desired precision, typically TimeUnit.MILLISECONDS; use TimeUnit.NANOSECONDS (rare) or
     * @return a new BasicTimer that this method registers in the DefaultMonitorRegistry before returning it.
     */
    public Timer createAndRegisterBasicTimer(
            String subsystem, String application, String klass, String timerName, TimeUnit timeUnit) {
        final MonitorConfig monitorConfig = buildMonitorConfig(subsystem, application, klass, timerName);
        final Timer basicTimer = new BasicTimer(monitorConfig, timeUnit);
        final Timer existingTimer = TIMERS.putIfAbsent(monitorConfig, basicTimer);
        if (existingTimer != null) {
            logger.warn(String.format(TIMER_ALREADY_REGISTERED, existingTimer.toString()));
            return existingTimer;
        }
        factory.getMonitorRegistry().register(basicTimer);
        return basicTimer;
    }

    private MonitorConfig buildMonitorConfig(String subsystem, String application, String klass, String monitorName) {
        final TaggingContext taggingContext = () -> getTags(subsystem, application, klass);
        return MonitorConfig.builder(monitorName).withTags(taggingContext.getTags()).build();
    }

    private static TagList getTags(String subsystem, String application, String klass) {
        final SmallTagMap.Builder builder = new SmallTagMap.Builder(3);
        builder.add(Tags.newTag(TAG_KEY_SUBSYSTEM, subsystem));
        builder.add(Tags.newTag(TAG_KEY_APPLICATION, application));
        builder.add(Tags.newTag(TAG_KEY_CLASS, klass));
        return new BasicTagList(builder.result());
    }

    /**
     * Factory to wrap static or final methods; this Factory facilitates unit testing.
     */
    static class Factory {
        Factory() {
            // default constructor
        }

        MonitorRegistry getMonitorRegistry() {
            return DefaultMonitorRegistry.getInstance();
        }
    }
}

