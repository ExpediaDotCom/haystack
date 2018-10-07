ad-mapper {
  streams {
    application.id = "ad-mapper"
    bootstrap.servers = "${kafka_endpoint}"
#    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.MetricDataSerde"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde"
    JsonPojoClass = "com.expedia.metrics.MetricData"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.serde.MetricDataTimestampExtractor"
  }
  health.status.path = "/app/isHealthy"
  model-service-uri-template = "${modelservice_uri_template}"
  inbound-topic = "aa-metrics"
  outbound-topic = "mapped-metrics"
}
