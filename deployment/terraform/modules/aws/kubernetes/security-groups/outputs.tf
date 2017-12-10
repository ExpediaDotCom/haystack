output "api-elb-security_group_ids" {
  value = [
    "${aws_security_group.api-elb-haystack-k8s.id}"]
}

output "master_security_group_ids" {
  value = [
    "${aws_security_group.masters-haystack-k8s.id}"]
}

output "node_security_group_ids" {
  value = [
    "${aws_security_group.nodes-haystack-k8s.id}"]
}
