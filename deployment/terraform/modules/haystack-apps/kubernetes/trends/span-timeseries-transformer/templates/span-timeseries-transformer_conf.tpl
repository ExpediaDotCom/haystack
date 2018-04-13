health.status.path = "/app/isHealthy"

kafka {
  close.timeout.ms = 30000

  streams {
    application.id = "span-timeseries-transformer"
    bootstrap.servers = "${kafka_endpoint}"
    num.stream.threads = 3
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
blacklist.services = [
  "cs-deposits-distribution",
  "lty-awards-service",
  "cs-tcs-gateway",
  "ews-booking-service"
]
