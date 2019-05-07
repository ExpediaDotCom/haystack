locals {
  defaultStreamName  = "${var.cluster-name}-spans"
  stream_name = "${var.kinesis-stream["name"] == "" ? local.defaultStreamName : var.kinesis-stream["name"]}"
}

resource "aws_kinesis_stream" "kinesis-spans-stream" {
  name             = "${local.stream_name}"
  shard_count      = "${var.kinesis-stream["shard_count"]}"
  retention_period = "${var.kinesis-stream["retention_period"]}"
  provider = "aws.aws_kinesis"

  tags = "${merge(var.common_tags, map(
    "ClusterName", "${var.cluster["name"]}",
    "Role", "${var.cluster["role_prefix"]}-kinesis",
    "Name", "${local.stream_name}",
    "Component", "kinesis-stream"
  ))}"
}
