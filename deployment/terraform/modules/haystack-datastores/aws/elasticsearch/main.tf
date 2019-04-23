locals {
  haystack_index_store_domain_name = "${format("%.16s", "${var.cluster["name"]}")}-index-store"
  haystack_index_store_access_policy_file_path = "${path.module}/templates/haystack-index-store-es-policy"
}

data "aws_caller_identity" "current" {
}
module "security_groups" {
  source = "security_groups"
  cluster = "${var.cluster}"
}

data "template_file" "es_access_policy" {
  template = "${file("${local.haystack_index_store_access_policy_file_path}")}"

  vars {
    k8s_nodes_iam-role_arn = "${var.k8s_nodes_iam-role_arn}"
    aws_region = "${var.cluster["aws_region"]}"
    aws_account_id = "${data.aws_caller_identity.current.account_id}"
    es_domain_name = "${local.haystack_index_store_domain_name}"
  }
}
resource "aws_elasticsearch_domain" "haystack_index_store" {
  domain_name = "${local.haystack_index_store_domain_name}"
  elasticsearch_version = "${var.haystack_index_store_es_version}"

  cluster_config {
    instance_type = "${var.es_spans_index["worker_instance_type"]}"
    instance_count = "${var.es_spans_index["worker_instance_count"]}"
    dedicated_master_enabled = "${var.es_spans_index["dedicated_master_enabled"]}"
    dedicated_master_type = "${var.es_spans_index["master_instance_type"]}"
    dedicated_master_count = "${var.es_spans_index["master_instance_count"]}"
  }
  vpc_options {
    subnet_ids = [
      "${var.aws_subnet}"
    ]
    security_group_ids = [
      "${module.security_groups.es_security_group_ids}"
    ]
  }

  advanced_options {
    "rest.action.multi.allow_explicit_index" = "true"
  }

  access_policies = "${data.template_file.es_access_policy.rendered}"

  snapshot_options {
    automated_snapshot_start_hour = 23
  }
  tags = {
    Product = "Haystack"
    Component = "ES"
    ClusterName = "${var.cluster["name"]}"
    Role = "${var.cluster["role_prefix"]}-index-store"
    Name = "${local.haystack_index_store_domain_name}"
  }
}
