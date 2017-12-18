//K8s apps to be deployed -- trace-indexer

module "span-timeseries-transformer" {
  source = "templates/haystack-app"
  app_name = "haystack-span-timeseries-transformer"
  image = "expediadotcom/haystack-span-timeseries-transformer:${var.span_timeseries_version}"
  replicas = "1"
}
