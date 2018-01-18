locals {
  k8s_cluster_name = "${var.haystack_cluster_name}-k8s.${var.aws_domain_name}"
}


module "kops" {
  source = "kops"
  k8s_version = "${var.k8s_version}"
  aws_vpc_id = "${var.aws_vpc_id}"
  nodes_instance_count = "${var.node_instance_count}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  masters_instance_type = "${var.master_instance_type}"
  kops_executable_name = "${var.kops_executable_name}"
  nodes_instance_type = "${var.node_instance_type}"
  s3_bucket_name = "${var.s3_bucket_name}"
  aws_hosted_zone_id = "${var.aws_hosted_zone_id}"
  aws_zone = "${var.aws_zone}"
  aws_nodes_subnet = "${var.aws_nodes_subnet_ids}"
  aws_utilities_subnet = "${var.aws_utility_subnet_ids}"

}

module "security_groups" {
  source = "security-groups"
  aws_vpc_id = "${var.aws_vpc_id}"
  reverse_proxy_port = "${var.reverse_proxy_port}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}

module "iam_roles" {
  source = "iam-roles"
  aws_hosted_zone_id = "${var.aws_hosted_zone_id}"
  s3_bucket_name = "${var.s3_bucket_name}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}
module "asg" {
  source = "asg"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  s3_bucket_name = "${var.s3_bucket_name}"
  nodes_iam-instance-profile_arn = "${module.iam_roles.nodes_iam-instance-profile_arn}"
  nodes_instance_type = "${var.node_instance_type}"
  nodes_instance_count = "${var.node_instance_count}"
  nodes_security_groups = "${module.security_groups.node_security_group_ids}"
  aws_region = "${var.aws_region}"
  aws_zone = "${var.aws_zone}"
  masters_instance_type = "${var.master_instance_type}"
  aws_ssh_key = "${var.aws_ssh_key}"
  aws_nodes_subnet_ids = "${var.aws_nodes_subnet_ids}"
  masters_security_groups = "${module.security_groups.master_security_group_ids}"
  masters_iam-instance-profile_arn = "${module.iam_roles.masters_iam-instance-profile_arn}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}

module "elbs" {
  source = "elbs"
  elb_api_security_groups = "${module.security_groups.api-elb-security_group_ids}"
  aws_elb_subnet = "${var.aws_utility_subnet_ids}"
  aws_hosted_zone_id = "${var.aws_hosted_zone_id}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  nodes_api_security_groups = "${module.security_groups.nodes-api-elb-security_group_ids}"
  reverse_proxy_port = "${var.reverse_proxy_port}"
  "master-1_asg_id" = "${module.asg.master-1_asg_id}"
  "master-2_asg_id" = "${module.asg.master-2_asg_id}"
  "master-3_asg_id" = "${module.asg.master-2_asg_id}"
  nodes_asg_id = "${module.asg.nodes_asg_id}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}
resource "aws_eip" "eip" {
  vpc = true
}
