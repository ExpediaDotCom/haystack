resource "aws_elasticsearch_domain" "es" {
  domain_name           = "${var.domain_name}"
  elasticsearch_version = "${var.es_version}"

  cluster_config {
    instance_type            = "${var.worker_instance_type}"
    instance_count           = "${var.worker_instance_count}"
    dedicated_master_enabled = "true"
    dedicated_master_type    = "${var.master_instance_type}"
    dedicated_master_count   = "${var.master_instance_count}"
  }

  advanced_options {
    "rest.action.multi.allow_explicit_index" = "true"
  }

  access_policies = <<CONFIG
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "es:*",
            "Principal": "*",
            "Effect": "Allow"
        }
    ]
}
CONFIG

  snapshot_options {
    automated_snapshot_start_hour = 23
  }

  tags {
    Domain  = "${var.domain_name}"
    Product = "Haystack"
  }
}
