provider "null" {}
provider "template" {}

provider "aws" {
  region = "${var.cluster["aws_region"]}"
}
