module "kaystack-k8s" {
  source = "../../../modules/aws/kubernetes"
  k8s_aws_region = "us-west-2"
  k8s_vpc_id = "${var.aws_vpc_id}"
  k8s_aws_external_master_subnet_ids = "${var.aws_subnet}"
  k8s_aws_external_worker_subnet_ids = "${var.aws_subnet}"
}

module "haystack-es-index-store" {
  source = "../../../modules/aws/elasticsearch"
  worker_instance_count = "${var.es_instance_count}"
  master_instance_count = "${var.es_master_count}"
  region = "us-west-2"

}