output "master_security_group_ids" {
  value = [
    "${aws_security_group.masters.id}"]
  depends_on = [
    "aws_security_group_rule.all-master-to-master",
    "aws_security_group_rule.all-node-to-master",
    "aws_security_group_rule.ssh-external-to-master-0-0-0-0--0",
    "aws_security_group_rule.https-elb-to-master",
    "aws_security_group_rule.master-egress"
  ]
}

output "node_security_group_ids" {
  value = [
    "${aws_security_group.nodes.id}"]
  depends_on = [
    "aws_security_group_rule.node-egress",
    "aws_security_group_rule.graphite_elb-to-node",
    "aws_security_group_rule.reverse_proxy-to-node",
    "aws_security_group_rule.ssh-external-to-node-0-0-0-0--0",
    "aws_security_group_rule.all-node-to-node",
    "aws_security_group_rule.all-master-to-node",
    "aws_security_group_rule.all-master-to-node"
  ]
}

output "nodes_elb_security_groups" {
  value = [
    "${aws_security_group.nodes-elb.id}"
  ]
}