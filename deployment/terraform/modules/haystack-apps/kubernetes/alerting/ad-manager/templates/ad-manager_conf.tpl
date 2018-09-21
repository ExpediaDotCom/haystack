ad-manager {
  streams {
    application.id = "ad-manager"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde"
    JsonPojoClass = "com.expedia.adaptivealerting.core.data.MappedMetricData"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataTimestampExtractor"
  }
  factories {
    constant-detector = "com.expedia.adaptivealerting.anomdetect.constant.ConstantThresholdAnomalyDetectorFactory"
    cusum-detector = "com.expedia.adaptivealerting.anomdetect.control.cusum.CusumAnomalyDetectorFactory"
    ewma-detector = "com.expedia.adaptivealerting.anomdetect.ewma.EwmaAnomalyDetectorFactory"
    pewma-detector = "com.expedia.adaptivealerting.anomdetect.control.PewmaAnomalyDetectorFactory"
    rcf-detector = "com.expedia.adaptivealerting.anomdetect.randomcutforest.RandomCutForestAnomalyDetectorFactory"
  }

  health.status.path = "/app/isHealthy"
  inbound-topic = "mapped-metrics"
  outbound-topic = "anomalies"
}
