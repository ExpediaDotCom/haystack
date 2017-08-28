package com.expedia.www.haystack.metrics;

public interface GraphiteConfig {
    String address();

    int port();

    int pollIntervalSeconds();

    int queueSize();
}
