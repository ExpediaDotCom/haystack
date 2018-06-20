metric-router {

  health.status.path = "/app/isHealthy"

  streams {
    application.id = "metric-router"
    bootstrap.servers = "${kafka_endpoint}"
  }
  topic = "mdm"
}
