ad-manager {
  streams {
    application.id = "ad-manager"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde"
    JsonPojoClass = "com.expedia.adaptivealerting.core.data.MappedMetricData"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataTimestampExtractor"
  }

  detectors {
    aquila {
      factory = "com.expedia.adaptivealerting.anomdetect.aquila.AquilaFactory"
      config {
        uri = "${aquila_uri}"
      }
    }
    constant {
      factory = "com.expedia.adaptivealerting.anomdetect.BasicAnomalyDetectorFactory"
      config {
        detectorClass = "com.expedia.adaptivealerting.anomdetect.ConstantThresholdAnomalyDetector"
        region = "${models_region}"
        bucket = "${models_bucket}"
        folder = "constant-detector"
      }
    }
    cusum {
      factory = "com.expedia.adaptivealerting.anomdetect.BasicAnomalyDetectorFactory"
      config {
        detectorClass = "com.expedia.adaptivealerting.anomdetect.CusumAnomalyDetector"
        region = "${models_region}"
        bucket = "${models_bucket}"
        folder = "cusum-detector"
      }
    }
    ewma {
      factory = "com.expedia.adaptivealerting.anomdetect.BasicAnomalyDetectorFactory"
      config {
        detectorClass = "com.expedia.adaptivealerting.anomdetect.EwmaAnomalyDetector"
        region = "${models_region}"
        bucket = "${models_bucket}"
        folder = "ewma-detector"
      }
    }
    pewma {
      factory = "com.expedia.adaptivealerting.anomdetect.BasicAnomalyDetectorFactory"
      config {
        detectorClass = "com.expedia.adaptivealerting.anomdetect.PewmaAnomalyDetector"
        region = "${models_region}"
        bucket = "${models_bucket}"
        folder = "pewma-detector"
      }
    }
    rcf {
      factory = "com.expedia.adaptivealerting.anomdetect.rcf.RandomCutForestFactory"
      config {
      }
    }
  }

  health.status.path = "/app/isHealthy"
  inbound-topic = "mapped-metrics"
  outbound-topic = "anomalies"
}
