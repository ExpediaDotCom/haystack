output "cluster_name" {
  value = "${local.k8s_cluster_name}"
}

output "master_security_group_ids" {
  value = [
    "${module.k8s_security_groups.master_security_group_ids}"]
}

output "node_security_group_ids" {
  value = [
    "${module.k8s_security_groups.node_security_group_ids}"]
}


output "masters_role_name" {
  value = "${module.k8s_iam_roles.masters_role_name}"
}

output "masters_role_arn" {
  value = "${module.k8s_iam_roles.masters_role_arn}"
}

output "nodes_role_arn" {
  value = "${module.k8s_iam_roles.nodes_role_arn}"
}

output "nodes_role_name" {
  value = "${module.k8s_iam_roles.nodes_role_name}"
}

output "region" {
  value = "${var.k8s_aws_zone}"
}

output "vpc_id" {
  value = "${var.k8s_aws_vpc_id}"
}
