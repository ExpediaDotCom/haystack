provider "null" {}
provider "template" {}

provider "aws" {
  region = "${var.cluster["aws_region"]}"
}

provider "aws"  {
  alias = "aws_kinesis"
  region = "${lookup(var.kinesis-stream, "aws_region", var.cluster["aws_region"])}"
}
