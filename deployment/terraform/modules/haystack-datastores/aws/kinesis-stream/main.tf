locals {
  defaultStreamName  = "${var.cluster["name"]}-spans"
  stream_name = "${var.kinesis-stream["name"] == "" ? local.defaultStreamName : var.kinesis-stream["name"]}"
  app_group_name = "${var.cluster["name"]}-kinesis-span-collector"
}

resource "aws_kinesis_stream" "kinesis-spans-stream" {
  name             = "${local.stream_name}"
  shard_count      = "${var.kinesis-stream["shard_count"]}"
  retention_period = "${var.kinesis-stream["retention_period"]}"
  provider = "aws.aws_kinesis"
  count = "${var.kinesis-stream["enabled"]}"
  tags = "${merge(var.common_tags, map(
    "ClusterName", "${var.cluster["name"]}",
    "Role", "${var.cluster["role_prefix"]}-kinesis",
    "Name", "${local.stream_name}",
    "Component", "kinesis-stream"
  ))}"
}


resource "aws_dynamodb_table" "kinesis-consumer-table" {
  "attribute" {
    name = "leaseKey"
    type = "S"
  }
  hash_key = "leaseKey"
  name = "${local.app_group_name}"
  read_capacity = "${var.dynamodb["read_limit"]}"
  write_capacity = "${var.dynamodb["write_limit"]}"
  provider = "aws.aws_kinesis"
  tags = "${merge(var.common_tags, map(
    "ClusterName", "${var.cluster["name"]}",
    "Name", "${local.app_group_name}",
    "Component", "dynamodb-consumer-table"
  ))}"
  count = "${var.kinesis-stream["enabled"]}"
  depends_on = ["aws_kinesis_stream.kinesis-spans-stream"]
}
