package com.expedia.www.haystack.span.decorators;

import com.expedia.open.tracing.Span;
import com.expedia.open.tracing.Tag;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AdditionalTagsSpanDecoratorTest {
    private final static Logger logger = LoggerFactory.getLogger(AdditionalTagsSpanDecorator.class);

    @Before
    public void setup() {

    }

    @Test
    public void decorateWithNoDuplicateTags() {
        final Map<String, String> tagConfig = new HashMap<String, String>(){{
            put("X-HAYSTACK-TAG1", "VALUE1");
            put("X-HAYSTACK-TAG2", "VALUE2");
        }};
        final AdditionalTagsSpanDecorator additionalTagsSpanDecorator = new AdditionalTagsSpanDecorator();
        additionalTagsSpanDecorator.init(ConfigFactory.parseMap(tagConfig));
        final Span resultSpan = additionalTagsSpanDecorator.decorate(Span.newBuilder()).build();

        final boolean res = resultSpan.getTagsList().stream().allMatch(tag -> {
            final String tagValue = tagConfig.getOrDefault(tag.getKey(), null);
            if (StringUtils.isEmpty(tagValue)) {
                return true;
            } else if(tagValue.equals(tag.getVStr())) {

                return true;
            }
            return false;
        });

        assertEquals(res, true);
    }

    @Test
    public void decorateWithExistingDuplicateTags() {
        final Map<String, String> tagConfig = new HashMap<String, String>(){{
            put("X-HAYSTACK-TAG1", "VALUE1");
            put("X-HAYSTACK-TAG2", "VALUE2");
        }};
        final AdditionalTagsSpanDecorator additionalTagsSpanDecorator = new AdditionalTagsSpanDecorator();
        additionalTagsSpanDecorator.init(ConfigFactory.parseMap(tagConfig));
        final Span.Builder spanBuilder = Span.newBuilder().addTags(Tag.newBuilder().setKey("X-HAYSTACK-TAG1").setVStr("VALUE3"));
        final Span resultSpan = additionalTagsSpanDecorator.decorate(spanBuilder).build();

        final boolean res = resultSpan.getTagsList().stream().allMatch(tag -> {
            final String tagValue = tagConfig.getOrDefault(tag.getKey(), null);
            if (StringUtils.isEmpty(tagValue)) {
                return true;
            } else if(tagValue.equals(tag.getVStr())) {
                return true;
            } else if(tag.getVStr().equals("VALUE3")) {
                return true;
            }
            return false;
        });

        assertEquals(res, true);
    }

}