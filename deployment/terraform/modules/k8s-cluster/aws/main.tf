locals {
  k8s_cluster_name = "${var.cluster["name"]}-k8s.${var.cluster["domain_name"]}"
  haystack_ui_cname = "${var.cluster["name"]}.${var.cluster["domain_name"]}"
  aws_nodes_subnet = "${element(split(",", var.cluster["aws_nodes_subnet"]),0)}"
}


data "aws_route53_zone" "haystack_dns_zone" {
  name = "${var.cluster["domain_name"]}"
}

data "aws_subnet" "haystack_node_subnet" {
  id = "${local.aws_nodes_subnet}"
}

module "kops" {
  source = "kops"
  k8s_version = "${var.kops_kubernetes["k8s_version"]}"
  aws_vpc_id = "${var.cluster["aws_vpc_id"]}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  masters_instance_type = "${var.kops_kubernetes["master_instance_type"]}"
  kops_executable_name = "${var.kops_executable_name}"
  app-nodes_instance_type = "${var.kops_kubernetes["app-nodes_instance_type"]}"
  app-nodes_instance_count = "${var.kops_kubernetes["app-nodes_instance_count"]}"
  monitoring-nodes_instance_type = "${var.kops_kubernetes["monitoring-nodes_instance_type"]}"
  monitoring-nodes_instance_count = "${var.kops_kubernetes["monitoring-nodes_instance_count"]}"
  s3_bucket_name = "${var.cluster["s3_bucket_name"]}"
  aws_hosted_zone_id = "${data.aws_route53_zone.haystack_dns_zone.id}"
  aws_zone = "${data.aws_subnet.haystack_node_subnet.availability_zone}"
  aws_nodes_subnet = "${local.aws_nodes_subnet}"
  aws_utilities_subnet = "${var.cluster["aws_utilities_subnet"]}"
  master_instance_volume = "${var.kops_kubernetes["master_instance_volume"]}"
  app-nodes_instance_volume = "${var.kops_kubernetes["app-nodes_instance_volume"]}"
  monitoring-nodes_instance_volume = "${var.kops_kubernetes["monitoring-nodes_instance_volume"]}"
}

module "security_groups" {
  source = "security-groups"
  aws_vpc_id = "${var.cluster["aws_vpc_id"]}"
  reverse_proxy_port = "${var.cluster["reverse_proxy_port"]}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  haystack_cluster_name = "${var.cluster["name"]}"
  graphite_node_port = "${var.graphite_node_port}"
}

module "iam_roles" {
  source = "iam-roles"
  aws_hosted_zone_id = "${data.aws_route53_zone.haystack_dns_zone.id}"
  s3_bucket_name = "${var.cluster["s3_bucket_name"]}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  haystack_cluster_name = "${var.cluster["name"]}"
}
module "asg" {
  source = "asg"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  s3_bucket_name = "${var.cluster["s3_bucket_name"]}"
  nodes_iam-instance-profile_arn = "${module.iam_roles.nodes_iam-instance-profile_arn}"
  app-nodes_instance_type = "${var.kops_kubernetes["app-nodes_instance_type"]}"
  app-nodes_instance_count = "${var.kops_kubernetes["app-nodes_instance_count"]}"
  monitoring-nodes_instance_type = "${var.kops_kubernetes["monitoring-nodes_instance_type"]}"
  monitoring-nodes_instance_count = "${var.kops_kubernetes["monitoring-nodes_instance_count"]}"
  nodes_security_groups = "${module.security_groups.node_security_group_ids}"
  aws_zone = "${data.aws_subnet.haystack_node_subnet.availability_zone}"
  masters_instance_type = "${var.kops_kubernetes["master_instance_type"]}"
  aws_ssh_key = "${var.cluster["aws_ssh_key"]}"
  aws_nodes_subnet = "${local.aws_nodes_subnet}"
  masters_security_groups = "${module.security_groups.master_security_group_ids}"
  masters_iam-instance-profile_arn = "${module.iam_roles.masters_iam-instance-profile_arn}"
  haystack_cluster_name = "${var.cluster["name"]}"
  master_instance_volume = "${var.kops_kubernetes["master_instance_volume"]}"
  app-nodes_instance_volume = "${var.kops_kubernetes["app-nodes_instance_volume"]}"
  monitoring-nodes_instance_volume = "${var.kops_kubernetes["monitoring-nodes_instance_volume"]}"
  nodes_ami = "${var.kops_kubernetes["node_ami"]}"
  masters_ami = "${var.kops_kubernetes["master_ami"]}"
}

module "elbs" {
  source = "elbs"
  elb_api_security_groups = "${module.security_groups.api-elb-security_group_ids}"
  aws_elb_subnet = "${var.cluster["aws_utilities_subnet"]}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  nodes_api_security_groups = "${module.security_groups.nodes-api-elb-security_group_ids}"
  reverse_proxy_port = "${var.cluster["reverse_proxy_port"]}"
  master-1_asg_id = "${module.asg.master-1_asg_id}"
  master-2_asg_id = "${module.asg.master-2_asg_id}"
  master-3_asg_id = "${module.asg.master-3_asg_id}"
  app-nodes_asg_id = "${module.asg.app-nodes_asg_id}"
  "monitoring-nodes_asg_id" = "${module.asg.monitoring-nodes_asg_id}"
  haystack_cluster_name = "${var.cluster["name"]}"
  monitoring_security_groups = "${module.security_groups.monitoring-elb-security_group_ids}"
  graphite_node_port = "${var.graphite_node_port}"
  aws_nodes_subnet = "${local.aws_nodes_subnet}"
}

module "route53" {
  source = "route53"
  master_elb_dns_name = "${module.elbs.master-elb-dns_name}"
  nodes_elb_dns_name = "${module.elbs.app-nodes-elb-dns_name}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  haystack_ui_cname = "${local.haystack_ui_cname}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  aws_hosted_zone_id = "${data.aws_route53_zone.haystack_dns_zone.id}"

}

resource "aws_eip" "eip" {
  vpc = true
}
