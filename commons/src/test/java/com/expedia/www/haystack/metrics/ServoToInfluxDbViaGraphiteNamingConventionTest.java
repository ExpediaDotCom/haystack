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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_APPLICATION;
import static com.expedia.www.haystack.metrics.ServoToInfluxDbViaGraphiteNamingConvention.METRIC_FORMAT;
import static com.expedia.www.haystack.metrics.ServoToInfluxDbViaGraphiteNamingConvention.MISSING_TAG;
import static com.expedia.www.haystack.metrics.ServoToInfluxDbViaGraphiteNamingConvention.STATISTIC_TAG_NAME;
import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_CLASS;
import static com.expedia.www.haystack.metrics.MetricObjects.TAG_KEY_SUBSYSTEM;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ServoToInfluxDbViaGraphiteNamingConventionTest {
    private final static Random RANDOM = new Random();
    private final static String METRIC_NAME = RANDOM.nextLong() + "METRIC_NAME";
    private final static String SUBSYSTEM = RANDOM.nextLong() + "SUBSYSTEM";
    private final static String APPLICATION = RANDOM.nextLong() + "APPLICATION";
    private final static String CLASS = RANDOM.nextLong() + "CLASS";
    private final static String TYPE = RANDOM.nextLong() + "TYPE";
    private final static String STATISTIC = RANDOM.nextLong() + "STATISTIC";
    private final static String LOCAL_HOST_NAME = "127.0.0.1";
    private final static String LOCAL_HOST_NAME_CLEANED = LOCAL_HOST_NAME.replace(".", "_");

    @Mock
    private InetAddress mockLocalHost;

    private ServoToInfluxDbViaGraphiteNamingConvention servoToInfluxDbViaGraphiteNamingConvention;

    @Before
    public void setUp() {
        servoToInfluxDbViaGraphiteNamingConvention = new ServoToInfluxDbViaGraphiteNamingConvention(LOCAL_HOST_NAME);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockLocalHost);
    }

    @Test
    public void testGetNameAllNull() {
        final Metric metric = new Metric(METRIC_NAME, BasicTagList.EMPTY, 0, 0);

        final String name = servoToInfluxDbViaGraphiteNamingConvention.getName(metric);

        final String expected = String.format(METRIC_FORMAT,
                String.format(MISSING_TAG, TAG_KEY_SUBSYSTEM),
                String.format(MISSING_TAG, TAG_KEY_APPLICATION), LOCAL_HOST_NAME_CLEANED,
                String.format(MISSING_TAG, TAG_KEY_CLASS), METRIC_NAME,
                String.format(MISSING_TAG, DataSourceType.KEY));
        assertEquals(expected, name);
    }

    @Test
    public void testGetNameNoneNullWithoutStatistic() {
        final List<Tag> tagList = new ArrayList<>(4);
        tagList.add(Tags.newTag(TAG_KEY_SUBSYSTEM, SUBSYSTEM));
        tagList.add(Tags.newTag(TAG_KEY_APPLICATION, APPLICATION));
        tagList.add(Tags.newTag(TAG_KEY_CLASS, CLASS));
        tagList.add(Tags.newTag(DataSourceType.KEY, TYPE));
        final Metric metric = new Metric(METRIC_NAME, new BasicTagList(tagList), 0, 0);

        final String name = servoToInfluxDbViaGraphiteNamingConvention.getName(metric);

        assertEquals(String.format(
                METRIC_FORMAT, SUBSYSTEM, APPLICATION, LOCAL_HOST_NAME_CLEANED, CLASS, METRIC_NAME, TYPE), name);
    }

    @Test
    public void testGetNameNoneNullWithStatistic() {
        final List<Tag> tagList = new ArrayList<>(5);
        tagList.add(Tags.newTag(TAG_KEY_SUBSYSTEM, SUBSYSTEM));
        tagList.add(Tags.newTag(TAG_KEY_APPLICATION, APPLICATION));
        tagList.add(Tags.newTag(TAG_KEY_CLASS, CLASS));
        tagList.add(Tags.newTag(DataSourceType.KEY, TYPE));
        tagList.add(Tags.newTag(STATISTIC_TAG_NAME, STATISTIC));
        final Metric metric = new Metric(METRIC_NAME, new BasicTagList(tagList), 0, 0);

        final String name = servoToInfluxDbViaGraphiteNamingConvention.getName(metric);

        final String expected = String.format(METRIC_FORMAT,
                SUBSYSTEM, APPLICATION, LOCAL_HOST_NAME_CLEANED, CLASS, METRIC_NAME, TYPE + '_' + STATISTIC);
        assertEquals(expected, name);
    }
}
