output "k8s-cluster-name" {
  value = "${module.haystack-k8s.cluster_name}"
}

output "k8s_logs_es_url" {
  value = "${module.haystack-es.haystack_logs_es_url}"
}