output "role_name" {
  value = "${aws_iam_role.haystack-kafka-role.name}"
}

output "role_arn" {
  value = "${aws_iam_role.haystack-kafka-role.arn}"
}
output "iam-instance-profile_arn" {
  value = "${aws_iam_instance_profile.haystack-kafka-profile.arn}"
}
