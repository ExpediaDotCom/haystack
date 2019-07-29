output "pipes_firehose_stream_name" {
  value = "${join("", aws_kinesis_firehose_delivery_stream.pipes_firehose_stream.*.name)}"
  depends_on = ["aws_kinesis_firehose_delivery_stream.pipes_firehose_stream"]
}