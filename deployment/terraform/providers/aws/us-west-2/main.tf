module "kaystack-k8s" {
  source = "../../../modules/aws/kubernetes"
  k8s_aws_region = "us-west-2"
  k8s_aws_vpc_id = "${var.aws_vpc_id}"
  k8s_aws_external_master_subnet_ids = "${var.aws_subnet}"
  k8s_aws_external_worker_subnet_ids = "${var.aws_subnet}"
  k8s_base_domain_name = "${var.aws_base_domain_name}"
}
module "kaystack-kafka" {
  source = "../../../modules/aws/kafka"
  kafka_aws_vpc_id = "${var.aws_vpc_id}"
  kafka_aws_region = "us-west-2"
}
