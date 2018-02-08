locals {
  haystack_index_store_domain_name = "${var.haystack_cluster_name}-index-store"
  haystack_index_store_access_policy_file_path = "${path.module}/templates/haystack-index-store-es-policy"

}

data "aws_caller_identity" "current" {
}
module "security_groups" {
  source = "security_groups"
  haystack_cluster_name = "${var.haystack_cluster_name}"
  aws_vpc_id = "${var.aws_vpc_id}"
}

data "template_file" "es_access_policy" {
  template = "${file("${local.haystack_index_store_access_policy_file_path}")}"

  vars {
    k8s_nodes_iam-instance-profile_arn = "${var.k8s_nodes_iam-instance-profile_arn}"
    aws_region = "${var.aws_region}"
    aws_account_id = "${data.aws_caller_identity.current.account_id}"
    es_domain_name = "${local.haystack_index_store_domain_name}"
  }
}
resource "aws_elasticsearch_domain" "haystack_index_store" {
  domain_name = "${local.haystack_index_store_domain_name}"
  elasticsearch_version = "${var.haystack_index_store_es_version}"

  cluster_config {
    instance_type = "${var.worker_instance_type}"
    instance_count = "${var.worker_instance_count}"
    dedicated_master_enabled = "true"
    dedicated_master_type = "${var.master_instance_type}"
    dedicated_master_count = "${var.master_instance_count}"
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
    ClusterName = "${var.haystack_cluster_name}"
    Role = "${local.haystack_index_store_domain_name}"
    Name = "${local.haystack_index_store_domain_name}"
  }
}