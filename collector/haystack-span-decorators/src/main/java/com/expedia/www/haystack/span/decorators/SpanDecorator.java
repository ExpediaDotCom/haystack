package com.expedia.www.haystack.span.decorators;

import com.expedia.open.tracing.Span;
import com.typesafe.config.Config;

public interface SpanDecorator {
    void init(Config config);
    Span.Builder decorate(Span.Builder span);
    String name();
}
