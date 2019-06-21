output "elasticsearch_hostname" {
  value = "${aws_elasticsearch_domain.haystack_index_store.endpoint}"
  depends_on = ["aws_elasticsearch_domain.haystack_index_store"]
}

output "elasticsearch_service_port" {
  value = "80"
}