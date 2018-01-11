output "cluster_name" {
  value = "${module.k8s_elbs.nodes-elb-dns_name}"
}
