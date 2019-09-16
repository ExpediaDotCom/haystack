locals {
  default_pipes_firehose_stream_name  = "${var.cluster["name"]}-json-spans"
  pipes_firehose_stream_name = "${var.pipes_firehose_stream["name"] == "" ? local.default_pipes_firehose_stream_name : var.pipes_firehose_stream["name"]}"
  s3_configuration_bucket_arn = "arn:aws:s3:::${var.pipes_firehose_stream["s3_configuration_bucket_name"]}"
  s3_configuration_bucket_prefix = "${var.pipes_firehose_stream["prefix"] == "" ? "json/${var.cluster["name"]}/" : var.pipes_firehose_stream["prefix"]}"
}

resource "aws_iam_role" "pipes-firehose-role" {
  name = "${local.pipes_firehose_stream_name}"
  count = "${var.pipes_firehose_stream["enabled"]}"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "firehose.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_iam_role_policy" "pipes-firehose-role-policy" {
  name = "${local.pipes_firehose_stream_name}"
  role = "${aws_iam_role.pipes-firehose-role.name}"
  policy = "${data.template_file.pipes-firehose-role-policy-template.rendered}"
  count = "${var.pipes_firehose_stream["enabled"]}"
}

data "template_file" "pipes-firehose-role-policy-template" {
  template = "${file("${path.module}/templates/pipes-firehose-role-policy.tpl")}"
  vars {
    s3_bucket_name = "${var.pipes_firehose_stream["s3_configuration_bucket_name"]}"
  }
}

resource "aws_kinesis_firehose_delivery_stream" "pipes-firehose-stream" {
  name        = "${local.pipes_firehose_stream_name}"
  destination = "${var.pipes_firehose_stream["destination"]}"
  count = "${var.pipes_firehose_stream["enabled"]}"

  tags = "${merge(var.common_tags, map(
    "ClusterName", "${var.cluster["name"]}",
    "Role", "${var.cluster["role_prefix"]}-pipes_firehose_stream",
    "Name", "${local.pipes_firehose_stream_name}",
    "Component", "pipes_firehose_stream"
  ))}"

  s3_configuration {
    role_arn   = "${aws_iam_role.pipes-firehose-role.arn}"
    bucket_arn = "${local.s3_configuration_bucket_arn}"
    prefix = "${local.s3_configuration_bucket_prefix}"
    compression_format = "${var.pipes_firehose_stream["compression_format"]}"
    buffer_size = "${var.pipes_firehose_stream["buffer_size"]}"
    buffer_interval = "${var.pipes_firehose_stream["buffer_interval"]}"
  }

  depends_on = ["aws_iam_role.pipes-firehose-role"]
}