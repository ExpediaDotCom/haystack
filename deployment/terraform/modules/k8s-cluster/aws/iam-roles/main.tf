resource "aws_iam_instance_profile" "masters-profile" {
  name = "masters.${var.k8s_cluster_name}"
  role = "${aws_iam_role.masters-role.name}"
}

resource "aws_iam_instance_profile" "nodes-profile" {
  name = "nodes.${var.k8s_cluster_name}"
  role = "${aws_iam_role.nodes-role.name}"
}

resource "aws_iam_role" "masters-role" {
  name = "masters.${var.k8s_cluster_name}"
  assume_role_policy = "${file("${path.module}/manifests/masters_iam-role.json")}"
}

resource "aws_iam_role" "nodes-role" {
  name = "nodes.${var.k8s_cluster_name}"
  assume_role_policy = "${file("${path.module}/manifests/nodes_iam-role.json")}"
}


data "template_file" "masters-iam-role-policy-template" {
  template = "${file("${path.module}/templates/masters_iam-role-policy.tpl")}"
  vars {
    aws_hosted_zone_id = "${var.k8s_hosted_zone_id}"
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
  }
}
resource "aws_iam_role_policy" "masters-policy" {
  name = "masters.${var.k8s_cluster_name}"
  role = "${aws_iam_role.masters-role.name}"
  policy = "${data.template_file.masters-iam-role-policy-template.rendered}"
}


data "template_file" "nodes-iam-role-policy-template" {
  template = "${file("${path.module}/templates/nodes_iam-role-policy.tpl")}"
  vars {
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
  }
}
resource "aws_iam_role_policy" "nodes-policy" {
  name = "nodes.${var.k8s_cluster_name}"
  role = "${aws_iam_role.nodes-role.name}"
  policy = "${data.template_file.nodes-iam-role-policy-template.rendered}"
}
