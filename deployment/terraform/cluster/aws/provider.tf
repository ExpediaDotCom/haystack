provider "aws" {
  access_key = "${var.aws_access_key}"
  secret_key = "${var.aws_secret_key}"
  region = "${var.aws_region}"
}

provider "kubernetes" {
  config_context = "${module.haystack-k8s.cluster_name}"
}

data "aws_route53_zone" "haystack_dns_zone" {
  zone_id = "${var.aws_hosted_zone_id}"
}