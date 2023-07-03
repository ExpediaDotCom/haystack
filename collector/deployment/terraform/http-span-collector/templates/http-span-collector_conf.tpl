kafka {
  producer {
    topic = "proto-spans"
    props {
      bootstrap.servers = "${kafka_endpoint}"
      retries = 50
      batch.size = 153600
      linger.ms = 250
      compression.type = "lz4"
    }
  }
}

extractor {
  output.format = "proto"
  spans.validation {
    max.size {
      enable = "${max_spansize_validation_enabled}"
      log.only = "${max_spansize_log_only}"
      max.size.limit = "${max_spansize_limit}"
      message.tag.key = "${message_tag_key}"
      message.tag.value = "${message_tag_value}"
      skip.tags = "${max_spansize_skip_tags}"
      skip.services = "${max_spansize_skip_services}"
    }
  }
}

http {
  host = "0.0.0.0"
  port = ${container_port}
}

