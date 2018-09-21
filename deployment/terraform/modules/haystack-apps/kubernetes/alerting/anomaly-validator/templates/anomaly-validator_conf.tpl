anomaly-validator {
  health.status.path = "/app/isHealthy"

  streams {
      application.id = "anomaly-validator"
      bootstrap.servers = "${kafka_endpoint}"
      default.value.serde = "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde"
      JsonPojoClass = "com.expedia.adaptivealerting.core.data.MappedMetricData"
      default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataTimestampExtractor"
      default.deserialization.exception.handler = "org.apache.kafka.streams.errors.LogAndContinueExceptionHandler"
    }

    # TODO Renaming "topic" to "inbound-topic". Remove topic once transition is complete. [WLW]
    topic = "anomalies"
    inbound-topic = "anomalies"
    outbound-topic = "alerts"

    investigation {
      endpoint = "${investigation_endpoint}"
    }
}
