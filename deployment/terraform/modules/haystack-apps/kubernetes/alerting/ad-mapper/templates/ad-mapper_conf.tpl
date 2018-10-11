ad-mapper {
  streams {
    application.id = "ad-mapper"
    bootstrap.servers = "${kafka_endpoint}"

    # IMPORTANT Until this is configurable on a per-environment basis, this needs to be MetricDataSerde.
    # While JsonPojoSerde is great for local development, it breaks test and prod. [WLW]
    # TODO Make this configurable on a per-environment basis.
#    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.MetricDataSerde"

    JsonPojoClass = "com.expedia.metrics.MetricData"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.serde.MetricDataTimestampExtractor"
  }
  health.status.path = "/app/isHealthy"
  model-service-uri-template = "${modelservice_uri_template}"
  inbound-topic = "aa-metrics"
  outbound-topic = "mapped-metrics"
}
