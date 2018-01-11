provider "null" {}
provider "template" {}
provider "aws" {
  region = "${var.aws_region}"
}
data "aws_route53_zone" "haystack_dns_zone" {
  name = "${var.aws_domain_name}"
}
