pewma-detector {
  health.status.path = "/app/isHealthy"

  streams {
    application.id = "pewma-detector"
    bootstrap.servers = "${kafka_endpoint}"
  }

  # TODO Renaming "topic" to "inbound-topic". Remove topic once transition is complete. [WLW]
  topic = "pewma-metrics"
  inbound-topic = "pewma-metrics"
}
