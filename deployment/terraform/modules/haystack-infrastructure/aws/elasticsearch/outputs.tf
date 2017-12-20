output "elasticsearch_hostname" {
  value = "${aws_elasticsearch_domain.haystack_index_store.endpoint}"
}

output "elasticsearch_service_port" {
  value = "80"
}