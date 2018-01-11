provider "aws" {
  region = "${var.aws_region}"
}
provider "kubernetes" {
  config_context = "${module.haystack-k8s.cluster_name}"
  load_config_file = false
}

data "aws_route53_zone" "haystack_dns_zone" {
  name = "${var.aws_domain_name}"
}
