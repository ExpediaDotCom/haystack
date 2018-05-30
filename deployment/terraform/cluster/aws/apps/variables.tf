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

    indexer_cpu_limit = "500m"
    indexer_memory_limit = "1500"
    indexer_jvm_memory_limit = "1024"
    indexer_elasticsearch_template = "{\"template\":\"haystack-traces*\",\"settings\":{\"number_of_shards\":16,\"index.mapping.ignore_malformed\":true,\"analysis\":{\"normalizer\":{\"lowercase_normalizer\":{\"type\":\"custom\",\"filter\":[\"lowercase\"]}}}},\"aliases\":{\"haystack-traces\":{}},\"mappings\":{\"spans\":{\"_field_names\":{\"enabled\":false},\"_all\":{\"enabled\":false},\"_source\":{\"includes\":[\"traceid\"]},\"properties\":{\"traceid\":{\"enabled\":false},\"spans\":{\"type\":\"nested\",\"properties\":{\"servicename\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false},\"operationname\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false},\"starttime\":{\"type\":\"long\",\"doc_values\":true}}}},\"dynamic_templates\":[{\"strings_as_keywords_1\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false}}},{\"longs_disable_doc_norms\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"doc_values\":false,\"norms\":false}}}]}}}"

    reader_instances = 1
    reader_environment_overrides = ""
    reader_cpu_limit = "500m"
    reader_memory_limit = "1500"
    reader_jvm_memory_limit = "1024"
  }
}

# service-graph config
variable "service-graph" {
  type = "map"
  default = {
    enabled = true
    version = "59a101cc9abba5a3f6f17305be4207ea350f3170"
    node_finder_instances = 1
    node_finder_environment_overrides = ""
    node_finder_cpu_limit = "500m"
    node_finder_memory_limit = "1500"
    node_finder_jvm_memory_limit = "1024"

    graph_builder_instances = 1
    graph_builder_environment_overrides = ""
    graph_builder_cpu_limit = "500m"
    graph_builder_memory_limit = "1500"
    graph_builder_jvm_memory_limit = "1024"
  }
}

variable "trends" {
  type = "map"
  default = {
    enabled = true,
    version = "77351a0028a2f644fedc7a7899e79a5b41d8273e"
    span_timeseries_transformer_instances = 1
    span_timeseries_transformer_cpu_limit = "500m"
    span_timeseries_transformer_memory_limit = "1500"
    span_timeseries_transformer_jvm_memory_limit = "1024"
    span_timeseries_transformer_environment_overrides = ""

    timeseries_aggregator_instances = 1
    timeseries_aggregator_environment_overrides = ""
    timeseries_aggregator_cpu_limit = "500m"
    timeseries_aggregator_memory_limit = "1500"
    timeseries_aggregator_jvm_memory_limit = "1024"
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
    firehose_writer_cpu_limit = "500m"
    firehose_writer_memory_limit = "1500"
    firehose_writer_jvm_memory_limit = "1024"

    http_poster_enabled = false
    http_poster_environment_overrides = ""
    http_poster_httppost_pollpercent = ""
    http_poster_httppost_url = ""
    http_poster_instances = 1
    http_poster_cpu_limit = "500m"
    http_poster_memory_limit = "1500"
    http_poster_jvm_memory_limit = "1024"

    json_transformer_enabled = false
    json_transformer_environment_overrides = ""
    json_transformer_instances = 1
    json_transformer_cpu_limit = "500m"
    json_transformer_memory_limit = "1500"
    json_transformer_jvm_memory_limit = "1024"

    kafka_producer_enabled = false
    kafka_producer_environment_overrides = ""
    kafka_producer_instances = 1
    kafka_producer_cpu_limit = "500m"
    kafka_producer_memory_limit = "1500"
    kafka_producer_jvm_memory_limit = "1024"

    secret_detector_enabled = false
    secret_detector_environment_overrides = ""
    secret_detector_kafka_threadcount = 1
    secret_detector_instances = 1
    secret_detector_secretsnotifications_email_from = ""
    secret_detector_secretsnotifications_email_host = ""
    secret_detector_secretsnotifications_email_subject = ""
    secret_detector_secretsnotifications_email_tos = ""
    secret_detector_secretsnotifications_whitelist_bucket = ""
    secret_detector_cpu_limit = "500m"
    secret_detector_memory_limit = "1500"
    secret_detector_jvm_memory_limit = "1024"
  }
}
# collectors config

variable "collector" {
  type = "map"
  default = {
    version = "08534fa97ffb723ef6f138bddecd9dfbce0b9dd2"
    kinesis_span_collector_instances = 1
    kinesis_span_collector_enabled = true
    kinesis_stream_region = ""
    kinesis_stream_name = ""
    kinesis_span_collector_sts_role_arn = ""
    kinesis_span_collector_environment_overrides = ""
    kinesis_span_collector_cpu_limit = "500m"
    kinesis_span_collector_memory_limit = "1500"
    kinesis_span_collector_jvm_memory_limit = "1024"
  }
}

# ui config
variable "ui" {
  type = "map"
  default = {
    enabled = true
    version = "c7be950888aef83fa1709c40a62b9ce68066b85b"
    instances = 1
    whitelisted_fields = ""
    enable_sso = false
    saml_callback_url = ""
    saml_entry_point = ""
    saml_issuer = ""
    session_secret = ""
    cpu_limit = "500m"
    memory_limit = "1500"
  }
}

#metrictank

variable "metrictank" {
  type = "map"
  default = {
    instances = 1
    environment_overrides = ""
    external_kafka_broker_hostname = ""
    external_kafka_broker_port = 1
    external_hostname = ""
    external_port = 1
    cpu_limit = "500m"
    memory_limit = "4000"
  }
}
