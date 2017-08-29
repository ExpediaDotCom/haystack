package com.expedia.www.haystack.metrics;

/**
 * Interface that glues configuration sources with code that needs and reads those configurations
 */
public interface GraphiteConfig {
    /**
     * IP address of the Graphite store that will receive Graphite messages
     *
     * @return the IP address or DNS name to use
     */
    String address();

    /**
     * Port of the Graphite store that will receive Graphite messages
     *
     * @return the port to use (typically 2003)
     */
    int port();

    /**
     * How often metric elements should be polled and sent to graphite
     *
     * @return the poll interval, in seconds
     */
    int pollIntervalSeconds();

    /**
     * The queue size of the asynchronous metric observer that polls metric data to send to Graphite
     *
     * @return the queue size to use
     */
    int queueSize();
}
