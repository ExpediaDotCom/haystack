module "cassandra" {
  source = "cassandra"
  replicas = "1"
  namespace = "${var.k8s_app_name_space}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  node_selecter_label = "${var.node_selecter_label}"
  memory_limit = "${var.memory_limit}"
  cpu_limit = "${var.cpu_limit}"
}

module "es" {
  source = "elasticsearch"
  replicas = "1"
  namespace = "${var.k8s_app_name_space}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  node_selecter_label = "${var.node_selecter_label}"
  memory_limit = "${var.memory_limit}"
  cpu_limit = "${var.cpu_limit}"
}



module "kafka" {
  source = "kafka"
  replicas = "1"
  namespace = "${var.k8s_app_name_space}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  kubectl_context_name = "${var.kubectl_context_name}"
  node_selecter_label = "${var.node_selecter_label}"
  memory_limit = "${var.memory_limit}"
  cpu_limit = "${var.cpu_limit}"
}
