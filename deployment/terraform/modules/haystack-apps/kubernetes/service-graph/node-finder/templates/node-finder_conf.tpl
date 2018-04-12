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
