output "master-1_asg_id" {
  value = "${aws_autoscaling_group.master-1.id}"
}
output "master-2_asg_id" {
  value = "${aws_autoscaling_group.master-2.id}"
}

output "master-3_asg_id" {
  value = "${aws_autoscaling_group.master-2.id}"
}

output "nodes_asg_id" {
  value = "${aws_autoscaling_group.nodes.id}"

}