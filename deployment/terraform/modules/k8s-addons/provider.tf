provider "kubernetes" {
  config_context = "${var.k8s_cluster_name}"
  load_config_file = false
}

