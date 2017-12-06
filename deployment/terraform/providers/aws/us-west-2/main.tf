module "haystack-es-index-store" {
  source = "../../../modules/aws/elasticsearch"
  worker_instance_count = "${var.es_instance_count}"
  master_instance_count = "${var.es_master_count}"
  region = "us-west-2"

}