module "cassandra" {
  source = "cassandra"
  replicas = "1"
  namespace = "${var.k8s_app_name_space}"
}

module "es" {
  source = "elasticsearch"
  replicas = "1"
  namespace = "${var.k8s_app_name_space}"
}



module "kafka" {
  source = "kafka"
  replicas = "1"
  namespace = "${var.k8s_app_name_space}"
}
