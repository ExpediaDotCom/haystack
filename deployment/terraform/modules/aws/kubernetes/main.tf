//we can't currently update the cluster name, TODO: make it configurable
locals {
  k8s_cluster_name = "haystack-k8s.${var.k8s_base_domain_name}"
  k8s_master_1_instance_group_name = "master-${var.k8s_aws_zone}-1"
  k8s_master_2_instance_group_name = "master-${var.k8s_aws_zone}-2"
  k8s_master_3_instance_group_name = "master-${var.k8s_aws_zone}-3"
  k8s_nodes_instance_group_name = "nodes"

}

module "kops" {
  source = "kops"
  k8s_version = "${var.k8s_version}"
  k8s_aws_vpc_id = "${var.k8s_aws_vpc_id}"
  k8s_node_instance_count = "${var.k8s_node_instance_count}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  k8s_master_instance_type = "${var.k8s_master_instance_type}"
  kops_executable_name = "${var.kops_executable_name}"
  k8s_node_instance_type = "${var.k8s_node_instance_type}"
  k8s_s3_bucket_name = "${var.k8s_s3_bucket_name}"
  k8s_hosted_zone_id = "${var.k8s_hosted_zone_id}"
  k8s_aws_zone = "${var.k8s_aws_zone}"
  k8s_aws_nodes_subnet = "${var.k8s_aws_nodes_subnet_ids}"
  k8s_aws_utilities_subnet = "${var.k8s_aws_utility_subnet_ids}"
}

module "addons" {
  source = "addons"
  k8s_aws_region = "${var.k8s_aws_region}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  k8s_logs_es_url = "${var.k8s_logs_es_url}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
}

module "k8s_aws_ebs" {
  source = "ebs"
  k8s_cluster_name = "${local.k8s_cluster_name}"
}

module "k8s_security_groups" {
  source = "security-groups"
  k8s_vpc_id = "${var.k8s_aws_vpc_id}"
}
module "k8s_iam_roles" {
  source = "iam-roles"
}


module "k8s_elbs" {
  source = "elbs"
  k8s_elb_api_security_groups = "${module.k8s_security_groups.api-elb-security_group_ids}"
  k8s_elb_subnet = "${var.k8s_aws_utility_subnet_ids}"
  k8s_hosted_zone_id = "${var.k8s_hosted_zone_id}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  k8s_nodes_api_security_groups = "${module.k8s_security_groups.nodes-api-elb-security_group_ids}"
}

resource "aws_autoscaling_attachment" "master-1-masters-haystack-k8s" {
  elb = "${module.k8s_elbs.api-elb-id}"
  autoscaling_group_name = "${aws_autoscaling_group.master-1-masters-haystack-k8s.id}"
}

resource "aws_autoscaling_attachment" "master-2-masters-haystack-k8s" {
  elb = "${module.k8s_elbs.api-elb-id}"
  autoscaling_group_name = "${aws_autoscaling_group.master-2-masters-haystack-k8s.id}"
}

resource "aws_autoscaling_attachment" "master-3-masters-haystack-k8s" {
  elb = "${module.k8s_elbs.api-elb-id}"
  autoscaling_group_name = "${aws_autoscaling_group.master-3-masters-haystack-k8s.id}"
}


resource "aws_autoscaling_attachment" "nodes-haystack-k8s" {
  elb = "${module.k8s_elbs.nodes-elb-id}"
  autoscaling_group_name = "${aws_autoscaling_group.nodes-haystack-k8s.id}"
}


resource "aws_autoscaling_group" "master-1-masters-haystack-k8s" {
  name = "master-1.masters.haystack-k8s"
  launch_configuration = "${aws_launch_configuration.master-1-masters-haystack-k8s.id}"
  max_size = 1
  min_size = 1
  vpc_zone_identifier = [
    "${var.k8s_aws_nodes_subnet_ids}"]

  tag = {
    key = "KubernetesCluster"
    value = "${local.k8s_cluster_name}"
    propagate_at_launch = true
  }

  tag = {
    key = "Name"
    value = "master-1.masters.haystack-k8s"
    propagate_at_launch = true
  }

  tag = {
    key = "k8s.io/cluster-autoscaler/node-template/label/kops.k8s.io/instancegroup"
    value = "${local.k8s_master_1_instance_group_name}"
    propagate_at_launch = true
  }

  tag = {
    key = "k8s.io/role/master"
    value = "1"
    propagate_at_launch = true
  }
}

resource "aws_autoscaling_group" "master-2-masters-haystack-k8s" {
  name = "master-2.masters.haystack-k8s"
  launch_configuration = "${aws_launch_configuration.master-2-masters-haystack-k8s.id}"
  max_size = 1
  min_size = 1
  vpc_zone_identifier = [
    "${var.k8s_aws_nodes_subnet_ids}"]

  tag = {
    key = "KubernetesCluster"
    value = "${local.k8s_cluster_name}"
    propagate_at_launch = true
  }

  tag = {
    key = "Name"
    value = "master-2.masters.haystack-k8s"
    propagate_at_launch = true
  }

  tag = {
    key = "k8s.io/cluster-autoscaler/node-template/label/kops.k8s.io/instancegroup"
    value = "${local.k8s_master_2_instance_group_name}"
    propagate_at_launch = true
  }

  tag = {
    key = "k8s.io/role/master"
    value = "1"
    propagate_at_launch = true
  }
}

resource "aws_autoscaling_group" "master-3-masters-haystack-k8s" {
  name = "master-3.masters.haystack-k8s"
  launch_configuration = "${aws_launch_configuration.master-3-masters-haystack-k8s.id}"
  max_size = 1
  min_size = 1
  vpc_zone_identifier = [
    "${var.k8s_aws_nodes_subnet_ids}"]

  tag = {
    key = "KubernetesCluster"
    value = "${local.k8s_cluster_name}"
    propagate_at_launch = true
  }

  tag = {
    key = "Name"
    value = "master-3.masters.haystack-k8s"
    propagate_at_launch = true
  }

  tag = {
    key = "k8s.io/cluster-autoscaler/node-template/label/kops.k8s.io/instancegroup"
    value = "${local.k8s_master_3_instance_group_name}"
    propagate_at_launch = true
  }

  tag = {
    key = "k8s.io/role/master"
    value = "1"
    propagate_at_launch = true
  }
}

resource "aws_autoscaling_group" "nodes-haystack-k8s" {
  name = "nodes.haystack-k8s"
  launch_configuration = "${aws_launch_configuration.nodes-haystack-k8s.id}"
  max_size = "${var.k8s_node_instance_count}"
  min_size = "${var.k8s_node_instance_count}"
  vpc_zone_identifier = [
    "${var.k8s_aws_nodes_subnet_ids}"]

  tag = {
    key = "KubernetesCluster"
    value = "${local.k8s_cluster_name}"
    propagate_at_launch = true
  }

  tag = {
    key = "Name"
    value = "nodes.haystack-k8s"
    propagate_at_launch = true
  }

  tag = {
    key = "k8s.io/cluster-autoscaler/node-template/label/kops.k8s.io/instancegroup"
    value = "nodes"
    propagate_at_launch = true
  }

  tag = {
    key = "k8s.io/role/node"
    value = "1"
    propagate_at_launch = true
  }
}

resource "aws_eip" "eip-haystack-k8s" {
  vpc = true
}


data "template_file" "master-1-user-data" {
  template = "${file("${path.module}/templates/k8s_master_user-data.tpl")}"
  vars {
    cluster_name = "${local.k8s_cluster_name}"
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
    instance_group_name = "${local.k8s_master_1_instance_group_name}"
  }
}
resource "aws_launch_configuration" "master-1-masters-haystack-k8s" {
  name_prefix = "master-1.masters.haystack-k8s"
  image_id = "${var.k8s_master_ami}"
  instance_type = "${var.k8s_master_instance_type}"
  key_name = "${var.k8s_aws_ssh_key}"
  iam_instance_profile = "${module.k8s_iam_roles.masters_iam-instance-profile_arn}"
  security_groups = [
    "${module.k8s_security_groups.master_security_group_ids}"]
  associate_public_ip_address = false
  user_data = "${data.template_file.master-1-user-data.rendered}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = 64
    delete_on_termination = true
  }

  lifecycle = {
    create_before_destroy = true
  }
}


data "template_file" "master-2-user-data" {
  template = "${file("${path.module}/templates/k8s_master_user-data.tpl")}"
  vars {
    cluster_name = "${local.k8s_cluster_name}"
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
    instance_group_name = "${local.k8s_master_2_instance_group_name}"
  }
}
resource "aws_launch_configuration" "master-2-masters-haystack-k8s" {
  name_prefix = "master-2.masters.haystack-k8s"
  image_id = "${var.k8s_master_ami}"
  instance_type = "${var.k8s_master_instance_type}"
  key_name = "${var.k8s_aws_ssh_key}"
  iam_instance_profile = "${module.k8s_iam_roles.masters_iam-instance-profile_arn}"
  security_groups = [
    "${module.k8s_security_groups.master_security_group_ids}"]
  associate_public_ip_address = false
  user_data = "${data.template_file.master-2-user-data.rendered}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = 64
    delete_on_termination = true
  }


  lifecycle = {
    create_before_destroy = true
  }
}


data "template_file" "master-3-user-data" {
  template = "${file("${path.module}/templates/k8s_master_user-data.tpl")}"
  vars {
    cluster_name = "${local.k8s_cluster_name}"
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
    instance_group_name = "${local.k8s_master_3_instance_group_name}"
  }
}
resource "aws_launch_configuration" "master-3-masters-haystack-k8s" {
  name_prefix = "master-3.masters.haystack-k8s"
  image_id = "${var.k8s_master_ami}"
  instance_type = "${var.k8s_master_instance_type}"
  key_name = "${var.k8s_aws_ssh_key}"
  iam_instance_profile = "${module.k8s_iam_roles.masters_iam-instance-profile_arn}"
  security_groups = [
    "${module.k8s_security_groups.master_security_group_ids}"]
  associate_public_ip_address = false
  user_data = "${data.template_file.master-3-user-data.rendered}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = 64
    delete_on_termination = true
  }
  lifecycle = {
    create_before_destroy = true
  }
}

data "template_file" "nodes-user-data" {
  template = "${file("${path.module}/templates/k8s_nodes_user-data.tpl")}"
  vars {
    cluster_name = "${local.k8s_cluster_name}"
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
    instance_group_name = "${local.k8s_nodes_instance_group_name}"
  }
}
resource "aws_launch_configuration" "nodes-haystack-k8s" {
  name_prefix = "nodes.haystack-k8s"
  image_id = "${var.k8s_node_ami}"
  instance_type = "${var.k8s_node_instance_type}"
  key_name = "${var.k8s_aws_ssh_key}"
  iam_instance_profile = "${module.k8s_iam_roles.nodes_iam-instance-profile_arn}"
  security_groups = [
    "${module.k8s_security_groups.node_security_group_ids}"]
  associate_public_ip_address = false
  user_data = "${data.template_file.nodes-user-data.rendered}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = 128
    delete_on_termination = true
  }

  lifecycle = {
    create_before_destroy = true
  }
}


terraform = {
  required_version = ">= 0.9.3"
}
