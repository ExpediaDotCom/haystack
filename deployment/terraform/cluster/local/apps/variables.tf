variable "kubectl_executable_name" {}
variable "haystack_cluster_name" {
  default = "haystack"
}
# traces config
variable "traces" {
  type = "map"
  default = {
    enabled = true,
    version = "6bf751efc8b0e87d062067a4d3df61ecb8ec0a11"
    traces_indexer_instances = 1,
    traces_reader_instances = 1,
    trace_indexer_environment_overrides = "",
    trace_reader_environment_overrides = ""
  }
}



variable "trends" {
  type = "map"
  default = {
    enabled = true,
    version = "10039dc9afa5d2db1b6d93a7540fb6d485192f02"
    span_timeseries_transformer_instances = 1,
    timeseries_aggregator_instances = 1,
    timeseries_aggregator_environment_overrides = "",
    span_timeseries_transformer_environment_overrides = ""
  }
}

# pipes config

variable "pipes" {
  type = "map"
  default = {
    version = "f48a026554636555fc3cb20ac760e4315857f949"

    firehose_writer_enabled = false
    firehose_writer_instances = 1
    firehose_writer_environment_overrides = "",
    firehose_kafka_threadcount = 1,
    firehose_writer_firehose_initialretrysleep = 1,
    firehose_writer_firehose_maxretrysleep = "",
    firehose_writer_firehose_signingregion = ""
    firehose_writer_firehose_streamname = ""
    firehose_writer_firehose_url = ""

    http_poster_enabled = false
    http_poster_environment_overrides = ""
    http_poster_httppost_pollpercent = ""
    http_poster_httppost_url = ""
    http_poster_instances = 1

    json_transformer_enabled = false
    json_transformer_environment_overrides = ""
    json_transformer_instances = 1

    kafka_producer_enabled = false
    kafka_producer_environment_overrides = ""
    kafka_producer_instances = 1

    pipes_secret_detector_enabled = false
    pipes_secret_detector_environment_overrides = ""
    pipes_secret_detector_instances = 1
    pipes_secret_detector_kafka_threadcount = 1
    pipes_secret_detector_secretsnotifications_email_from = ""
    pipes_secret_detector_secretsnotifications_email_host = ""
    pipes_secret_detector_secretsnotifications_email_subject = ""
    pipes_secret_detector_secretsnotifications_email_tos = ""
    pipes_secret_detector_secretsnotifications_whitelist_bucket = ""
  }
}
# collectors config

variable "collector" {
  type = "map"
  default = {
    version = "e1d967e30a9a87122d8c332700cc4a3152db7f8a"
    kinesis_span_collector_instances = 1,
    kinesis_span_collector_enabled = false,
    kinesis_stream_region = "",
    kinesis_stream_name = "",
    kinesis_span_collector_sts_role_arn = "",
    kinesis_span_collector_environment_overrides = ""
  }
}

# service-graph config
variable "service-graph" {
  type = "map"
  default = {
    enabled = false,
    version = "e407d22f377a6baa2aae24b184d0dd7d23c7486d"
    node_finder_instances = 1,
    node_finder_environment_overrides = ""
    graph_builder_instances = 1,
    graph_builder_environment_overrides = ""
  }
}

# ui config
variable "ui" {
  type = "map"
  default = {
    enabled = true
    version = "c7be950888aef83fa1709c40a62b9ce68066b85b"
    instances = 1,
    whitelisted_fields = "",
    enable_sso = false,
    saml_callback_url = "",
    saml_entry_point = "",
    saml_issuer = "",
    session_secret = ""
  }
}

#metrictank

variable "metrictank" {
  type = "map"
  default = {
    instances = 1,
    memory_limit = "250Mi",
    environment_overrides = ""
    external_kafka_broker_hostname = ""
    external_kafka_broker_port = 9092,
    external_hostname = "",
    external_port = 6060,
  }
}
