locals {
  stream_name = "${var.cluster-name}-spans"
}

resource "aws_kinesis_stream" "kinesis-spans-stream" {
  name             = "${local.stream_name}"
  shard_count      = "${var.kinesis-stream["shard_count"]}"
  retention_period = "${var.kinesis-stream["retention_period"]}"
  provider = "aws.aws_kinesis"

  tags = {
    Product = "Haystack"
    Component = "kinesis-stream"
    Role = "haystack-kinesis"
    Name = "${local.stream_name}"
  }

}
