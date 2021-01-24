locals {
  nodes_api_sgs = "${concat("${module.security_groups.nodes_elb_security_groups}","${var.cluster["external_sgs"]}")}"
  aws_nodes_subnet = "${split(",", var.cluster["aws_nodes_subnet"])}"
}


module "security_groups" {
  source = "security-groups"
  aws_vpc_id = "${var.cluster["aws_vpc_id"]}"
  reverse_proxy_port = "${var.cluster["reverse_proxy_port"]}"
  cluster_name = "${var.cluster["name"]}"
  role_name = "${var.role_name}"
}

module "iam_roles" {
  source = "iam-roles"
  cluster_name = "${var.cluster["name"]}"
}


module "aws_eks_cluster" {
  source = "eks"
  cluster_name = "${var.cluster["name"]}"
  role_arn = "${module.iam_roles.masters_role_arn}"
  master_security_group_ids = "${module.security_groups.master_security_group_ids}"
  aws_subnet_ids = "${var.cluster["reverse_proxy_port"]}"
  depends_on = [
    "${module.iam_roles.aws_iam_role_policy_attachment_master-AmazonEKSClusterPolicy_arn}",
    "${module.iam_roles.aws_iam_role_policy_attachment_master-AmazonEKSServicePolicy_arn}"]
}

module "kubectl-config" {
  source = "kubectl-config"
  cluster_name = "${var.cluster["name"]}"
  nodes_role_arn = "${module.iam_roles.nodes_role_arn}"
  aws_eks_cluster_endpoint = "${module.aws_eks_cluster.endpoint}"
  certificate-authority-data = "${module.aws_eks_cluster.certificate-authority-data}"
  aws_iam_authenticator_executable_name = "${var.aws_iam_authenticator_executable_name}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
}

module "node_asg" {
  source = "nodes_asg"
  cluster_name = "${var.cluster["name"]}"
  aws_region = "${var.cluster["aws_region"]}"
  aws_eks_cluster_endpoint = "${module.aws_eks_cluster.endpoint}"
  nodes_iam-instance-profile_arn = "${module.iam_roles.nodes_iam-instance-profile_arn}"
  certificate-authority-data = "${module.aws_eks_cluster.certificate-authority-data}"
  nodes_security_group_ids = "${module.security_groups.node_security_group_ids}"
  aws_subnet_ids = "${local.aws_nodes_subnet}"
  app-node_instance_type = "${var.eks_kubernetes["app-nodes_instance_type"]}"
  app-node_min_instance_count = "${var.eks_kubernetes["app-nodes_min_instance_count"]}"
  app-node_max_instance_count = "${var.eks_kubernetes["app-nodes_max_instance_count"]}"
  app-node_ami = "${var.eks_kubernetes["worker-node_ami"]}"
  aws_ssh_key = "${var.cluster["aws_ssh_key"]}"
  role_name = "${var.role_name}"
  monitoring-node_instance_type = "${var.eks_kubernetes["monitoring-node_instance_type"]}"
  monitoring-node_instance_count = "${var.eks_kubernetes["monitoring-node_instance_count"]}"
}


module "node_elb" {
  source = "nodes_elb"
  cluster_name = "${var.cluster["name"]}"
  nodes_api_security_groups = "${local.nodes_api_sgs}"
  reverse_proxy_port = "${var.cluster["reverse_proxy_port"]}"
  aws_elb_subnet = "${var.cluster["aws_utility_subnet"]}"
  role_name = "${var.role_name}"
  "app-nodes_asg_id" = "${module.node_asg.app-nodes_asg_id}"
  nodes_api_ssl_cert = "${var.cluster["nodes_api_ssl_cert"]}"
}


module "route53" {
  source = "route53"
  aws_hosted_zone_id = "${var.cluster["aws_hosted_zone_id"]}"
  cluster_root_cname = "${var.cluster["domain_name"]}"
  nodes_elb_dns_name = "${module.node_elb.app-nodes-elb-dns_name}"
}
