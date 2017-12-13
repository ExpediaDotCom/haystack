resource "aws_elasticsearch_domain" "haystack_index_store" {
  domain_name = "${var.haystack_index_store_domain_name}"
  elasticsearch_version = "${var.haystack_index_store_es_version}"

  cluster_config {
    instance_type = "${var.haystack_index_store_worker_instance_type}"
    instance_count = "${var.haystack_index_store_worker_instance_count}"
    dedicated_master_enabled = "true"
    dedicated_master_type = "${var.haystack_index_store_master_instance_type}"
    dedicated_master_count = "${var.haystack_index_store_master_instance_count}"
  }

  advanced_options {
    "rest.action.multi.allow_explicit_index" = "true"
  }

  access_policies = "${file("${path.module}/data/haystack-es-policy")}"

  snapshot_options {
    automated_snapshot_start_hour = 23
  }

  tags {
    Domain = "${var.haystack_index_store_domain_name}"
    Product = "Haystack"
  }
}

resource "aws_elasticsearch_domain" "haystack_logs" {
  domain_name = "${var.haystack_logs_domain_name}"
  elasticsearch_version = "${var.haystack_logs_es_version}"

  cluster_config {
    instance_type = "${var.haystack_logs_instance_type}"
    instance_count = "${var.haystack_logs_instance_count}"
    dedicated_master_enabled = "false"
  }

  advanced_options {
    "rest.action.multi.allow_explicit_index" = "true"
  }

  access_policies = "${file("${path.module}/data/haystack-es-policy")}"

  snapshot_options {
    automated_snapshot_start_hour = 23
  }

  tags {
    Domain = "${var.haystack_logs_domain_name}"
    Product = "Haystack"
  }
}
