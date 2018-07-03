ewma-detector {
  health.status.path = "/app/isHealthy"

  streams {
    application.id = "ewma-detector"
    bootstrap.servers = "${kafka_endpoint}"
  }

  # TODO Renaming "topic" to "inbound-topic". Remove topic once transition is complete. [WLW]
  topic = "ewma-metrics"
  inbound-topic = "ewma-metrics"
}
