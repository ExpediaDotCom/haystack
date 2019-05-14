output "kinesis-stream_arn" {
  value = "${join("", aws_kinesis_stream.kinesis-spans-stream.*.arn)}"
  depends_on = ["aws_kinesis_stream.kinesis-spans-stream"]
}

output "kinesis-stream_name" {
  value = "${join("", aws_kinesis_stream.kinesis-spans-stream.*.name)}"
  depends_on = ["aws_kinesis_stream.kinesis-spans-stream"]
}

output "kinesis-stream_shardcount" {
  value = "${join("", aws_kinesis_stream.kinesis-spans-stream.*.shard_count)}"
  depends_on = ["aws_kinesis_stream.kinesis-spans-stream"]
}

output "kinesis-stream_region" {
  value = "${var.kinesis-stream["aws_region"]}"
  depends_on = ["aws_kinesis_stream.kinesis-spans-stream"]
}
