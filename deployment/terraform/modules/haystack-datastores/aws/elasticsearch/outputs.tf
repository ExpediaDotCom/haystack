output "elasticsearch_hostname" {
  value = "${join("", aws_elasticsearch_domain.haystack_index_store.*.endpoint)}"
  depends_on = ["aws_elasticsearch_domain.haystack_index_store"]
}

output "elasticsearch_hostnames" {
  value = "${join("," , list("${join("", aws_elasticsearch_domain.haystack_index_store.*.endpoint)}","${join("", aws_elasticsearch_domain.a-haystack_index_store.*.endpoint)}"))}"
  depends_on = ["aws_elasticsearch_domain.haystack_index_store", "aws_elasticsearch_domain.a-haystack_index_store"]
}

output "elasticsearch_a-hostname" {
  value = "${join("", aws_elasticsearch_domain.a-haystack_index_store.*.endpoint)}"
  depends_on = ["aws_elasticsearch_domain.a-haystack_index_store"]
}

output "elasticsearch_service_port" {
  value = "80"
}