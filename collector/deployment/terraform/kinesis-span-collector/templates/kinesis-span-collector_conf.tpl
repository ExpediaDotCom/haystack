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

kinesis {
  sts.role.arn = "${sts_role_arn}"
  aws.region = "${kinesis_stream_region}"
  app.group.name = "${app_group_name}"

  stream {
    name = "${kinesis_stream_name}"
    position = "LATEST"
  }

  checkpoint {
    interval.ms = 15000
    retries = 50
    retry.interval.ms = 250
  }

  task.backoff.ms = 200
  max.records.read = 2000
  idle.time.between.reads.ms = 500
  shard.sync.interval.ms = 30000

  metrics {
    level = "NONE"
    buffer.time.ms = 15000
  }
}
