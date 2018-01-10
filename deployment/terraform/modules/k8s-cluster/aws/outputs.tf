output "cluster_name" {
  value = "${module.k8s_elbs.nodes-elb-dns_name}"
}

output "cluster_endpoint" {
  value = "https://${module.k8s_elbs.api-elb-dns_name}"
}
output "kube_context" {
  value = "${local.k8s_cluster_name}"
}