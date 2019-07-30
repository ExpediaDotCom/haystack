locals {
  /* appending char `e` explicitly at the end to avoid the issue due to name ending
    with a special charater `-`. Limit on length for ES name is 28 chars.
  */
  haystack_index_store_domain_name = "${format("%.27s", "${var.cluster["name"]}-index-stor")}e"
  count = "${var.es_spans_index["enabled"]?1:0}"
  haystack_index_store_access_policy_file_path = "${path.module}/templates/haystack-index-store-es-policy"

  a-haystack_index_store_domain_name = "${format("%.27s", "a-${var.cluster["name"]}-index-stor")}e"
  a-count = "${var.es_spans_index["a-enabled"]?1:0}"
}

data "aws_caller_identity" "current" {
}
module "security_groups" {
  source = "security_groups"
  cluster = "${var.cluster}"
  common_tags = "${var.common_tags}"
}
data "template_file" "es_access_policy" {
  template = "${file("${local.haystack_index_store_access_policy_file_path}")}"

  vars {
    k8s_nodes_iam-role_arn = "${var.k8s_nodes_iam-role_arn}"
    aws_region = "${var.cluster["aws_region"]}"
    aws_account_id = "${data.aws_caller_identity.current.account_id}"
    es_domain_name = "${local.haystack_index_store_domain_name}"
    a-es_domain_name = "${local.a-haystack_index_store_domain_name}"
  }
}
resource "aws_elasticsearch_domain" "haystack_index_store" {
  domain_name = "${local.haystack_index_store_domain_name}"
  elasticsearch_version = "${var.es_spans_index["es_version"]}"

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
  tags = "${merge(var.common_tags, map(
    "ClusterName", "${var.cluster["name"]}",
    "Role", "${var.cluster["role_prefix"]}-index-store",
    "Name", "${local.haystack_index_store_domain_name}",
    "Component", "ES"
  ))}"
  count = "${local.count}"
}

resource "aws_elasticsearch_domain" "a-haystack_index_store" {
  domain_name = "${local.a-haystack_index_store_domain_name}"
  elasticsearch_version = "${var.es_spans_index["a-es_version"]}"

  cluster_config {
    instance_type = "${var.es_spans_index["a-worker_instance_type"]}"
    instance_count = "${var.es_spans_index["a-worker_instance_count"]}"
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
  tags = "${merge(var.common_tags, map(
    "ClusterName", "${var.cluster["name"]}",
    "Role", "${var.cluster["role_prefix"]}-index-store",
    "Name", "${local.a-haystack_index_store_domain_name}",
    "Component", "ES"
  ))}"
  count = "${local.a-count}"
}