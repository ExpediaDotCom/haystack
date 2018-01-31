locals {
  rendered_config_path = "${path.module}/manifests/cluster-config.yaml"
  kops_public_key_path = "${path.module}/manifests/aws_key_pair_kubernetes.haystack-k8s.com-public_key"
}

data "template_file" "cluster_config" {
  template = "${file("${path.module}/templates/cluster.tpl")}"
  vars {
    k8s_version = "${var.k8s_version}"
    cluster_name = "${var.k8s_cluster_name}"
    aws_vpc_id = "${var.aws_vpc_id}"
    aws_zone = "${var.aws_zone}"
    s3_bucket_name = "${var.s3_bucket_name}"
    master_instance_type = "${var.masters_instance_type}"
    app-node_instance_count = "${var.app-nodes_instance_count}"
    app-node_instance_type = "${var.app-nodes_instance_type}"
    monitoring-node_instance_count = "${var.monitoring-nodes_instance_count}"
    monitoring-node_instance_type = "${var.monitoring-nodes_instance_type}"
    aws_node_subnet = "${var.aws_nodes_subnet}"
    aws_utilities_subnet = "${var.aws_utilities_subnet}"
    aws_dns_zone_id = "${var.aws_hosted_zone_id}"
  }
}

resource "null_resource" "export-cluster-rendered-template" {
  triggers {
    template = "${data.template_file.cluster_config.rendered}"
  }
  //The --unregister flag just deletes the kops configurations stored in s3
  provisioner "local-exec" {
    command = "${var.kops_executable_name} delete cluster ${var.k8s_cluster_name} --state s3://${var.s3_bucket_name} --unregister --yes || true"
  }
  provisioner "local-exec" {
    command = "cat > ${local.rendered_config_path} <<EOL\n${data.template_file.cluster_config.rendered}EOL"
  }
  //updating the cluster config in S3
  provisioner "local-exec" {
    command = "${var.kops_executable_name} create -f ${local.rendered_config_path} --state s3://${var.s3_bucket_name}"
  }
  provisioner "local-exec" {
    command = "${var.kops_executable_name} create secret --name ${var.k8s_cluster_name} --state s3://${var.s3_bucket_name} sshpublickey admin -i ${local.kops_public_key_path}"
  }
  //generating certs using kops
  provisioner "local-exec" {
    command = "${var.kops_executable_name} update cluster ${var.k8s_cluster_name} --state s3://${var.s3_bucket_name} --target terraform"
  }


}