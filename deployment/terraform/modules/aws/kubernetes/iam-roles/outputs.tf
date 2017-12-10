output "masters_role_name" {
  value = "${aws_iam_role.masters-haystack-k8s-role.name}"
}

output "masters_role_arn" {
  value = "${aws_iam_role.masters-haystack-k8s-role.arn}"
}

output "nodes_role_arn" {
  value = "${aws_iam_role.nodes-haystack-k8s-role.arn}"
}

output "nodes_role_name" {
  value = "${aws_iam_role.nodes-haystack-k8s-role.name}"
}

output "masters_iam-instance-profile_arn" {
  value = "${aws_iam_instance_profile.masters-haystack-k8s-profile.arn}"
}


output "nodes_iam-instance-profile_arn" {
  value = "${aws_iam_instance_profile.nodes-haystack-k8s-profile.arn}"
}
