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

output "cassandra_hostname" {
  value = "${module.cassandra.cassandra_hostname}"
}
output "cassandra_port" {
  value = "${module.cassandra.cassandra_port}"
}
