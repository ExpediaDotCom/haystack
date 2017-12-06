module "kubernetes" {
  source = "coreos/kubernetes/aws"
  tectonic_admin_email = "${var.k8s_admin_email}"
  tectonic_admin_password = "${var.k8s_admin_password}"
  tectonic_aws_ssh_key = "${var.k8s_aws_ssh_key}"
  tectonic_cluster_name = "${var.ks8_cluster_name}"
  tectonic_aws_assets_s3_bucket_name = "${var.k8s_s3_bucket_name}"
  tectonic_aws_external_vpc_id = "${var.k8s_vpc_id}"
  tectonic_aws_external_vpc_id = "${var.k8s_aws_external_master_subnet_ids}"
  tectonic_aws_external_vpc_id = "${var.k8s_aws_external_worker_subnet_ids}"
  tectonic_aws_extra_tags = {
    Product = "Haystack"
  }
  tectonic_aws_region = "${var.k8s_aws_region}"
  tectonic_container_linux_version = "latest"
  tectonic_aws_public_endpoints = "false"
  tectonic_vanilla_k8s = "true"

}