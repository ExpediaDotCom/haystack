locals {
  aws_nodes_subnets = "${split(",", var.cluster["aws_nodes_subnet"])}"
  aws_nodes_subnet = "${element(local.aws_nodes_subnets,0)}"
}

data "aws_route53_zone" "haystack_dns_zone" {
  name = "${var.cluster["domain_name"]}"
}


module "cassandra" {
  source = "cassandra"
  aws_vpc_id = "${var.cluster["aws_vpc_id"]}"
  aws_subnet = "${local.aws_nodes_subnet}"
  aws_hosted_zone_id = "${data.aws_route53_zone.haystack_dns_zone.id}"
  node_image = "${var.cassandra_spans_backend["node_image"]}"
  seed_node_count = "${var.cassandra_spans_backend["seed_node_instance_count"]}"
  non_seed_node_count = "${var.cassandra_spans_backend["non_seed_node_instance_count"]}"
  seed_node_volume_size = "${var.cassandra_spans_backend["seed_node_volume_size"]}"
  non_seed_node_volume_size = "${var.cassandra_spans_backend["non_seed_node_volume_size"]}"
  seed_node_instance_type = "${var.cassandra_spans_backend["seed_node_instance_type"]}"
  non_seed_node_instance_type = "${var.cassandra_spans_backend["non_seed_node_instance_type"]}"
  aws_ssh_key_pair_name = "${var.cluster["aws_ssh_key"]}"
  graphite_host = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
  haystack_cluster_name = "${var.cluster["name"]}"
  haystack_cluster_role = "${var.cluster["role_prefix"]}"
}

module "es" {
  source = "elasticsearch"
  master_instance_count = "${var.es_spans_index["master_instance_count"]}"
  master_instance_type = "${var.es_spans_index["master_instance_type"]}"
  worker_instance_count = "${var.es_spans_index["worker_instance_count"]}"
  worker_instance_type = "${var.es_spans_index["worker_instance_type"]}"
  dedicated_master_enabled = "${var.es_spans_index["dedicated_master_enabled"]}"
  haystack_cluster_name = "${var.cluster["name"]}"
  haystack_cluster_role = "${var.cluster["role_prefix"]}"
  aws_vpc_id = "${var.cluster["aws_vpc_id"]}"
  aws_subnet = "${local.aws_nodes_subnet}"
  aws_region = "${var.cluster["aws_region"]}"
  k8s_nodes_iam-role_arn = "${var.k8s_nodes_iam-role_arn}"
}

module "kafka" {
  source = "kafka"
  cluster = "${var.cluster}"
  kafka= "${var.kafka}"
  aws_subnets = "${local.aws_nodes_subnets}"
  aws_hosted_zone_id = "${data.aws_route53_zone.haystack_dns_zone.id}"
  aws_graphite_host = "${var.graphite_hostname}"
  aws_graphite_port = "${var.graphite_port}"
}
