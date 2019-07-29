output "elasticsearch_hostname" {
  value = "${module.es.elasticsearch_hostname}"
}

output "elasticsearch_port" {
  value = "${module.es.elasticsearch_service_port}"
}

output "kafka_hostname" {
  value = "${module.kafka.kafka_service_name}"
}
output "kafka_port" {
  value = "${module.kafka.kafka_port}"
}

output "kinesis-stream_arn" {
  value = "${module.kinesis-stream.kinesis-stream_arn}"
}

output "kinesis-stream_name" {
  value = "${module.kinesis-stream.kinesis-stream_name}"
}

output "kinesis-stream_shardcount" {
  value = "${module.kinesis-stream.kinesis-stream_shardcount}"
}

output "kinesis-stream_region" {
  value = "${module.kinesis-stream.kinesis-stream_region}"
}

output "pipes_firehose_stream_role_arn" {
  value = "${module.pipes_firehose_stream.pipes_firehose_stream_role_arn}"
}

output "pipes_firehose_stream_name" {
  value = "${module.pipes_firehose_stream.pipes_firehose_stream_name}"
}

output "pipes_firehose_s3_configuration_bucket_arn" {
  value = "${module.pipes_firehose_stream.pipes_firehose_s3_configuration_bucket_arn}"
}

output "cassandra_hostname" {
  value = "${module.cassandra.cassandra_hostname}"
}
output "cassandra_port" {
  value = "${module.cassandra.cassandra_port}"
}
