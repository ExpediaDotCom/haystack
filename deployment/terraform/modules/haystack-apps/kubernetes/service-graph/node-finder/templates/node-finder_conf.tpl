health.status.path = "/app/isHealthy"

kafka {
  close.timeout.ms = 30000

  streams {
    application.id = "haystack-service-graph-node-finder"
    bootstrap.servers = "${kafka_endpoint}"
    num.stream.threads = 4
    request.timeout.ms = 60000
    commit.interval.ms = 3000
    auto.offset.reset = latest
  }

  producer {
    metrics {
      topic = "metricpoints"
      // there are three types of encoders that are used on service and operation names:
      // 1) periodreplacement: replaces all periods with 3 underscores
      // 2) base64: base64 encodes the full name with a padding of _
      // 3) noop: does not perform any encoding
      key.encoder = "${metricpoint_encoder_type}"

    }
    service.call {
      topic = "graph-nodes"
    }
  }

  consumer {
    topic = "proto-spans"
  }

  aggregator {
    interval = 60000
  }
}


haystack.graphite.host = "monitoring-influxdb-graphite.kube-system.svc"
