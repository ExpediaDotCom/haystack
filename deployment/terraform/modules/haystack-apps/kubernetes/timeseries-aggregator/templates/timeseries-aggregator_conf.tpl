health.status.path = "/app/isHealthy"

kafka {
  close.timeout.ms = 30000

  streams {
    application.id = "haystack-timeseries-aggregator"
    bootstrap.servers = "${kafka_endpoint}"
    num.stream.threads = 2
    commit.interval.ms = 5000
    auto.offset.reset = latest
    timestamp.extractor = "com.expedia.www.haystack.trends.kstream.MetricPointTimestampExtractor"
  }

  // For producing data to external kafka: set enable.external.kafka.produce to true and uncomment the props.
  // For producing to same kafka: set enable.external.kafka.produce to false and comment the props.
  producer {
    topic = "mdm"
    enable.external.kafka.produce = ${enable_external_kafka_producer}
     props {
       bootstrap.servers = "${external_kafka_producer_endpoint}"
      retries = 50,
      batch.size = 65536,
      linger.ms = 250
     }
  }

  consumer {
    topic = "metricpoints"
  }
}

state.store {
  cleanup.policy = "compact,delete"
  retention.ms = 14400000 // 4Hrs
}

statestore {
  enable.logging = true
  logging.delay.seconds = 60
}

enable.metricpoint.period.replacement = true