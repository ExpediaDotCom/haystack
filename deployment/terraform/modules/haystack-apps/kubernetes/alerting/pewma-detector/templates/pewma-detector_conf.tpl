pewma-detector {
  health.status.path = "/app/isHealthy"

  streams {
    application.id = "pewma-detector"
    bootstrap.servers = "${kafka_endpoint}"
  }
  topic = "pewma-metrics"

}
