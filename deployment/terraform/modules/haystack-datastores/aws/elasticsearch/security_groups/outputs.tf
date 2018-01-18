output "es_security_group_ids" {
  value = "${aws_security_group.haystack-es.id}"
}
