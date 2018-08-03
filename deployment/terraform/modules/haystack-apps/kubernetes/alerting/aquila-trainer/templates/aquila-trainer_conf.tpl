repositories {
  datasets {
    class = "com.expedia.adaptivealerting.aws.core.data.repo.S3MetricDataRepo"
    region = "us-west-2"
    bucket.name = "aa-datasets"
  }
  models {
    class = "com.expedia.aquila.repo.s3.S3DetectorModelRepo"
    region = "us-west-2"
    bucket.name = "aa-models"
  }
}
