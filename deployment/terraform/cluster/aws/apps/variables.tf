##configs to setup kubectl context in case just apps is deployed in another host
variable "haystack_cluster_name" {}
variable "s3_bucket_name" {}
variable "aws_domain_name" {}
variable "kubectl_executable_name" {}

# traces config
variable "traces" {
  type = "map"
  default = {
    enabled = true
    version = "1.0"
    indexer_instances = 1
    indexer_environment_overrides = ""
    indexer_cpu_request = "500m"
    indexer_cpu_limit = "2000m"
    indexer_memory_limit = "1536"
    indexer_memory_request = "1536"
    indexer_jvm_memory_limit = "1024"
    indexer_elasticsearch_template = "{\"template\":\"haystack-traces*\",\"settings\":{\"number_of_shards\":16,\"index.mapping.ignore_malformed\":true,\"analysis\":{\"normalizer\":{\"lowercase_normalizer\":{\"type\":\"custom\",\"filter\":[\"lowercase\"]}}}},\"aliases\":{\"haystack-traces\":{}},\"mappings\":{\"spans\":{\"_field_names\":{\"enabled\":false},\"_all\":{\"enabled\":false},\"_source\":{\"includes\":[\"traceid\"]},\"properties\":{\"traceid\":{\"enabled\":false},\"starttime\":{\"type\":\"long\",\"doc_values\": true},\"spans\":{\"type\":\"nested\",\"properties\":{\"servicename\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false},\"operationname\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false},\"starttime\":{\"enabled\":false}}}},\"dynamic_templates\":[{\"strings_as_keywords_1\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false}}},{\"longs_disable_doc_norms\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"doc_values\":false,\"norms\":false}}}]}}}"

    reader_instances = 1
    reader_environment_overrides = ""
    reader_cpu_request = "500m"
    reader_cpu_limit = "2000m"
    reader_memory_request = "1536"
    reader_memory_limit = "1536"
    reader_jvm_memory_limit = "1024"
  }
}

# service-graph config
variable "service-graph" {
  type = "map"
  default = {
    enabled = true
    version = "1.0"
    metricpoint_encoder_type = "base64"
    node_finder_instances = 1
    node_finder_environment_overrides = ""
    node_finder_cpu_request = "500m"
    node_finder_cpu_limit = "2000m"
    node_finder_memory_limit = "1536"
    node_finder_memory_request = "1536"
    node_finder_jvm_memory_limit = "1024"
    collect_tags = "[]"

    graph_builder_instances = 1
    graph_builder_environment_overrides = ""
    graph_builder_cpu_request = "500m"
    graph_builder_cpu_limit = "2000m"
    graph_builder_memory_request = "1536"
    graph_builder_memory_limit = "1536"
    graph_builder_jvm_memory_limit = "1024"
  }
}

variable "trends" {
  type = "map"
  default = {
    enabled = true
    version = "1.0"
    metricpoint_encoder_type = "base64"
    span_timeseries_transformer_instances = 1
    span_timeseries_transformer_cpu_request = "500m"
    span_timeseries_transformer_cpu_limit = "2000m"
    span_timeseries_transformer_memory_request = "1536"
    span_timeseries_transformer_memory_limit = "1536"
    span_timeseries_transformer_jvm_memory_limit = "1024"
    span_timeseries_transformer_environment_overrides = ""

    timeseries_aggregator_instances = 1
    timeseries_aggregator_environment_overrides = ""
    timeseries_aggregator_cpu_request = "500m"
    timeseries_aggregator_cpu_limit = "2000m"
    timeseries_aggregator_memory_request = "1536"
    timeseries_aggregator_memory_limit = "1536"
    timeseries_aggregator_jvm_memory_limit = "1024"
  }
}

# pipes config

variable "pipes" {
  type = "map"
  default = {
    version = "a20a8087f5ddc3fbf1a1c72dcff840608accadbf"

    firehose_kafka_threadcount = 1
    firehose_writer_enabled = false
    firehose_writer_environment_overrides = ""
    firehose_writer_firehose_initialretrysleep = 10
    firehose_writer_firehose_maxretrysleep = 3000
    firehose_writer_firehose_signingregion = ""
    firehose_writer_firehose_streamname = ""
    firehose_writer_firehose_url = ""
    firehose_writer_haystack_kafka_fromtopic = ""
    firehose_writer_instances = 1
    firehose_writer_cpu_request = "500m"
    firehose_writer_cpu_limit = "2000m"
    firehose_writer_memory_request = "1536"
    firehose_writer_memory_limit = "1536"
    firehose_writer_jvm_memory_limit = "1024"

    http_poster_enabled = false
    http_poster_environment_overrides = ""
    http_poster_httppost_pollpercent = ""
    http_poster_httppost_url = ""
    http_poster_instances = 1
    http_poster_cpu_request = "500m"
    http_poster_cpu_limit = "2000m"
    http_poster_memory_request = "1536"
    http_poster_memory_limit = "1536"
    http_poster_jvm_memory_limit = "1024"

    json_transformer_enabled = false
    json_transformer_environment_overrides = ""
    json_transformer_instances = 1
    json_transformer_cpu_request = "500m"
    json_transformer_cpu_limit = "2000m"
    json_transformer_memory_request = "1536"
    json_transformer_memory_limit = "1536"
    json_transformer_jvm_memory_limit = "1024"

    kafka_producer_enabled = false
    kafka_producer_environment_overrides = ""
    kafka_producer_instances = 1
    kafka_producer_cpu_request = "500m"
    kafka_producer_cpu_limit = "2000m"
    kafka_producer_memory_request = "1536"
    kafka_producer_memory_limit = "1536"
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
    secret_detector_cpu_request = "500m"
    secret_detector_cpu_limit = "2000m"
    secret_detector_memory_request = "1536"
    secret_detector_memory_limit = "1536"
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
    kinesis_span_collector_cpu_request = "500m"
    kinesis_span_collector_cpu_limit = "2000m"
    kinesis_span_collector_memory_request = "1536"
    kinesis_span_collector_memory_limit = "1536"
    kinesis_span_collector_jvm_memory_limit = "1024"
  }
}

# ui config
variable "ui" {
  type = "map"
  default = {
    enabled = true
    version = "1.0"
    instances = 1
    whitelisted_fields = ""
    enable_sso = false
    saml_callback_url = ""
    saml_entry_point = ""
    saml_issuer = ""
    session_secret = ""
    cpu_request = "500m"
    cpu_limit = "2000m"
    memory_request = "1536"
    memory_limit = "1536"
    metricpoint_encoder_type = "base64"
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
    cpu_request = "500m"
    cpu_limit = "2000m"
    memory_request = "4096"
    memory_limit = "4096"
  }
}

variable "alerting" {
  type = "map"
  default = {
    enabled = false
    version = "02da63dfd5a559c9048b6e83d205d49da3af25f2"
  }
}

variable "modelservice" {
  type = "map"
  default = {
    enabled = false
    modelservice_instances = 1
    modelservice_cpu_request = "100m"
    modelservice_cpu_limit = "1000m"
    modelservice_memory_request = "250"
    modelservice_memory_limit = "250"
    modelservice_jvm_memory_limit = "200"
    modelservice_environment_overrides = ""
    modelservice_db_endpoint= ""
  }
}

variable "ad-mapper" {
  type = "map"
  default = {
    enabled = false
    ad_mapper_instances = 1
    ad_mapper_cpu_request = "500m"
    ad_mapper_cpu_limit = "2000m"
    ad_mapper_memory_request = "1024"
    ad_mapper_memory_limit = "1024"
    ad_mapper_jvm_memory_limit = "512"
    ad_mapper_environment_overrides = ""
    ad_mapper_modelservice_uri_template = "http://modelservice.aa-apps/api/models/search/findByMetricHash?hash=%s"
  }
}

variable "ad-manager" {
  type = "map"
  default = {
    enabled = false
    ad_manager_instances = 1
    ad_manager_cpu_request = "500m"
    ad_manager_cpu_limit = "2000m"
    ad_manager_memory_request = "1024"
    ad_manager_memory_limit = "1024"
    ad_manager_jvm_memory_limit = "512"
    ad_manager_environment_overrides = ""
  }
}

variable "anomaly-validator" {
  type = "map"
  default = {
    enabled = false
    anomaly_validator_instances = 1
    anomaly_validator_cpu_request = "500m"
    anomaly_validator_cpu_limit = "2000m"
    anomaly_validator_memory_request = "1024"
    anomaly_validator_memory_limit = "1024"
    anomaly_validator_jvm_memory_limit = "512"
    anomaly_validator_environment_overrides = ""
    anomaly_validator_investigation_endpoint = ""
  }
}

variable "aquila-trainer" {
  type = "map"

  # I removed the app name from the keys here, as the keys are already app-scoped.
  # It's easier to use this as a template for future apps. Please consider adopting
  # this approach for the other apps. [WLW]
  default = {
    enabled = false
    instances = 1

    image = "expediadotcom/aquila-trainer:f72f754b8e20a96491ac3652877cd38b69b8c798"
    image_pull_policy = "IfNotPresent"

    cpu_request = "500m"
    cpu_limit = "2000m"
    memory_request = "1024"
    memory_limit = "1024"
    jvm_memory_limit = "512"
    environment_overrides = ""
  }
}

variable "aquila-detector" {
  type = "map"

  default = {
    enabled = false
    instances = 1

    image = "expediadotcom/aquila-detector:f72f754b8e20a96491ac3652877cd38b69b8c798"
    image_pull_policy = "IfNotPresent"

    cpu_request = "500m"
    cpu_limit = "2000m"
    memory_request = "1024"
    memory_limit = "1024"
    jvm_memory_limit = "512"
    environment_overrides = ""
  }
}
