module "kaystack-aws-infrastructure" {
  source = "aws"
  aws_vpc_id = "${var.aws_vpc_id}"
  aws_subnet = "${var.aws_subnet}"
  aws_access_key = "${var.aws_access_key}"
  aws_secret_key = "${var.aws_secret_key}"
  aws_hosted_zone_id = "${var.aws_hosted_zone_id}"
  s3_bucket_name = "${var.s3_bucket_name}"
}

module "kaystack-app-deployments" {
  source = "kubernetes"
  k8s_cluster_name = "${module.kaystack-aws-infrastructure.k8s-cluster-name}"
}
