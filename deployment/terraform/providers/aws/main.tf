provider "aws" {
  access_key = "AKIAJZP3VKLQUCYDDHUQ"
  secret_key = "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"
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