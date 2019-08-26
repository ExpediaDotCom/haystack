output "pipes_firehose_stream_name" {
  value = "${join("", aws_kinesis_firehose_delivery_stream.pipes-firehose-stream.*.name)}"
  depends_on = ["aws_kinesis_firehose_delivery_stream.pipes-firehose-stream"]
}