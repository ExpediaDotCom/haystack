module "kubernetes-addons" {
  source = "../../modules/kubernetes/addons"
  k8s_aws_region = "${var.k8s_aws_region}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  k8s_logs_es_url = "${var.k8s_logs_es_url}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
}
resource "kubernetes_namespace" "example" {
  metadata {
    name = "${var.k8s_app_name_space}"
  }
}

