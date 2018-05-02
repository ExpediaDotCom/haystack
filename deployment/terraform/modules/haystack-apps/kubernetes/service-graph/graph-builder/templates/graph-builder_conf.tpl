health.status.path = "/app/isHealthy"

kafka {
  close.timeout.ms = 30000

  streams {
    application.id = "haystack-service-graph-graph-builder"
    bootstrap.servers = "${kafka_endpoint}"
    application.server = "localhost:8080"
    num.stream.threads = 4
    request.timeout.ms = 60000
    commit.interval.ms = 3000
    auto.offset.reset = latest
    timestamp.extractor = "org.apache.kafka.streams.processor.WallclockTimestampExtractor"
  }

  consumer {
    topic = "graph-nodes"
  }

  producer {
    topic = "service-graph"
  }

  accumulator {
    interval = 60000
  }
}

service {
    threads {
        min = 1
        max = 5
        idle.timeout = 12000
    }

    http {
        port = 8080
        idle.timeout = 12000
    }

    client {
        connection.timeout = 1000
        socket.timeout = 1000
    }
}

haystack.graphite.host = "monitoring-influxdb-graphite.kube-system.svc"
