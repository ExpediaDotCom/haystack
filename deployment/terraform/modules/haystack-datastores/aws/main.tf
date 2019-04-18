locals {
  aws_nodes_subnets = "${split(",", var.cluster["aws_nodes_subnet"])}"
  aws_nodes_subnet = "${element(local.aws_nodes_subnets,0)}"
}

data "aws_route53_zone" "haystack_dns_zone" {
  name = "${var.cluster["domain_name"]}"
}


module "cassandra" {
  source = "cassandra"
  cluster = "${var.cluster}"
  cassandra_spans_backend = "${var.cassandra_spans_backend}"
  aws_subnet = "${local.aws_nodes_subnet}"
  aws_hosted_zone_id = "${data.aws_route53_zone.haystack_dns_zone.id}"
  graphite_host = "${var.graphite_hostname}"
  graphite_port = "${var.graphite_port}"
}

module "es" {
  source = "elasticsearch"
  es_spans_index = "${var.es_spans_index}"
  cluster = "${var.cluster}"
  aws_subnet = "${local.aws_nodes_subnet}"
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

module "kinesis-stream" {
  source = "kinesis-stream"
  cluster-name = "${var.cluster["name"]}"
  kinesis-stream = "${var.kinesis-stream}"
}
