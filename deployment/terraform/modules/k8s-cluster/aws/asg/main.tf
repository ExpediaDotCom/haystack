//we can't currently update the cluster name, TODO: make it configurable
locals {
  k8s_master_1_instance_group_name = "master-${var.k8s_aws_zone}-1"
  k8s_master_2_instance_group_name = "master-${var.k8s_aws_zone}-2"
  k8s_master_3_instance_group_name = "master-${var.k8s_aws_zone}-3"
  k8s_nodes_instance_group_name = "nodes"
}


resource "aws_autoscaling_group" "master-1" {
  name = "${var.haystack_cluster_name}-master-1}"
  launch_configuration = "${aws_launch_configuration.master-1.id}"
  max_size = 1
  min_size = 1
  vpc_zone_identifier = [
    "${var.k8s_aws_nodes_subnet_ids}"]

  tag = {
    key = "KubernetesCluster"
    value = "${var.k8s_cluster_name}"
    propagate_at_launch = true
  }

  tag = {
    key = "Name"
    value = "${local.k8s_master_2_instance_group_name}"
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
  depends_on = [
    "aws_ebs_volume.1-etcd-events",
    "aws_ebs_volume.1-etcd-main",
    "aws_ebs_volume.2-etcd-events",
    "aws_ebs_volume.2-etcd-main",
    "aws_ebs_volume.3-etcd-events",
    "aws_ebs_volume.3-etcd-main"]
}

resource "aws_autoscaling_group" "master-2" {
  name = "${var.haystack_cluster_name}-master-2}"
  launch_configuration = "${aws_launch_configuration.master-2.id}"
  max_size = 1
  min_size = 1
  vpc_zone_identifier = [
    "${var.k8s_aws_nodes_subnet_ids}"]

  tag = {
    key = "KubernetesCluster"
    value = "${var.k8s_cluster_name}"
    propagate_at_launch = true
  }

  tag = {
    key = "Name"
    value = "${local.k8s_master_2_instance_group_name}"
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
  depends_on = [
    "aws_ebs_volume.1-etcd-events",
    "aws_ebs_volume.1-etcd-main",
    "aws_ebs_volume.2-etcd-events",
    "aws_ebs_volume.2-etcd-main",
    "aws_ebs_volume.3-etcd-events",
    "aws_ebs_volume.3-etcd-main"]
}

resource "aws_autoscaling_group" "master-3" {
  name = "${var.haystack_cluster_name}-master-3}"
  launch_configuration = "${aws_launch_configuration.master-3.id}"
  max_size = 1
  min_size = 1
  vpc_zone_identifier = [
    "${var.k8s_aws_nodes_subnet_ids}"]

  tag = {
    key = "KubernetesCluster"
    value = "${var.k8s_cluster_name}"
    propagate_at_launch = true
  }

  tag = {
    key = "Name"
    value = "${local.k8s_master_3_instance_group_name}"
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
  depends_on = [
    "aws_ebs_volume.1-etcd-events",
    "aws_ebs_volume.1-etcd-main",
    "aws_ebs_volume.2-etcd-events",
    "aws_ebs_volume.2-etcd-main",
    "aws_ebs_volume.3-etcd-events",
    "aws_ebs_volume.3-etcd-main"]
}

resource "aws_autoscaling_group" "nodes" {
  name = "${var.haystack_cluster_name}-nodes"
  launch_configuration = "${aws_launch_configuration.nodes.id}"
  max_size = "${var.k8s_node_instance_count}"
  min_size = "${var.k8s_node_instance_count}"
  vpc_zone_identifier = [
    "${var.k8s_aws_nodes_subnet_ids}"]

  tag = {
    key = "KubernetesCluster"
    value = "${var.k8s_cluster_name}"
    propagate_at_launch = true
  }

  tag = {
    key = "Name"
    value = "nodes.${var.k8s_cluster_name}"
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


data "template_file" "master-1-user-data" {
  template = "${file("${path.module}/templates/k8s_master_user-data.tpl")}"
  vars {
    cluster_name = "${var.k8s_cluster_name}"
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
    instance_group_name = "${local.k8s_master_1_instance_group_name}"
  }
}
resource "aws_launch_configuration" "master-1" {
  name_prefix = "${var.haystack_cluster_name}-master-1}"
  image_id = "${var.k8s_master_ami}"
  instance_type = "${var.k8s_master_instance_type}"
  key_name = "${var.k8s_aws_ssh_key}"
  iam_instance_profile = "${var.master_iam-instance-profile_arn}"
  security_groups = [
    "${var.master_security_groups}"]
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
    cluster_name = "${var.k8s_cluster_name}"
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
    instance_group_name = "${local.k8s_master_2_instance_group_name}"
  }
}

resource "aws_launch_configuration" "master-2" {
  name_prefix = "${var.haystack_cluster_name}-master-2}"
  image_id = "${var.k8s_master_ami}"
  instance_type = "${var.k8s_master_instance_type}"
  key_name = "${var.k8s_aws_ssh_key}"
  iam_instance_profile = "${var.master_iam-instance-profile_arn}"
  security_groups = [
    "${var.master_security_groups}"]
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
    cluster_name = "${var.k8s_cluster_name}"
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
    instance_group_name = "${local.k8s_master_3_instance_group_name}"
  }
}

resource "aws_launch_configuration" "master-3" {
  name_prefix = "${var.haystack_cluster_name}-master-3}"
  image_id = "${var.k8s_master_ami}"
  instance_type = "${var.k8s_master_instance_type}"
  key_name = "${var.k8s_aws_ssh_key}"
  iam_instance_profile = "${var.master_iam-instance-profile_arn}"
  security_groups = [
    "${var.master_security_groups}"]
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
    cluster_name = "${var.k8s_cluster_name}"
    s3_bucket_name = "${var.k8s_s3_bucket_name}"
    instance_group_name = "${local.k8s_nodes_instance_group_name}"
  }
}

resource "aws_launch_configuration" "nodes" {
  name_prefix = "nodes"
  image_id = "${var.k8s_node_ami}"
  instance_type = "${var.k8s_node_instance_type}"
  key_name = "${var.k8s_aws_ssh_key}"
  iam_instance_profile = "${var.nodes_iam-instance-profile_arn}"
  security_groups = [
    "${var.node_security_groups}"]
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

resource "aws_ebs_volume" "1-etcd-events" {
  availability_zone = "us-west-2c"
  size = 20
  type = "gp2"
  encrypted = false

  tags = {
    KubernetesCluster = "${var.k8s_cluster_name}"
    Name = "1.etcd-events.${var.k8s_cluster_name}"
    "k8s.io/etcd/events" = "1/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "1-etcd-main" {
  availability_zone = "us-west-2c"
  size = 20
  type = "gp2"
  encrypted = false

  tags = {
    KubernetesCluster = "${var.k8s_cluster_name}"
    Name = "1.etcd-main.${var.k8s_cluster_name}"
    "k8s.io/etcd/main" = "1/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "2-etcd-events" {
  availability_zone = "us-west-2c"
  size = 20
  type = "gp2"
  encrypted = false

  tags = {
    KubernetesCluster = "${var.k8s_cluster_name}"
    Name = "2.etcd-events.${var.k8s_cluster_name}"
    "k8s.io/etcd/events" = "2/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "2-etcd-main" {
  availability_zone = "us-west-2c"
  size = 20
  type = "gp2"
  encrypted = false

  tags = {
    KubernetesCluster = "${var.k8s_cluster_name}"
    Name = "2.etcd-main.${var.k8s_cluster_name}"
    "k8s.io/etcd/main" = "2/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "3-etcd-events" {
  availability_zone = "us-west-2c"
  size = 20
  type = "gp2"
  encrypted = false

  tags = {
    KubernetesCluster = "${var.k8s_cluster_name}"
    Name = "3.etcd-events.${var.k8s_cluster_name}"
    "k8s.io/etcd/events" = "3/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "3-etcd-main" {
  availability_zone = "us-west-2c"
  size = 20
  type = "gp2"
  encrypted = false

  tags = {
    KubernetesCluster = "${var.k8s_cluster_name}"
    Name = "3.etcd-main.${var.k8s_cluster_name}"
    "k8s.io/etcd/main" = "3/1,2,3"
    "k8s.io/role/master" = "1"
  }
}
