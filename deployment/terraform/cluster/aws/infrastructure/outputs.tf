output "k8s_cluster_name" {
  value = "${module.haystack-k8s.cluster_name}"
}

output "k8s_app_namespace" {
  value = "${module.k8s-addons.k8s_app_namespace}"
}

output "kafka_hostname" {
  value = "${module.haystack-infrastructure.kafka_hostname}"
}

output "kafka_port" {
  value = "${module.haystack-infrastructure.kafka_port}"
}

output "elasticsearch_hostname" {
  value = "${module.haystack-infrastructure.elasticsearch_port}"
}
output "elasticsearch_port" {
  value = "${module.haystack-infrastructure.elasticsearch_port}"
}


output "cassandra_hostname" {
  value = "${module.haystack-infrastructure.cassandra_hostname}"
}

output "cassandra_port" {
  value = "${module.haystack-infrastructure.cassandra_port}"
}

output "graphite_hostname" {
  value = "${module.haystack-infrastructure.graphite_hostname}"
}

output "graphite_port" {
  value = "${module.haystack-infrastructure.graphite_port}"
}
