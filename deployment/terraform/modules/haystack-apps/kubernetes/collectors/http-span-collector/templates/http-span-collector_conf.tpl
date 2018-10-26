kafka {
  producer {
    topic = "proto-spans"
    props {
      bootstrap.servers = "${kafka_endpoint}"
      retries = 50
      batch.size = 65536
      linger.ms = 100
    }
  }
}

extractor {
  output.format = "proto"
}

http {
  host = "0.0.0.0"
  port = ${container_port}
}

