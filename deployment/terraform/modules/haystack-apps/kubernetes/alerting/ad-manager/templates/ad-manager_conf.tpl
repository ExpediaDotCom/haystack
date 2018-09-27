ad-manager {
  streams {
    application.id = "ad-manager"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde"
    JsonPojoClass = "com.expedia.adaptivealerting.core.data.MappedMetricData"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataTimestampExtractor"
  }
  factories {
    region = "${models_region}"
    bucket = "${models_bucket}"
    by-type {
      aquila-detector = "com.expedia.adaptivealerting.anomdetect.aquila.AquilaFactory"
      constant-detector = "com.expedia.adaptivealerting.anomdetect.constant.ConstantThresholdFactory"
      cusum-detector = "com.expedia.adaptivealerting.anomdetect.cusum.CusumFactory"
      ewma-detector = "com.expedia.adaptivealerting.anomdetect.ewma.EwmaFactory"
      pewma-detector = "com.expedia.adaptivealerting.anomdetect.pewma.PewmaFactory"
      rcf-detector = "com.expedia.adaptivealerting.anomdetect.rcf.RandomCutForestFactory"
    }
  }

  health.status.path = "/app/isHealthy"
  inbound-topic = "mapped-metrics"
  outbound-topic = "anomalies"
}
