module "es" {
  source = "../templates/haystack-app"
  app_name = "elasticsearch"
  image = "${var.es_docker_image}"
  replicas = "1"
  create_service = true
  service_port = "9200"
  container_port = "9200"
  enabled = "${var.enabled}"
}

module "cassandra" {
  source = "../templates/haystack-app"
  app_name = "cassandra"
  image = "${var.cassandra_docker_image}"
  replicas = "1"
  create_service = true
  service_port = "9042"
  container_port = "9042"
  enabled = "${var.enabled}"
}

module "zookeeper" {
  source = "../templates/haystack-app"
  app_name = "zookeeper"
  image = "${var.zookeeper_docker_image}"
  replicas = "1"
  create_service = true
  service_port = "2181"
  container_port = "2181"
  enabled = "${var.enabled}"
}

module "kafka" {
  source = "../templates/haystack-app"
  app_name = "kafka"
  image = "${var.kafka_docker_image}"
  replicas = "1"
  create_service = true
  service_port = "9092"
  container_port = "9092"
  enabled = "${var.enabled}"
}
module "metrictank" {
  source = "../templates/haystack-app"
  app_name = "metrictank"
  image = "${var.metrictank_docker_image}"
  replicas = "1"
  create_service = true
  service_port = "6060"
  container_port = "6060"
  enabled = "${var.enabled}"
}