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

  accumulator {
    interval = 2500
  }
  // collector tags allow service graph to collect tags from spans and have them available when querying service
  // graph. Example: you can collect the tags service tier and infraprovider tags using value "[tier,infraprovider]"
  collectorTags = ${collect_tags}

  node.metadata {
    topic {
      autocreate = true
      name = "haystack-node-finder-metadata"
      partition.count = 6
      replication.factor = 2
    }
  }
}

