//we can't currently update the cluster name, TODO: make it configurable
locals {
  k8s_cluster_name = "${var.haystack_cluster_name}-k8s.${var.k8s_base_domain_name}"
  k8s_master_1_instance_group_name = "master-${var.haystack_cluster_name}-1"
  k8s_master_2_instance_group_name = "master-${var.haystack_cluster_name}-2"
  k8s_master_3_instance_group_name = "master-${var.haystack_cluster_name}-3"
  k8s_nodes_instance_group_name = "nodes-${var.haystack_cluster_name}"

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

module "k8s_security_groups" {
  source = "security-groups"
  k8s_vpc_id = "${var.k8s_aws_vpc_id}"
  reverse_proxy_port = "${var.reverse_proxy_port}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
}

module "k8s_iam_roles" {
  source = "iam-roles"
  k8s_hosted_zone_id = "${var.k8s_hosted_zone_id}"
  k8s_s3_bucket_name = "${var.k8s_s3_bucket_name}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
}

module "k8s_elbs" {
  source = "elbs"
  k8s_elb_api_security_groups = "${module.k8s_security_groups.api-elb-security_group_ids}"
  k8s_elb_subnet = "${var.k8s_aws_utility_subnet_ids}"
  k8s_hosted_zone_id = "${var.k8s_hosted_zone_id}"
  k8s_cluster_name = "${local.k8s_cluster_name}"
  k8s_nodes_api_security_groups = "${module.k8s_security_groups.nodes-api-elb-security_group_ids}"
  reverse_proxy_port = "${var.reverse_proxy_port}"
  "master-1_asg_id" = "${aws_autoscaling_group.master-1.id}"
  "master-2_asg_id" = "${aws_autoscaling_group.master-2.id}"
  "master-3_asg_id" = "${aws_autoscaling_group.master-3.id}"
  nodes_asg_id = "${aws_autoscaling_group.nodes.id}"
  kubectl_executable_name = "${var.kubectl_executable_name}"
  haystack_cluster_name  = "${var.haystack_cluster_name}"
}

resource "aws_autoscaling_group" "master-1" {
  name = "${local.k8s_master_1_instance_group_name}"
  launch_configuration = "${aws_launch_configuration.master-1.id}"
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
  name = "${local.k8s_master_2_instance_group_name}"
  launch_configuration = "${aws_launch_configuration.master-2.id}"
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
  name = "${local.k8s_master_3_instance_group_name}"
  launch_configuration = "${aws_launch_configuration.master-3.id}"
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
  name = "nodes.${local.k8s_cluster_name}"
  launch_configuration = "${aws_launch_configuration.nodes.id}"
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
    value = "nodes.${local.k8s_cluster_name}"
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

resource "aws_eip" "eip" {
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
resource "aws_launch_configuration" "master-1" {
  name_prefix = "${local.k8s_master_1_instance_group_name}"
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

resource "aws_launch_configuration" "master-2" {
  name_prefix = "${local.k8s_master_2_instance_group_name}"
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

resource "aws_launch_configuration" "master-3" {
  name_prefix = "${local.k8s_master_3_instance_group_name}"
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

resource "aws_launch_configuration" "nodes" {
  name_prefix = "nodes"
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

resource "aws_ebs_volume" "1-etcd-events" {
  availability_zone = "us-west-2c"
  size = 20
  type = "gp2"
  encrypted = false

  tags = {
    KubernetesCluster = "${local.k8s_cluster_name}"
    Name = "1.etcd-events.${local.k8s_cluster_name}"
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
    KubernetesCluster = "${local.k8s_cluster_name}"
    Name = "1.etcd-main.${local.k8s_cluster_name}"
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
    KubernetesCluster = "${local.k8s_cluster_name}"
    Name = "2.etcd-events.${local.k8s_cluster_name}"
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
    KubernetesCluster = "${local.k8s_cluster_name}"
    Name = "2.etcd-main.${local.k8s_cluster_name}"
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
    KubernetesCluster = "${local.k8s_cluster_name}"
    Name = "3.etcd-events.${local.k8s_cluster_name}"
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
    KubernetesCluster = "${local.k8s_cluster_name}"
    Name = "3.etcd-main.${local.k8s_cluster_name}"
    "k8s.io/etcd/main" = "3/1,2,3"
    "k8s.io/role/master" = "1"
  }
}
