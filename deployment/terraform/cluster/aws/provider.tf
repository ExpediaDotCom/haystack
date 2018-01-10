provider "aws" {
  region = "${var.aws_region}"
}

provider "kubernetes" {
  config_context = "${module.haystack-k8s.kube_context}"
}

data "aws_route53_zone" "haystack_dns_zone" {
  name = "${var.aws_domain_name}"
}
