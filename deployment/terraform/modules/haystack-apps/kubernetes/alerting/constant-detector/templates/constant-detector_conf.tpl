constant-detector {
  health.status.path = "/app/isHealthy"

  streams {
    application.id = "constant-detector"
    bootstrap.servers = "${kafka_endpoint}"
  }

  # TODO Renaming "topic" to "inbound-topic". Remove topic once transition is complete. [WLW]
  topic = "constant-metrics"
  inbound-topic = "constant-metrics"
}
