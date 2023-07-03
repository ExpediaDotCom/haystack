package com.expedia.www.haystack.span.decorators;

import com.expedia.open.tracing.Span;
import com.expedia.open.tracing.Tag;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.stream.Collectors;

public class AdditionalTagsSpanDecorator implements SpanDecorator {

    private Config tagConfig;

    public AdditionalTagsSpanDecorator() {
    }

    @Override
    public void init(Config config) {
        tagConfig = config;
    }

    @Override
    public Span.Builder decorate(Span.Builder span) {
        return addHaystackMetadataTags(span);
    }

    @Override
    public String name() {
        return AdditionalTagsSpanDecorator.class.getName();
    }

    private Span.Builder addHaystackMetadataTags(Span.Builder spanBuilder) {
        final Map<String, String> spanTags = spanBuilder.getTagsList().stream()
                .collect(Collectors.toMap(Tag::getKey, Tag::getVStr));

        tagConfig.entrySet().forEach(tag -> {
            final String tagValue = spanTags.getOrDefault(tag.getKey(), null);
            if (StringUtils.isEmpty(tagValue)) {
                spanBuilder.addTags(Tag.newBuilder().setKey(tag.getKey()).setVStr(tag.getValue().unwrapped().toString()));
            }
        });

        return spanBuilder;
    }

}
