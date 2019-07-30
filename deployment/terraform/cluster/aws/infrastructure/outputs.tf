output "k8s_cluster_name" {
  value = "${module.haystack-k8s.cluster_name}"
}

output "k8s_app_namespace" {
  value = "${module.k8s-addons.k8s_app_namespace}"
}

output "aa_app_namespace" {
  value = "${module.k8s-addons.aa_app_namespace}"
}

output "kafka_hostname" {
  value = "${module.haystack-datastores.kafka_hostname}"
}

output "kafka_port" {
  value = "${module.haystack-datastores.kafka_port}"
}

output "kinesis-stream_name" {
  value = "${module.haystack-datastores.kinesis-stream_name}"
}

output "kinesis-stream_arn" {
  value = "${module.haystack-datastores.kinesis-stream_arn}"
}

output "kinesis-stream_shardcount" {
  value = "${module.haystack-datastores.kinesis-stream_shardcount}"
}

output "kinesis-stream_region" {
  value = "${module.haystack-datastores.kinesis-stream_region}"
}

output "pipes_firehose_writer_firehose_stream_name" {
  value = "${module.haystack-datastores.pipes_firehose_stream_name}"
}

output "elasticsearch_hostname" {
  value = "${module.haystack-datastores.elasticsearch_hostname}"
}

output "elasticsearch_port" {
  value = "${module.haystack-datastores.elasticsearch_port}"
}

output "cassandra_hostname" {
  value = "${module.haystack-datastores.cassandra_hostname}"
}

output "cassandra_port" {
  value = "${module.haystack-datastores.cassandra_port}"
}

output "graphite_hostname" {
  value = "${module.k8s-addons.graphite_hostname}"
}

output "graphite_port" {
  value = "${module.k8s-addons.graphite_port}"
}
