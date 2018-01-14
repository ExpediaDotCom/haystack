output "api-elb-security_group_ids" {
  value = [
    "${aws_security_group.api-elb.id}"]
}

output "nodes-api-elb-security_group_ids" {
  value = [
    "${aws_security_group.nodes-elb.id}"]
}


output "master_security_group_ids" {
  value = [
    "${aws_security_group.masters.id}"]
}

output "node_security_group_ids" {
  value = [
    "${aws_security_group.nodes.id}"]
}
