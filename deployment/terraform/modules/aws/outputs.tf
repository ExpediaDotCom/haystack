output "k8s-cluster-name" {
  value = "${module.kaystack-k8s.cluster_name}"
}

output "k8s_logs_es_url" {
  value = "${module.kaystack-es.haystack_logs_es_url}"
}