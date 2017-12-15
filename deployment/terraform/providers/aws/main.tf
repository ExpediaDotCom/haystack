data "aws_route53_zone" "haystack_dns_zone" {
  zone_id = "${var.aws_hosted_zone_id}"
}


module "kaystack-k8s" {
  source = "../../modules/aws/kubernetes"
  k8s_aws_zone = "${var.aws_zone}"
  k8s_aws_vpc_id = "${var.aws_vpc_id}"
  k8s_aws_nodes_subnet_ids = "${var.aws_nodes_subnet}"
  k8s_aws_utility_subnet_ids = "${var.aws_utilities_subnet}"
  k8s_s3_bucket_name = "${var.s3_bucket_name}"
  k8s_master_instance_type = "${var.k8s_master_instance_type}"
  k8s_node_instance_type = "${var.k8s_node_instance_type}"
  k8s_node_instance_count = "${var.k8s_node_instance_count}"
  k8s_hosted_zone_id = "${var.aws_hosted_zone_id}"
  k8s_aws_ssh_key = "${var.aws_ssh_key}"
  //Refer to bug https://github.com/hashicorp/terraform/issues/8511
  k8s_base_domain_name = "${replace(data.aws_route53_zone.haystack_dns_zone.name, "/[.]$/", "")}"
  k8s_aws_region = "${var.aws_region}"
  k8s_logs_es_url = ""
}
