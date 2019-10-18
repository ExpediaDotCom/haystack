output "cluster_name" {
  value = "${module.route53.cluster_name}"
}

output "external_graphite_hostname" {
  value = "${module.elbs.monitoring-nodes-elb-dns_name}"
}

output "external_graphite_cname" {
  value = "${module.route53.graphite_cname}"
}

output "nodes_iam-role_arn" {
  value = "${module.iam_roles.nodes_role_arn}"
}
