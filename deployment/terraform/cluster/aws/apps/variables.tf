# configs to setup kubectl context in case just apps is deployed in another host
variable "haystack_cluster_name" {}
variable "s3_bucket_name" {}
variable "domain_name" {}
variable "kubectl_executable_name" {}

# ========================================
# Haystack
# ========================================

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

    backend_cpu_request = "500m"
    backend_cpu_limit = "1000m"
    backend_memory_request = "500"
    backend_memory_limit = "500"
    backend_jvm_memory_limit = "256"
    backend_environment_overrides = ""
  }
}

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
    version = "1.1"
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
    timeseries_aggregator_enable_metrics_sink = true
    timeseries_aggregator_histogram_max_value = 1800000
    timeseries_aggregator_histogram_precision = 2
    timeseries_aggregator_histogram_value_unit = "millis"
  }
}

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

variable "collector" {
  type = "map"
  default = {
    version = "1.1"
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

    http_span_collector_instances = 1
    http_span_collector_enabled = false
    http_span_collector_environment_overrides = ""
    http_span_collector_cpu_request = "500m"
    http_span_collector_cpu_limit = "2000m"
    http_span_collector_memory_request = "1536"
    http_span_collector_memory_limit = "1536"
    http_span_collector_jvm_memory_limit = "1024"
  }
}

variable "ui" {
  type = "map"
  default = {
    enabled = true
    version = "1.1"
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

variable "metrictank" {
  type = "map"
  default = {
    instances = 1
    environment_overrides = ""
    tag_support = "true"
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

variable "haystack-alerts" {
  type = "map"
  default = {
    enabled = false
    es_curator_enabled = false
    version = "f7f7e516ac0459fb4e4cf3df977a3255a729bed8"
    elasticsearch_template = "{\"template\": \"haystack-anomalies*\",\"settings\": {\"number_of_shards\": 1,\"index.mapping.ignore_malformed\": true,\"analysis\": {\"normalizer\": {\"lowercase_normalizer\": {\"type\": \"custom\",\"filter\": [\"lowercase\"]}}}},\"mappings\": {\"anomaly\": {\"_source\": {\"enabled\": true},\"_field_names\": {\"enabled\": false},\"_all\": {\"enabled\": false},\"properties\": {\"startTime\": {\"type\": \"long\",\"doc_values\": true}},\"dynamic_templates\": [{\"strings_as_keywords_1\": {\"match_mapping_type\": \"string\",\"mapping\": {\"type\": \"keyword\",\"normalizer\": \"lowercase_normalizer\",\"doc_values\": false,\"norms\": false}}}, {\"longs_disable_doc_norms\": {\"match_mapping_type\": \"long\",\"mapping\": {\"type\": \"long\",\"doc_values\": false,\"norms\": false}}}]}}}"
    alert-api_instances = 1
    alert-api_environment_overrides = ""
    alert-api_cpu_request = "500m"
    alert-api_cpu_limit = "2000m"
    alert-api_memory_request = "1536"
    alert-api_memory_limit = "1536"
    alert-api_jvm_memory_limit = "1024"
    subscription_service_hostname = "http://alert-manager-service.aa-apps.svc.cluster.local"
    subscription_service_port = 80

    anomaly-store_instances = 1
    anomaly-store_environment_overrides = ""
    anomaly-store_cpu_request = "500m"
    anomaly-store_cpu_limit = "2000m"
    anomaly-store_memory_request = "1536"
    anomaly-store_memory_limit = "1536"
    anomaly-store_jvm_memory_limit = "1024"
  }
}


# ========================================
# Adaptive Alerting
# ========================================

variable "alerting" {
  type = "map"
  default = {
    enabled = false
    version = "baf31dac6b41c83f871dfbe0fa1cc0892d8258b0"
  }
}

variable "modelservice" {
  type = "map"
  default = {
    enabled = false
    instances = 1
    image = "expediadotcom/adaptive-alerting-modelservice:baf31dac6b41c83f871dfbe0fa1cc0892d8258b0"
    image_pull_policy = "IfNotPresent"
    cpu_request = "500m"
    cpu_limit = "2000m"
    memory_request = "1024"
    memory_limit = "1024"
    jvm_memory_limit = "512"
    environment_overrides = ""
    db_endpoint = ""
  }
}

variable "ad-mapper" {
  type = "map"
  default = {
    enabled = false
    instances = 1
    image = "expediadotcom/adaptive-alerting-ad-mapper:baf31dac6b41c83f871dfbe0fa1cc0892d8258b0"
    image_pull_policy = "IfNotPresent"
    cpu_request = "500m"
    cpu_limit = "2000m"
    memory_request = "1024"
    memory_limit = "1024"
    jvm_memory_limit = "512"
    environment_overrides = ""
    modelservice_uri_template = "http://modelservice/api/detectors/search/findByMetricHash?hash=%s"
  }
}

variable "ad-manager" {
  type = "map"
  default = {
    enabled = false
    instances = 1
    image = "expediadotcom/adaptive-alerting-ad-manager:baf31dac6b41c83f871dfbe0fa1cc0892d8258b0"
    image_pull_policy = "IfNotPresent"
    cpu_request = "500m"
    cpu_limit = "2000m"
    memory_request = "1024"
    memory_limit = "1024"
    jvm_memory_limit = "512"
    environment_overrides = ""
    modelservice_uri_template = "http://modelservice/api/models/search/findLatestByDetectorUuid?uuid=%s"
  }
}

variable "mc-a2m-mapper" {
  type = "map"
  default = {
    enabled = false
    instances = 1
    image = "expediadotcom/adaptive-alerting-mc-a2m-mapper:baf31dac6b41c83f871dfbe0fa1cc0892d8258b0"
    image_pull_policy = "IfNotPresent"
    cpu_request = "500m"
    cpu_limit = "2000m"
    memory_request = "1024"
    memory_limit = "1024"
    jvm_memory_limit = "512"
    environment_overrides = ""
  }
}

variable "notifier" {
  type = "map"
  default = {
    enabled = false
    instances = 1
    image_pull_policy = "IfNotPresent"
    cpu_request = "100m"
    cpu_limit = "1000m"
    memory_request = "500"
    memory_limit = "500"
    jvm_memory_limit = "300"
    environment_overrides = ""
    webhook_url = ""
  }
}

# ========================================
# Alert Manager
# ========================================

variable "alert-manager-service" {
  type = "map"
  default = {
    enabled = false
    instances = 1
    version = "36606bf915f7c45d8b4f9ae6c8dfc4909b0117f6"
    image_pull_policy = "IfNotPresent"
    cpu_request = "100m"
    cpu_limit = "1000m"
    memory_request = "700"
    memory_limit = "700"
    jvm_memory_limit = "300"
    environment_overrides = ""
    es_urls = ""
    additional_email_validator_expression = ""
  }
}

variable "alert-manager-store" {
  type = "map"
  default = {
    enabled = false
    instances = 1
    version = "36606bf915f7c45d8b4f9ae6c8dfc4909b0117f6"
    image_pull_policy = "IfNotPresent"
    cpu_request = "100m"
    cpu_limit = "1000m"
    memory_request = "500"
    memory_limit = "500"
    jvm_memory_limit = "300"
    environment_overrides = ""
    es_urls = ""
  }
}

variable "alert-manager-notifier" {
  type = "map"
  default = {
    enabled = false
    instances = 1
    version = "36606bf915f7c45d8b4f9ae6c8dfc4909b0117f6"
    image_pull_policy = "IfNotPresent"
    cpu_request = "100m"
    cpu_limit = "1000m"
    memory_request = "700"
    memory_limit = "700"
    jvm_memory_limit = "300"
    environment_overrides = ""
    subscription_search_url = ""
    mail_from = ""
    rate_limit_enabled = false
    es_urls = ""
  }
}
