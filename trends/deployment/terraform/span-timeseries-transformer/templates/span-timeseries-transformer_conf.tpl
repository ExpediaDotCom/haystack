health.status.path = "/app/isHealthy"

kafka {
  close.timeout.ms = 30000

  streams {
    application.id = "span-timeseries-transformer-v2"
    bootstrap.servers = "${kafka_endpoint}"
    num.stream.threads = "${kafka_num_stream_threads}"
    commit.interval.ms = 3000
    auto.offset.reset = latest
    timestamp.extractor = "com.expedia.www.haystack.commons.kstreams.SpanTimestampExtractor"
  }

  producer {
    topic = "metric-data-points"
  }

  consumer {
    topic = "proto-spans"
  }
}

// there are three types of encoders that are used on service and operation names:
// 1) periodreplacement: replaces all periods with 3 underscores
// 2) base64: base64 encodes the full name with a padding of _
// 3) noop: does not perform any encoding
metricpoint.encoder.type = "${metricpoint_encoder_type}"
enable.metricpoint.service.level.generation = false

blacklist.services = []
