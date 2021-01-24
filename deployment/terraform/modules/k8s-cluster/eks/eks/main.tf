resource "aws_eks_cluster" "master" {
  name            = "${var.cluster_name}"
  role_arn        = "${var.role_arn}"

  vpc_config {
    security_group_ids = ["${var.master_security_group_ids}"]
    subnet_ids         = ["${var.aws_subnet_ids}"]
  }
}