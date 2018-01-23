output "cluster_name" {
  value = "${module.route53.k8s_cluster_name}"
}

output "external_graphite_hostname" {
  value = "${module.elbs.monitoring-elb-dns_name}"
}