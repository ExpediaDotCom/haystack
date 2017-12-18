module "span-timeseries-transformer" {
  source = "../templates/haystack-app"
  app_name = "haystack-span-timeseries-transformer"
  image = "expediadotcom/haystack-span-timeseries-transformer:${var.trends_version}"
  replicas = "1"
}
module "timeseries-transformer" {
  source = "../templates/haystack-app"
  app_name = "haystack-timeseries-aggregator"
  image = "expediadotcom/haystack-timeseries-aggregator:${var.trends_version}"
  replicas = "1"
}
module "trace-reader" {
  source = "../templates/haystack-app"
  app_name = "haystack-trace-reader"
  image = "expediadotcom/haystack-trace-reader:${var.traces_version}"
  replicas = "1"
}
module "trace-indexer" {
  source = "../templates/haystack-app"
  app_name = "haystack-trace-indexer"
  image = "expediadotcom/haystack-trace-indexer:${var.traces_version}"
  replicas = "1"
  enabled = true
}
module "pipes-json-transformer" {
  source = "../templates/haystack-app"
  app_name = "haystack-pipes-json-transformer"
  image = "expediadotcom/haystack-pipes-json-transformer:${var.pipes_version}"
  replicas = "1"
}
module "pipes-kafka-producer" {
  source = "../templates/haystack-app"
  app_name = "haystack-pipes-kafka-producer"
  image = "expediadotcom/haystack-pipes-kafka-producer:${var.pipes_version}"
  replicas = "1"
}

module "ui" {
  source = "../templates/haystack-app"
  app_name = "haystack-ui"
  image = "expediadotcom/haystack-ui:${var.ui_version}"
  replicas = "1"
  create_service = true
}