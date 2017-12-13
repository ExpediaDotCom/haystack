locals {
  rendered_config_path = "${path.module}/manifests/cluster-config.yaml"
  kops_public_key_path = "${path.module}/data/aws_key_pair_kubernetes.haystack-k8s.com-public_key"
}

data "template_file" "cluster_config" {
  template = "${file("${path.module}/templates/cluster.tpl")}"
  vars {
    k8s_version = "${var.k8s_version}"
    cluster_name = "${var.k8s_cluster_name}"
    aws_vpc_id = "${var.k8s_aws_vpc_id}"
    aws_zone = "${var.k8s_aws_zone}"
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
    master_instance_type = "${var.k8s_master_instance_type}"
    node_instance_count = "${var.k8s_node_instance_count}"
    node_instance_type = "${var.k8s_node_instance_type}"
    aws_subnet = "${var.k8s_aws_subnet}"
    aws_dns_zone_id = "${var.k8s_hosted_zone_id}"
  }
}

resource "aws_s3_bucket_object" "kops_config_folder" {
  bucket = "${var.k8s_s3_bucket_name}"
  acl    = "private"
  key    = "${var.k8s_cluster_name}/"
  source = "/dev/null"
}

resource "null_resource" "export-cluster-rendered-template" {
  triggers {
    template = "${data.template_file.cluster_config.rendered}"
  }
  provisioner "local-exec" {
    command = "cat > ${local.rendered_config_path} <<EOL\n${data.template_file.cluster_config.rendered}EOL"
  }
  //updating the cluster config in S3
  provisioner "local-exec" {
    command = "${var.kops_executable_name} create -f ${local.rendered_config_path} --state s3://${var.k8s_s3_bucket_name}"
  }
  provisioner "local-exec" {
    command = "${var.kops_executable_name} create secret --name ${var.k8s_cluster_name} --state s3://${var.k8s_s3_bucket_name} sshpublickey admin -i ${local.kops_public_key_path}"
  }
  //generating certs using kops
  provisioner "local-exec" {
    command = "${var.kops_executable_name} update cluster ${var.k8s_cluster_name} --state s3://${var.k8s_s3_bucket_name} --target terraform"
  }

}