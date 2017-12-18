locals {
  traefik_node_port = 32800
}
module "haystack-aws-infrastructure" {
  source = "../../modules/aws"
  aws_vpc_id = "${var.aws_vpc_id}"
  aws_access_key = "${var.aws_access_key}"
  aws_secret_key = "${var.aws_secret_key}"
  aws_hosted_zone_id = "${var.aws_hosted_zone_id}"
  s3_bucket_name = "${var.s3_bucket_name}"
  aws_nodes_subnet = "${var.aws_nodes_subnet}"
  aws_utilities_subnet = "${var.aws_utilities_subnet}"
}

module "haystack-app-deployments" {
  source = "../../modules/kubernetes"
  k8s_cluster_name = "${module.haystack-aws-infrastructure.k8s-cluster-name}"
  k8s_logs_es_url = "${module.haystack-aws-infrastructure.k8s_logs_es_url}"
  haystack_domain_name = "${module.haystack-aws-infrastructure.k8s-cluster-name}"
  traefik_node_port = "${local.traefik_node_port}"
}

