anomaly-validator {
  health.status.path = "/app/isHealthy"

  streams {
      application.id = "anomaly-validator"
      bootstrap.servers = "${kafka_endpoint}"
      default.value.serde = "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde"
      JsonPojoClass = "com.expedia.adaptivealerting.core.anomaly.AnomalyResult"
    }

    # TODO Renaming "topic" to "inbound-topic". Remove topic once transition is complete. [WLW]
    topic = "anomalies"
    inbound-topic = "anomalies"

    investigation {
      endpoint = "${investigation_endpoint}"
    }
}
