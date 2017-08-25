package com.expedia.www.haystack.metrics;

import com.netflix.servo.Metric;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.Tags;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.expedia.www.haystack.metrics.HaystackGraphiteNamingConvention.HOST_NAME_UNKNOWN_HOST_EXCEPTION;
import static com.expedia.www.haystack.metrics.HaystackGraphiteNamingConvention.LOCAL_HOST_NAME;
import static com.expedia.www.haystack.metrics.HaystackGraphiteNamingConvention.METRIC_FORMAT;
import static com.expedia.www.haystack.metrics.HaystackGraphiteNamingConvention.MISSING_TAG;
import static com.expedia.www.haystack.metrics.HaystackGraphiteNamingConvention.MISSING_VALUE;
import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_CLASS;
import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_SUBSYSTEM;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HaystackGraphiteNamingConventionTest {
    private final static Random RANDOM = new Random();
    private final static String METRIC_NAME = RANDOM.nextLong() + "METRIC_NAME";
    private final static String SUBSYSTEM = RANDOM.nextLong() + "SUBSYSTEM";
    private final static String CLASS = RANDOM.nextLong() + "CLASS";
    private final static String TYPE = RANDOM.nextLong() + "TYPE";

    @Mock
    private HaystackGraphiteNamingConvention.Factory mockFactory;
    private HaystackGraphiteNamingConvention.Factory realFactory;

    @Mock
    private InetAddress mockLocalHost;

    private HaystackGraphiteNamingConvention haystackGraphiteNamingConvention;

    @Before
    public void setUp() {
        realFactory = HaystackGraphiteNamingConvention.factory;
        HaystackGraphiteNamingConvention.factory = mockFactory;
        haystackGraphiteNamingConvention = new HaystackGraphiteNamingConvention();
    }

    @After
    public void tearDown() {
        HaystackGraphiteNamingConvention.factory = realFactory;
        verifyNoMoreInteractions(mockFactory, mockLocalHost);
    }

    @Test
    public void testThatStaticHostNameIsLocalHost() throws UnknownHostException {
        final String expected = InetAddress.getLocalHost().getHostName().replace(" ", "_").replace(".", "_");
        assertEquals(expected, LOCAL_HOST_NAME);
    }

    @Test
    public void testGetLocalHostException() throws UnknownHostException {
        when(mockFactory.getLocalHost()).thenThrow(new UnknownHostException());

        final String localHost = HaystackGraphiteNamingConvention.getLocalHost();
        assertEquals(HOST_NAME_UNKNOWN_HOST_EXCEPTION, localHost);

        verify(mockFactory).getLocalHost();
    }

    @Test
    public void testGetLocalHostNullHostName() throws UnknownHostException {
        when(mockFactory.getLocalHost()).thenReturn(mockLocalHost);

        final String localHost = HaystackGraphiteNamingConvention.getLocalHost();
        assertEquals(String.format(MISSING_VALUE, ""), localHost);

        verify(mockFactory).getLocalHost();
        //noinspection ResultOfMethodCallIgnored
        verify(mockLocalHost).getHostName();
    }

    @Test
    public void testGetNameAllNull() {
        final Metric metric = new Metric(METRIC_NAME, BasicTagList.EMPTY, 0, 0);

        final String name = haystackGraphiteNamingConvention.getName(metric);

        final String expected = String.format(METRIC_FORMAT, String.format(MISSING_TAG, TAG_KEY_SUBSYSTEM),
                LOCAL_HOST_NAME, String.format(MISSING_TAG, TAG_KEY_CLASS), METRIC_NAME,
                String.format(MISSING_TAG, DataSourceType.KEY));
        assertEquals(expected, name);
    }

    @Test
    public void testGetNameNoneNull() {
        final List<Tag> tagList = new ArrayList<>(3);
        tagList.add(Tags.newTag(TAG_KEY_SUBSYSTEM, SUBSYSTEM));
        tagList.add(Tags.newTag(TAG_KEY_CLASS, CLASS));
        tagList.add(Tags.newTag(DataSourceType.KEY, TYPE));
        final Metric metric = new Metric(METRIC_NAME, new BasicTagList(tagList), 0, 0);

        final String name = haystackGraphiteNamingConvention.getName(metric);

        assertEquals(String.format(METRIC_FORMAT, SUBSYSTEM, LOCAL_HOST_NAME, CLASS, METRIC_NAME, TYPE), name);
    }
}
