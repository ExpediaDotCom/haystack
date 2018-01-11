provider "aws" {
  region = "${var.aws_region}"
}
provider "kubernetes" {}

data "aws_route53_zone" "haystack_dns_zone" {
  name = "${var.aws_domain_name}"
}
