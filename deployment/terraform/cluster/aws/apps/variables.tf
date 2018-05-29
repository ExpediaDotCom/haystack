##configs to setup kubectl context in case just apps is deployed in another host
variable "haystack_cluster_name" {}
variable "s3_bucket_name" {}
variable "aws_domain_name" {}
variable "kubectl_executable_name" {}

# traces config
variable "traces" {
  type = "map"
  default = {
    enabled = true,
    version = "00754f095eef7926418e68d5ee47630a47be6c94"
    traces_indexer_instances = 1,
    traces_reader_instances = 1,
    trace_indexer_environment_overrides = "",
    trace_reader_environment_overrides = "",
    elasticsearch_template = "{\"template\":\"haystack-traces*\",\"settings\":{\"number_of_shards\":16,\"index.mapping.ignore_malformed\":true,\"analysis\":{\"normalizer\":{\"lowercase_normalizer\":{\"type\":\"custom\",\"filter\":[\"lowercase\"]}}}},\"aliases\":{\"haystack-traces\":{}},\"mappings\":{\"spans\":{\"_field_names\":{\"enabled\":false},\"_all\":{\"enabled\":false},\"_source\":{\"includes\":[\"traceid\"]},\"properties\":{\"traceid\":{\"enabled\":false},\"spans\":{\"type\":\"nested\",\"properties\":{\"servicename\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":true,\"norms\":false},\"operationname\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":true,\"norms\":false},\"starttime\":{\"type\":\"long\",\"doc_values\":true}}}},\"dynamic_templates\":[{\"strings_as_keywords_1\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false}}},{\"longs_disable_doc_norms\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"doc_values\":false,\"norms\":false}}}]}}}"
  }
}

# service-graph config
variable "service-graph" {
  type = "map"
  default = {
    enabled = true,
    version = "59a101cc9abba5a3f6f17305be4207ea350f3170"
    node_finder_instances = 1,
    node_finder_environment_overrides = ""
    graph_builder_instances = 1,
    graph_builder_environment_overrides = ""
  }
}

variable "trends" {
  type = "map"
  default = {
    enabled = true,
    version = "77351a0028a2f644fedc7a7899e79a5b41d8273e"
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
    version = "a20a8087f5ddc3fbf1a1c72dcff840608accadbf"

    firehose_kafka_threadcount = 1,
    firehose_writer_enabled = false
    firehose_writer_environment_overrides = "",
    firehose_writer_firehose_initialretrysleep = 10,
    firehose_writer_firehose_maxretrysleep = 3000,
    firehose_writer_firehose_signingregion = ""
    firehose_writer_firehose_streamname = ""
    firehose_writer_firehose_url = ""
    firehose_writer_haystack_kafka_fromtopic = ""
    firehose_writer_instances = 1

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
    pipes_secret_detector_kafka_threadcount = 1
    pipes_secret_detector_instances = 1
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
    version = "08534fa97ffb723ef6f138bddecd9dfbce0b9dd2"
    kinesis_span_collector_instances = 1,
    kinesis_span_collector_enabled = true,
    kinesis_stream_region = "",
    kinesis_stream_name = "",
    kinesis_span_collector_sts_role_arn = "",
    kinesis_span_collector_environment_overrides = ""
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
    memory_limit = "4000Mi",
    environment_overrides = ""
    external_kafka_broker_hostname = ""
    external_kafka_broker_port = 1,
    external_hostname = "",
    external_port = 1,
  }
}
