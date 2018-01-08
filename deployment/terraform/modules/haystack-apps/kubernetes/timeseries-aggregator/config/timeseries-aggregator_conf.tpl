
health.status.path = "/app/isHealthy"

kafka {
  close.timeout.ms = 30000

  streams {
    application.id = "haystack-timeseries-aggregator"
    bootstrap.servers = "${kafka_endpoint}"
    num.stream.threads = 1
    commit.interval.ms = 3000
    auto.offset.reset = latest
    timestamp.extractor = "com.expedia.www.haystack.trends.kstream.MetricPointTimestampExtractor"
  }

  producer {
    topic = "mdm"
  }

  consumer {
    topic = "metricpoints"
  }
}