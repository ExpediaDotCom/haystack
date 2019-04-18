output "kinesis-stream_arn" {
  value = "${aws_kinesis_stream.kinesis-spans-stream.arn}"
}

output "kinesis-stream_name" {
  value = "${aws_kinesis_stream.kinesis-spans-stream.name}"
}

output "kinesis-stream_shardcount" {
  value = "${aws_kinesis_stream.kinesis-spans-stream.shard_count}"
}

