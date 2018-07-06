anomaly-detector-mapper {
  streams {
    application.id = "anomaly-detector-mapper"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde"
    JsonPojoClass = "com.expedia.adaptivealerting.core.data.Mpoint"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.serde.MpointTimestampExtractor"
  }
  factories {
    constant-detector = "com.expedia.adaptivealerting.anomdetect.ConstantThresholdAnomalyDetectorFactory"
    cusum-detector = "com.expedia.adaptivealerting.anomdetect.CusumAnomalyDetectorFactory"
    ewma-detector = "com.expedia.adaptivealerting.anomdetect.EwmaAnomalyDetectorFactory"
    pewma-detector = "com.expedia.adaptivealerting.anomdetect.PewmaAnomalyDetectorFactory"
    rcf-detector = "com.expedia.adaptivealerting.anomdetect.randomcutforest.RandomCutForestAnomalyDetectorFactory"
  }

  # We're using "mdm-temp" until we consolidate the metric point representations. [WLW]
  inbound-topic = "mdm-temp"
  outbound-topic = "mapped-metrics"

  health.status.path = "/app/isHealthy"
}
