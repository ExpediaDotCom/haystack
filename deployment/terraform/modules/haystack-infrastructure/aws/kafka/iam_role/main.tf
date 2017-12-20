resource "aws_iam_instance_profile" "haystack-kafka-profile" {
  name = "haystack-kafka-profile"
  role = "${aws_iam_role.haystack-kafka-role.name}"
}

resource "aws_iam_role" "haystack-kafka-role" {
  name = "haystack-kafka-role"
  assume_role_policy = "${file("${path.module}/data/aws_iam_role_haystack-kafka_policy")}"
}


resource "aws_iam_role_policy" "haystack-kafka-policy" {
  name = "haystack-kafka-policy"
  role = "${aws_iam_role.haystack-kafka-role.name}"
  policy = "${file("${path.module}/data/aws_iam_role_policy_haystack-kafka_policy")}"
}

