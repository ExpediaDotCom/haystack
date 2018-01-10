provider "aws" {
  region = "${var.aws_region}"
}

data "aws_route53_zone" "haystack_dns_zone" {
  zone_id = "${var.aws_hosted_zone_id}"
}