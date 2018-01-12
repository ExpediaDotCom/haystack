output "k8s_app_namespace" {
  value = "${local.k8s_app_namespace}"
  depends_on = ["null_resource.haystack_app_namespace"]
}