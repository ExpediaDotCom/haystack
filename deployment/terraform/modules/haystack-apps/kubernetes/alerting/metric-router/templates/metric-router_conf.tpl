metric-router {

  health.status.path = "/app/isHealthy"

  streams {
    application.id = "metric-router"
    bootstrap.servers = "${kafka_endpoint}"
  }

  # TODO Renaming "topic" to "inbound-topic". Remove topic once transition is complete. [WLW]
  topic = "mdm"
  inbound-topic = "mdm"
}
