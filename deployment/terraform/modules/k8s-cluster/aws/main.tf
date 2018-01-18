locals {
  k8s_cluster_name = "${var.haystack_cluster_name}-k8s.${var.k8s_base_domain_name}"
}

module "kops" {
  source = "kops"
  k8s_version = "${var.k8s_version}"
  k8s_aws_vpc_id = "${var.k8s_aws_vpc_id}"
  k8s_node_instance_count = "${var.k8s_node_instance_count}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  k8s_master_instance_type = "${var.k8s_master_instance_type}"
  kops_executable_name = "${var.kops_executable_name}"
  k8s_node_instance_type = "${var.k8s_node_instance_type}"
  k8s_s3_bucket_name = "${var.k8s_s3_bucket_name}"
  k8s_hosted_zone_id = "${var.k8s_hosted_zone_id}"
  k8s_aws_zone = "${var.k8s_aws_zone}"
  k8s_aws_nodes_subnet = "${var.k8s_aws_nodes_subnet_ids}"
  k8s_aws_utilities_subnet = "${var.k8s_aws_utility_subnet_ids}"
}

module "security_groups" {
  source = "security-groups"
  k8s_vpc_id = "${var.k8s_aws_vpc_id}"
  reverse_proxy_port = "${var.reverse_proxy_port}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}

module "iam_roles" {
  source = "iam-roles"
  k8s_hosted_zone_id = "${var.k8s_hosted_zone_id}"
  k8s_s3_bucket_name = "${var.k8s_s3_bucket_name}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}
module "asg" {
  source = "asg"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  k8s_s3_bucket_name = "${var.k8s_s3_bucket_name}"
  nodes_iam-instance-profile_arn = "${module.iam_roles.nodes_iam-instance-profile_arn}"
  k8s_node_instance_type = "${var.k8s_node_instance_type}"
  k8s_node_instance_count = "${var.k8s_node_instance_count}"
  node_security_groups = "${module.security_groups.node_security_group_ids}"
  k8s_aws_region = "${var.k8s_aws_region}"
  k8s_aws_zone = "${var.k8s_aws_zone}"
  k8s_master_instance_type = "${var.k8s_master_instance_type}"
  k8s_aws_ssh_key = "${var.k8s_aws_ssh_key}"
  k8s_aws_nodes_subnet_ids = "${var.k8s_aws_nodes_subnet_ids}"
  master_security_groups = "${module.security_groups.master_security_group_ids}"
  master_iam-instance-profile_arn = "${module.iam_roles.masters_iam-instance-profile_arn}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}

module "elbs" {
  source = "elbs"
  k8s_elb_api_security_groups = "${module.security_groups.api-elb-security_group_ids}"
  k8s_elb_subnet = "${var.k8s_aws_utility_subnet_ids}"
  k8s_hosted_zone_id = "${var.k8s_hosted_zone_id}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  k8s_nodes_api_security_groups = "${module.security_groups.nodes-api-elb-security_group_ids}"
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
