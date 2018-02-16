locals {
  rendered_config_path = "${path.module}/manifests/cluster-config.yaml"
  kops_public_key_path = "${path.module}/manifests/aws_key_pair_kubernetes.haystack-k8s.com-public_key"
}

data "aws_vpc" "haystack_deployment_vpc" {
  id = "${var.aws_vpc_id}"

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
    app-node_instance_volume = "${var.app-nodes_instance_volume}"
    monitoring-node_instance_volume = "${var.monitoring-nodes_instance_volume}"
    master_instance_volume = "${var.master_instance_volume}"
    monitoring-node_instance_count = "${var.monitoring-nodes_instance_count}"
    monitoring-node_instance_type = "${var.monitoring-nodes_instance_type}"
    aws_node_subnet = "${var.aws_nodes_subnet}"
    aws_utilities_subnet = "${var.aws_utilities_subnet}"
    aws_dns_zone_id = "${var.aws_hosted_zone_id}"
    aws_network_cidr = "${data.aws_vpc.haystack_deployment_vpc.cidr_block}"
  }
}

//creating the cluster for the first time
resource "null_resource" "create_cluster_configuration" {
  provisioner "local-exec" {
    command = "cat > ${local.rendered_config_path} <<EOL\n${data.template_file.cluster_config.rendered}EOL"
  }
  //updating the cluster config in S3
  provisioner "local-exec" {
    command = "${var.kops_executable_name} create -f ${local.rendered_config_path} --state s3://${var.s3_bucket_name}"
  }
  //generating certs using kops
  provisioner "local-exec" {
    command = "${var.kops_executable_name} create secret --name ${var.k8s_cluster_name} --state s3://${var.s3_bucket_name} sshpublickey admin -i ${local.kops_public_key_path}"
  }
  provisioner "local-exec" {
    command = "${var.kops_executable_name} update cluster ${var.k8s_cluster_name} --state s3://${var.s3_bucket_name} --target terraform"
  }

  //The --unregister flag just deletes the kops configurations stored in s3
  provisioner "local-exec" {
    command = "${var.kops_executable_name} delete cluster ${var.k8s_cluster_name} --state s3://${var.s3_bucket_name} --unregister --yes"
    when = "destroy"
  }
}


//runs each time the cluster configuration is updated
resource "null_resource" "update_cluster" {
  triggers {
    template = "${data.template_file.cluster_config.rendered}"
  }
  provisioner "local-exec" {
    command = "cat > ${local.rendered_config_path} <<EOL\n${data.template_file.cluster_config.rendered}EOL"
  }
  //updating the cluster config in S3
  provisioner "local-exec" {
    command = "${var.kops_executable_name} replace -f ${local.rendered_config_path} --state s3://${var.s3_bucket_name} --force"
  }
  depends_on = [
    "null_resource.create_cluster_configuration"]
}
