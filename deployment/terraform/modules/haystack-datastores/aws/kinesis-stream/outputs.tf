output "kinesis-stream_arn" {
  value = "${join("", aws_kinesis_stream.kinesis-spans-stream.*.arn)}"
}

output "kinesis-stream_name" {
  value = "${join("", aws_kinesis_stream.kinesis-spans-stream.*.name)}"
}

output "kinesis-stream_shardcount" {
  value = "${join("", aws_kinesis_stream.kinesis-spans-stream.*.shard_count)}"
}

output "kinesis-stream_region" {
  value = "${var.kinesis-stream["aws_region"]}"
}
