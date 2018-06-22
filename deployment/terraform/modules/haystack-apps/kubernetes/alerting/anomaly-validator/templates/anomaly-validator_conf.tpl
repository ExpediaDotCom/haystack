anomaly-validator {
  health.status.path = "/app/isHealthy"

  streams {
      application.id = "anomaly-validator"
      default.value.serde = "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde"
      JsonPojoClass = "com.expedia.adaptivealerting.core.anomaly.AnomalyResult"
    }
    topic = "anomalies"

}

