package com.expedia.www.sample.span.decorator;

import com.expedia.open.tracing.Span;
import com.expedia.open.tracing.Tag;
import com.expedia.www.haystack.span.decorators.SpanDecorator;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleSpanDecorator implements SpanDecorator {
    private static final Logger logger = LoggerFactory.getLogger(SampleSpanDecorator.class);
    private Config config;

    public void init(Config config) {
        this.config = config;
    }

    public SampleSpanDecorator() {
    }

    @Override
    public Span.Builder decorate(Span.Builder spanBuilder) {
        spanBuilder.addTags(Tag.newBuilder().setKey(config.getString("tag.key"))
                .setVStr("SAMPLE-TAG").build());
        return spanBuilder;
    }

    @Override
    public String name() {
        return "SAMPLE_SPAN_DECORATOR";
    }
}
