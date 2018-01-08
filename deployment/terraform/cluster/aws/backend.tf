terraform {
  backend "s3" {
    bucket = "haystack-deployment-tf"
    key = "terraform/state"
    region = "us-west-2"
  }
}