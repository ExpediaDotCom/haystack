constant-detector {
  health.status.path = "/app/isHealthy"

  streams {
    application.id = "constant-detector"
    bootstrap.servers = "${kafka_endpoint}"
  }
  topic = "constant-metrics"

}
