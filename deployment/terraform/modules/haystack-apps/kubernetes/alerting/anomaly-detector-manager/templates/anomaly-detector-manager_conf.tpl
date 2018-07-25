anomaly-detector-manager {
  streams {
    application.id = "anomaly-detector-manager"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde"
    JsonPojoClass = "com.expedia.adaptivealerting.core.data.MappedMpoint"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.serde.MappedMpointTimestampExtractor"
  }
  factories {
    constant-detector = "com.expedia.adaptivealerting.anomdetect.ConstantThresholdAnomalyDetectorFactory"
    cusum-detector = "com.expedia.adaptivealerting.anomdetect.CusumAnomalyDetectorFactory"
    ewma-detector = "com.expedia.adaptivealerting.anomdetect.EwmaAnomalyDetectorFactory"
    pewma-detector = "com.expedia.adaptivealerting.anomdetect.PewmaAnomalyDetectorFactory"
    rcf-detector = "com.expedia.adaptivealerting.anomdetect.randomcutforest.RandomCutForestAnomalyDetectorFactory"
  }

  health.status.path = "/app/isHealthy"
  inbound-topic = "mapped-metrics"
  outbound-topic = "anomalies"
}
