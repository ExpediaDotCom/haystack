terraform {
  backend "s3" {
    bucket = "haystack-deployment-tf"
    key = "terraform/us-west-2"
    region = "us-west-2"
  }
}