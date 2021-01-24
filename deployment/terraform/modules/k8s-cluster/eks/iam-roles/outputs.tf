output "masters_role_name" {
  value = "${aws_iam_role.masters-role.name}"
}

output "masters_role_arn" {
  value = "${aws_iam_role.masters-role.arn}"
}

output "nodes_role_arn" {
  value = "${aws_iam_role.nodes-role.arn}"
}

output "nodes_role_name" {
  value = "${aws_iam_role.nodes-role.name}"
}

output "masters_iam-instance-profile_arn" {
  value = "${aws_iam_instance_profile.masters-profile.arn}"
}

output "nodes_iam-instance-profile_arn" {
  value = "${aws_iam_instance_profile.nodes-profile.arn}"
}

output "aws_iam_role_policy_attachment_master-AmazonEKSClusterPolicy_arn" {
  value = "${aws_iam_role_policy_attachment.master-AmazonEKSClusterPolicy.policy_arn}"
}

output "aws_iam_role_policy_attachment_master-AmazonEKSServicePolicy_arn" {
  value = "${aws_iam_role_policy_attachment.master-AmazonEKSServicePolicy.policy_arn}"
}
