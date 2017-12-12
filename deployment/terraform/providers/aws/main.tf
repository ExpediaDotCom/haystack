
module "kaystack-k8s" {
  source = "../../modules/aws/kubernetes"
  k8s_aws_zone = "${var.aws_zone}"
  k8s_aws_vpc_id = "${var.aws_vpc_id}"
  k8s_aws_external_master_subnet_ids = "${var.aws_subnet}"
  k8s_aws_external_worker_subnet_ids = "${var.aws_subnet}"
  k8s_s3_bucket_name = "${var.s3_bucket_name}"
  k8s_master_instance_type = "${var.k8s_master_instance_type}"
  k8s_node_instance_type = "${var.k8s_node_instance_type}"
  k8s_node_instance_count = "${var.k8s_node_instance_count}"
  k8s_hosted_zone_id = "${var.aws_hosted_zone_id}"
  k8s_aws_ssh_key = "${var.aws_ssh_key}"
  k8s_base_domain_name = "test.monitoring.expedia.com"
}

/*
module "kaystack-kafka" {
  source = "../../modules/aws/kafka"
  kafka_aws_vpc_id = "${var.aws_vpc_id}"
  kafka_aws_region = "${var.aws_region}"
  kafka_aws_ssh_key = "${var.aws_ssh_key}"
  kafka_aws_subnet = "${var.aws_subnet}"
  kafka_broker_count = "${var.kafka_broker_count}"
  kafka_broker_instance_type = "${var.kafka_broker_instance_type}"
}

module "kaystack-es" {
  source = "../../modules/aws/elasticsearch"
  master_instance_count = "${var.es_master_count}"
  worker_instance_count = "${var.es_instance_count}"
  master_instance_type = "${var.es_master_instance_type}"
  worker_instance_type = "${var.es_master_instance_type}"
}


*/


