provider "null" {}
provider "template" {}
provider "aws" {
  region = "${var.cluster["aws_region"]}"
}

provider "aws" {
  alias = "us-east-1"
  region = "us-east-1"
}

