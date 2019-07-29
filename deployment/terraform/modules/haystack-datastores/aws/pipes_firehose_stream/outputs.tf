output "pipes_firehose_stream_role_arn" {
  value = "${join("", aws_iam_role.pipes_firehose_role.*.arn)}"
  depends_on = ["aws_iam_role.pipes_firehose_role"]
}

output "pipes_firehose_stream_name" {
  value = "${join("", aws_kinesis_firehose_delivery_stream.pipes_firehose_stream.*.name)}"
  depends_on = ["aws_kinesis_firehose_delivery_stream.pipes_firehose_stream"]
}

output "pipes_firehose_s3_configuration_bucket_arn" {
  value = "${join("", aws_kinesis_firehose_delivery_stream.pipes_firehose_stream.*.s3_configuration.bucket_arn)}"
  depends_on = ["aws_kinesis_firehose_delivery_stream.pipes_firehose_stream"]
}
