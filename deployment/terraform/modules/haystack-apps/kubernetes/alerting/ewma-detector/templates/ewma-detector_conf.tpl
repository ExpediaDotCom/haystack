ewma-detector {
  health.status.path = "/app/isHealthy"

  streams {
    application.id = "ewma-detector"
    bootstrap.servers = "${kafka_endpoint}"
  }
  topic = "ewma-metrics"

}
