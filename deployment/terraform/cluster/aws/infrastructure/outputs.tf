output "k8s_cluster_name" {
  value = "${module.haystack-k8s.cluster_name}"
}

output "k8s_app_namespace" {
  value = "${module.k8s-addons.k8s_app_namespace}"
}
