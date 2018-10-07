ad-manager {
  streams {
    application.id = "ad-manager"
    bootstrap.servers = "${kafka_endpoint}"
    default.value.serde = "com.expedia.adaptivealerting.kafka.serde.JsonPojoSerde"
    JsonPojoClass = "com.expedia.adaptivealerting.core.data.MappedMetricData"
    default.timestamp.extractor = "com.expedia.adaptivealerting.kafka.serde.MappedMetricDataTimestampExtractor"
  }
  detectors {
    aquila-detector {
      factory = "com.expedia.adaptivealerting.anomdetect.aquila.AquilaAnomalyDetectorFactory"
      config {
        uri = "${aquila_uri}"
      }
    }
    constant-detector {
      factory = "com.expedia.adaptivealerting.anomdetect.BasicAnomalyDetectorFactory"
      config {
        detectorClass = "com.expedia.adaptivealerting.anomdetect.constant.ConstantThresholdAnomalyDetector"
        region = "us-west-2"
        bucket = "aa-models"
        folder = "constant-detector"
      }
    }
    cusum-detector {
      factory = "com.expedia.adaptivealerting.anomdetect.BasicAnomalyDetectorFactory"
      config {
        detectorClass = "com.expedia.adaptivealerting.anomdetect.cusum.CusumAnomalyDetector"
        region = "us-west-2"
        bucket = "aa-models"
        folder = "cusum-detector"
      }
    }
    ewma-detector {
      factory = "com.expedia.adaptivealerting.anomdetect.BasicAnomalyDetectorFactory"
      config {
        detectorClass = "com.expedia.adaptivealerting.anomdetect.ewma.EwmaAnomalyDetector"
        region = "us-west-2"
        bucket = "aa-models"
        folder = "ewma-detector"
      }
    }
    pewma-detector {
      factory = "com.expedia.adaptivealerting.anomdetect.BasicAnomalyDetectorFactory"
      config {
        detectorClass = "com.expedia.adaptivealerting.anomdetect.pewma.PewmaAnomalyDetector"
        region = "us-west-2"
        bucket = "aa-models"
        folder = "pewma-detector"
      }
    }
    rcf-detector {
      factory = "com.expedia.adaptivealerting.anomdetect.rcf.RandomCutForestAnomalyDetectorFactory"
      config {
      }
    }
  }

  health.status.path = "/app/isHealthy"
  inbound-topic = "mapped-metrics"
  outbound-topic = "anomalies"
}
