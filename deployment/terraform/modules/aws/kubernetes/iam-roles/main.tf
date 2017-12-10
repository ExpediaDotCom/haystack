resource "aws_iam_instance_profile" "masters-haystack-k8s-profile" {
  name = "masters.haystack-k8s"
  role = "${aws_iam_role.masters-haystack-k8s-role.name}"
}

resource "aws_iam_instance_profile" "nodes-haystack-k8s-profile" {
  name = "nodes.haystack-k8s"
  role = "${aws_iam_role.nodes-haystack-k8s-role.name}"
}

resource "aws_iam_role" "masters-haystack-k8s-role" {
  name = "masters.haystack-k8s"
  assume_role_policy = "${file("${path.module}/data/aws_iam_role_masters.haystack-k8s_policy")}"
}

resource "aws_iam_role" "nodes-haystack-k8s-role" {
  name = "nodes.haystack-k8s"
  assume_role_policy = "${file("${path.module}/data/aws_iam_role_nodes.haystack-k8s_policy")}"
}

resource "aws_iam_role_policy" "masters-haystack-k8s-policy" {
  name = "masters.haystack-k8s"
  role = "${aws_iam_role.masters-haystack-k8s-role.name}"
  policy = "${file("${path.module}/data/aws_iam_role_policy_masters.haystack-k8s_policy")}"
}

resource "aws_iam_role_policy" "nodes-haystack-k8s-policy" {
  name = "nodes.haystack-k8s"
  role = "${aws_iam_role.nodes-haystack-k8s-role.name}"
  policy = "${file("${path.module}/data/aws_iam_role_policy_nodes.haystack-k8s_policy")}"
}
