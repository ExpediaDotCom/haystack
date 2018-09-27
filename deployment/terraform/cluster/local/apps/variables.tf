variable "kubectl_executable_name" {}

variable "haystack_cluster_name" {
  default = "haystack"
}

# traces config
variable "traces" {
  type = "map"
  default = {
    enabled = true
    version = "1.0"
    indexer_instances = 1
    indexer_environment_overrides = ""
    indexer_cpu_request = "100m"
    indexer_cpu_limit = "1000m"
    indexer_memory_request = "250"
    indexer_memory_limit = "250"
    indexer_jvm_memory_limit = "200"
    indexer_elasticsearch_template = "{\"template\":\"haystack-traces*\",\"settings\":{\"number_of_shards\":16,\"index.mapping.ignore_malformed\":true,\"analysis\":{\"normalizer\":{\"lowercase_normalizer\":{\"type\":\"custom\",\"filter\":[\"lowercase\"]}}}},\"aliases\":{\"haystack-traces\":{}},\"mappings\":{\"spans\":{\"_field_names\":{\"enabled\":false},\"_all\":{\"enabled\":false},\"_source\":{\"includes\":[\"traceid\"]},\"properties\":{\"traceid\":{\"enabled\":false},\"starttime\":{\"type\":\"long\",\"doc_values\": true},\"spans\":{\"type\":\"nested\",\"properties\":{\"servicename\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false},\"operationname\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false},\"starttime\":{\"enabled\":false}}}},\"dynamic_templates\":[{\"strings_as_keywords_1\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false}}},{\"longs_disable_doc_norms\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"doc_values\":false,\"norms\":false}}}]}}}"

    reader_instances = 1
    reader_environment_overrides = ""
    reader_cpu_request = "100m"
    reader_cpu_limit = "1000m"
    reader_memory_request = "250"
    reader_memory_limit = "250"
    reader_jvm_memory_limit = "200"
  }
}

variable "trends" {
  type = "map"
  default = {
    enabled = true
    version = "1.0"
    metricpoint_encoder_type = "base64"
    span_timeseries_transformer_instances = 1
    span_timeseries_transformer_cpu_request = "100m"
    span_timeseries_transformer_cpu_limit = "1000m"
    span_timeseries_transformer_memory_request = "250"
    span_timeseries_transformer_memory_limit = "250"
    span_timeseries_transformer_jvm_memory_limit = "200"
    span_timeseries_transformer_environment_overrides = ""

    timeseries_aggregator_instances = 1
    timeseries_aggregator_environment_overrides = ""
    timeseries_aggregator_cpu_request = "100m"
    timeseries_aggregator_cpu_limit = "1000m"
    timeseries_aggregator_memory_request = "250"
    timeseries_aggregator_memory_limit = "250"
    timeseries_aggregator_jvm_memory_limit = "200"
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
    firehose_writer_firehose_initialretrysleep = 1
    firehose_writer_firehose_maxretrysleep = ""
    firehose_writer_firehose_signingregion = ""
    firehose_writer_firehose_streamname = ""
    firehose_writer_firehose_totopic = ""
    firehose_writer_firehose_url = ""
    firehose_writer_haystack_kafka_fromtopic = ""
    firehose_writer_instances = 1
    firehose_writer_cpu_request = "100m"
    firehose_writer_cpu_limit = "500m"
    firehose_writer_memory_request = "250"
    firehose_writer_memory_limit = "250"
    firehose_writer_jvm_memory_limit = "200"

    http_poster_enabled = false
    http_poster_environment_overrides = ""
    http_poster_httppost_pollpercent = ""
    http_poster_httppost_url = ""
    http_poster_instances = 1
    http_poster_cpu_request = "100m"
    http_poster_cpu_limit = "500m"
    http_poster_memory_request = "250"
    http_poster_memory_limit = "250"
    http_poster_jvm_memory_limit = "200"

    json_transformer_enabled = false
    json_transformer_environment_overrides = ""
    json_transformer_instances = 1
    json_transformer_cpu_request = "100m"
    json_transformer_cpu_limit = "500m"
    json_transformer_memory_request = "250"
    json_transformer_memory_limit = "250"
    json_transformer_jvm_memory_limit = "200"

    kafka_producer_enabled = false
    kafka_producer_environment_overrides = ""
    kafka_producer_instances = 1
    kafka_producer_cpu_request = "100m"
    kafka_producer_cpu_limit = "500m"
    kafka_producer_memory_request = "250"
    kafka_producer_memory_limit = "250"
    kafka_producer_jvm_memory_limit = "200"

    secret_detector_enabled = false
    secret_detector_environment_overrides = ""
    secret_detector_instances = 1
    secret_detector_kafka_threadcount = 1
    secret_detector_secretsnotifications_email_from = ""
    secret_detector_secretsnotifications_email_host = ""
    secret_detector_secretsnotifications_email_subject = ""
    secret_detector_secretsnotifications_email_tos = ""
    secret_detector_secretsnotifications_whitelist_bucket = ""
    secret_detector_cpu_request = "100m"
    secret_detector_cpu_limit = "500m"
    secret_detector_memory_request = "250"
    secret_detector_memory_limit = "250"
    secret_detector_jvm_memory_limit = "200"
  }
}

# collectors config
variable "collector" {
  type = "map"
  default = {
    version = "08534fa97ffb723ef6f138bddecd9dfbce0b9dd2"
    kinesis_span_collector_instances = 1
    kinesis_span_collector_enabled = false
    kinesis_stream_region = ""
    kinesis_stream_name = ""
    kinesis_span_collector_sts_role_arn = ""
    kinesis_span_collector_environment_overrides = ""
    kinesis_span_collector_cpu_request = "100m"
    kinesis_span_collector_cpu_limit = "1000m"
    kinesis_span_collector_memory_request = "250"
    kinesis_span_collector_memory_limit = "250"
    kinesis_span_collector_jvm_memory_limit = "200"
  }

}

# service-graph config
variable "service-graph" {
  type = "map"
  default = {
    enabled = false
    version = "1.0"
    metricpoint_encoder_type = "base64"
    node_finder_instances = 1
    node_finder_environment_overrides = ""
    node_finder_cpu_request = "100m"
    node_finder_cpu_limit = "1000m"
    node_finder_memory_request = "250"
    node_finder_memory_limit = "250"
    node_finder_jvm_memory_limit = "200"
    collect_tags = "[]"

    graph_builder_instances = 1
    graph_builder_environment_overrides = ""
    graph_builder_cpu_request = "100m"
    graph_builder_cpu_limit = "1000m"
    graph_builder_memory_request = "250"
    graph_builder_memory_limit = "250"
    graph_builder_jvm_memory_limit = "200"
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
    cpu_request = "100m"
    cpu_limit = "1000m"
    memory_request = "250"
    memory_limit = "250"
    metricpoint_encoder_type = "base64"
  }
}

#metrictank
variable "metrictank" {
  type = "map"
  default = {
    instances = 1,
    environment_overrides = ""
    external_kafka_broker_hostname = ""
    external_kafka_broker_port = 9092,
    external_hostname = ""
    external_port = 6060
    cpu_request = "100m"
    cpu_limit = "1000m"
    memory_request = "250"
    memory_limit = "250"
  }
}


# ========================================
# Adaptive Alerting
# ========================================

variable "alerting" {
  type = "map"
  default = {
    version = "c55b786a7fc6614c92fc40bda3be125ae828a445"
  }
}

variable "modelservice" {
  type = "map"
  default = {
    enabled = false
    modelservice_instances = 1
    modelservice_cpu_request = "100m"
    modelservice_cpu_limit = "1000m"
    modelservice_memory_request = "500"
    modelservice_memory_limit = "500"
    modelservice_jvm_memory_limit = "300"
    modelservice_environment_overrides = ""
    modelservice_db_endpoint = ""
  }
}

variable "ad-mapper" {
  type = "map"
  default = {
    enabled = false
    ad_mapper_instances = 1
    ad_mapper_cpu_request = "100m"
    ad_mapper_cpu_limit = "1000m"
    ad_mapper_memory_request = "250"
    ad_mapper_memory_limit = "250"
    ad_mapper_jvm_memory_limit = "200"
    ad_mapper_environment_overrides = ""
    ad_mapper_modelservice_uri_template = "http://modelservice/api/models/search/findByMetricHash?hash=%s"
  }
}

variable "ad-manager" {
  type = "map"
  default = {
    enabled = false
    ad_manager_models_region = "us-west-2"
    ad_manager_models_bucket = "aa-models"
    ad_manager_instances = 1
    ad_manager_cpu_request = "100m"
    ad_manager_cpu_limit = "1000m"
    ad_manager_memory_request = "250"
    ad_manager_memory_limit = "250"
    ad_manager_jvm_memory_limit = "200"
    ad_manager_environment_overrides = ""
  }
}

variable "anomaly-validator" {
  type = "map"
  default = {
    enabled = false
    anomaly_validator_instances = 1
    anomaly_validator_cpu_request = "100m"
    anomaly_validator_cpu_limit = "1000m"
    anomaly_validator_memory_request = "250"
    anomaly_validator_memory_limit = "250"
    anomaly_validator_jvm_memory_limit = "200"
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

    # This allows us to use Minikube-local images.
    # https://stackoverflow.com/questions/42564058/how-to-use-local-docker-images-with-minikube
#    image = "aquila-trainer:latest"
#    image_pull_policy = "Never"
    image_pull_policy = "IfNotPresent"

    cpu_request = "100m"
    cpu_limit = "1000m"
    memory_request = "500"
    memory_limit = "500"
    jvm_memory_limit = "300"
    environment_overrides = ""
  }
}

variable "aquila-detector" {
  type = "map"
  default = {
    enabled = false
    instances = 1

#    image = "aquila-detector:latest"
#    image_pull_policy = "Never"
    image_pull_policy = "IfNotPresent"

    cpu_request = "100m"
    cpu_limit = "1000m"
    memory_request = "500"
    memory_limit = "500"
    jvm_memory_limit = "300"
    environment_overrides = ""
  }
}
