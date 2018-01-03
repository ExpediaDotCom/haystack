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
  assume_role_policy = "${file("${path.module}/manifests/haystack-k8s_master_iam-role.tpl")}"
}

resource "aws_iam_role" "nodes-haystack-k8s-role" {
  name = "nodes.haystack-k8s"
  assume_role_policy = "${file("${path.module}/manifests/haystack-k8s_nodes_iam-role.tpl")}"
}


data "template_file" "master-iam-role-policy-template" {
  template = "${file("${path.module}/templates/haystack-k8s_master_iam-role-policy.tpl")}"
  vars {
    aws_hosted_zone_id = "${var.k8s_hosted_zone_id}"
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
  }
}
resource "aws_iam_role_policy" "masters-haystack-k8s-policy" {
  name = "masters.haystack-k8s"
  role = "${aws_iam_role.masters-haystack-k8s-role.name}"
  policy = "${data.template_file.master-iam-role-policy-template.rendered}"
}


data "template_file" "nodes-iam-role-policy-template" {
  template = "${file("${path.module}/templates/haystack-k8s_nodes_iam-role-policy.tpl")}"
  vars {
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
  }
}
resource "aws_iam_role_policy" "nodes-haystack-k8s-policy" {
  name = "nodes.haystack-k8s"
  role = "${aws_iam_role.nodes-haystack-k8s-role.name}"
  policy = "${data.template_file.nodes-iam-role-policy-template.rendered}"
}
