//The config cluster should be set by default
provider "kubernetes" {
  config_context = "${var.k8s_cluster_name}"
}