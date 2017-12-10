provider "aws" {
  region = "${var.region}"
}
terraform {
  backend "s3" {
    bucket = "haystack-deployment-tf"
    key = "terraform/us-west-2"
    region = "us-west-2"

  }
}

module "us-west-2-deployment" {
  source = "us-west-2"
}