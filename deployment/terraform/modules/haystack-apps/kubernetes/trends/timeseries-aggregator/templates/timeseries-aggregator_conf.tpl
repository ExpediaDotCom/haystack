health.status.path = "/app/isHealthy"

kafka {
  close.timeout.ms = 30000

  streams {
    application.id = "timeseries-aggregator"
    bootstrap.servers = "${kafka_endpoint}"
    num.stream.threads = 2
    commit.interval.ms = 5000
    auto.offset.reset = latest
    timestamp.extractor = "com.expedia.www.haystack.commons.kstreams.MetricPointTimestampExtractor"
    producer.retries = 50,
    replication.factor = 2
    producer.batch.size = 65536,
    producer.linger.ms = 250
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
  cache.size = 32767
}


// there are three types of encoders that are used on service and operation names:
// 1) periodreplacement: replaces all periods with 3 underscores
// 2) base64: base64 encodes the full name with a padding of _
// 3) noop: does not perform any encoding
metricpoint.encoder.type = "periodreplacement"

histogram {
  max.value = 2147483647
  precision = 0
}
