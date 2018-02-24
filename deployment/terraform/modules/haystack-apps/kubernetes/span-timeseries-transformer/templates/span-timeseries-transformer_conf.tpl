health.status.path = "/app/isHealthy"

kafka {
  close.timeout.ms = 30000

  streams {
    application.id = "haystack-span-timeseries-transformer"
    bootstrap.servers = "${kafka_endpoint}"
    num.stream.threads = 1
    commit.interval.ms = 3000
    auto.offset.reset = latest
    timestamp.extractor = "org.apache.kafka.streams.processor.WallclockTimestampExtractor"
  }

  producer {
    topic = "metricpoints"
  }

  consumer {
    topic = "proto-spans"
  }
}