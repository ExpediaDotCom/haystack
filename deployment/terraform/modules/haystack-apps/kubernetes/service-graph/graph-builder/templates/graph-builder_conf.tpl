health.status.path = "/app/isHealthy"

kafka {
  close.timeout.ms = 30000

  streams {
    application.id = "haystack-service-graph-graph-builder"
    bootstrap.servers = "${kafka_endpoint}"
    num.stream.threads = 4
    request.timeout.ms = 60000
    commit.interval.ms = 3000
    auto.offset.reset = latest
    timestamp.extractor = "org.apache.kafka.streams.processor.WallclockTimestampExtractor"
    replication.factor = 1
  }

  consumer {
    topic = "graph-nodes"
  }

  producer {
    topic = "service-graph"
  }

  aggregate {
    window.sec = 300
    retention.days = 2
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
        connection.timeout = 10000
        socket.timeout = 10000
    }
}
