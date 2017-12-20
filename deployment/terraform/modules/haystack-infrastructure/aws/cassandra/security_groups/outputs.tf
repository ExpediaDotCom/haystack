output "nodes_security_group_ids" {
  value = [
    "${aws_security_group.haystack-cassandra-nodes.id}"]
}
