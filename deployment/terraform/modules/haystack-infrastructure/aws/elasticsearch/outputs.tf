output "haystack_index_store_es_url" {
  value = "${aws_elasticsearch_domain.haystack_index_store.endpoint}"
}

output "haystack_logs_es_url" {
  value = "${aws_elasticsearch_domain.haystack_index_store.endpoint}"
}
