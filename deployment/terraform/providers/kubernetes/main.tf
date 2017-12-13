resource "kubernetes_namespace" "example" {
  metadata {
    name = "${var.k8s_app_name_space}"
  }
}
