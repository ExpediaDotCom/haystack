output "cluster_name" {
  value = "${module.elbs.nodes-elb-dns_name}"
}
