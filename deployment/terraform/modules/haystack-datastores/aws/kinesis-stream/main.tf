locals {
  stream_name = "${var.cluster}-spans"
}

resource "aws_kinesis_stream" "kinesis-spans-stream" {
  name             = "${local.stream_name}"
  shard_count      = "${var.kinesis-stream["shard_count"]}"
  retention_period = "${var.kinesis-stream["retention_period"]}"

  shard_level_metrics = [
    "IncomingBytes",
    "OutgoingBytes",
  ]

  tags = {
    Product = "Haystack"
    Component = "kinesis-stream"
    Role = "haystack-kinesis"
    Name = "${local.stream_name}"
  }

}
