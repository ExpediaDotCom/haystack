output "k8s_cluster_name" {
  value = "${var.kubectl_context_name}"
}

output "k8s_app_namespace" {
  value = "${module.k8s-addons.k8s_app_namespace}"
}

output "aa_app_namespace" {
  value = "${module.k8s-addons.aa_app_namespace}"
}

output "graphite_hostname" {
  value = "${module.k8s-addons.graphite_hostname}"
}

output "graphite_port" {
  value = "${module.k8s-addons.graphite_port}"
}

output "graphite_enabled" {
  value = "${var.monitoring_addons["enabled"]? "true" : "false"}"
}


output "kafka_hostname" {
  value = "${module.haystack-infrastructure.kafka_hostname}.${module.k8s-addons.k8s_app_namespace}.svc.cluster.local"
}

output "kafka_port" {
  value = "${module.haystack-infrastructure.kafka_port}"
}

output "elasticsearch_hostname" {
  value = "${module.haystack-infrastructure.elasticsearch_hostname}"
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



