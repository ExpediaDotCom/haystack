package com.expedia.www.haystack.metrics;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.MonitorRegistry;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.Monitor;
import com.netflix.servo.monitor.Timer;
import com.netflix.servo.tag.TagList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_CLASS;
import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_SUBSYSTEM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetricObjectsTest {
    private final static Random RANDOM = new Random();
    private final static String SUBSYSTEM = RANDOM.nextLong() + "SUBSYSTEM";
    private final static String CLASS = RANDOM.nextLong() + "CLASS";
    private final static String METRIC_NAME = RANDOM.nextLong() + "METRIC_NAME";

    @Mock
    private MetricObjects.Factory mockFactory;
    private MetricObjects.Factory realFactory;

    @Mock
    private MonitorRegistry mockMonitorRegistry;

    @Before
    public void setUp() {
        realFactory = MetricObjects.factory;
        MetricObjects.factory = mockFactory;
    }

    @After
    public void tearDown() {
        MetricObjects.factory = realFactory;
        verifyNoMoreInteractions(mockFactory, mockMonitorRegistry);
    }

    @Test
    public void testCreateAndRegisterCounter() {
        when(mockFactory.getMonitorRegistry()).thenReturn(mockMonitorRegistry);

        final Counter counter = MetricObjects.createAndRegisterCounter(SUBSYSTEM, CLASS, METRIC_NAME);

        assertsAndVerifiesForCreateAndRegister(counter);
    }

    @Test
    public void testCreateAndRegisterTimer() {
        when(mockFactory.getMonitorRegistry()).thenReturn(mockMonitorRegistry);

        final Timer timer = MetricObjects.createAndRegisterTimer(SUBSYSTEM, CLASS, METRIC_NAME, TimeUnit.MILLISECONDS);

        assertsAndVerifiesForCreateAndRegister(timer);
    }

    private void assertsAndVerifiesForCreateAndRegister(Monitor<?> monitor) {
        final TagList tagList = monitor.getConfig().getTags();
        assertEquals(2, tagList.size());
        assertEquals(SUBSYSTEM, tagList.getValue(TAG_KEY_SUBSYSTEM));
        assertEquals(CLASS, tagList.getValue(TAG_KEY_CLASS));
        assertEquals(METRIC_NAME, monitor.getConfig().getName());
        verify(mockMonitorRegistry).register(monitor);
        verify(mockFactory).getMonitorRegistry();
    }

    @Test
    public void testFactoryGetDefaultMonitorRegisterInstance() {
        assertSame(DefaultMonitorRegistry.getInstance(), realFactory.getMonitorRegistry());
    }

    @Test
    public void testDefaultConstructor() {
        new MetricObjects();
    }
}
