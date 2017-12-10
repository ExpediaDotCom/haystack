module "k8s_security_groups" {
  source = "security-groups"
  k8s_vpc_id = "${var.k8s_aws_vpc_id}"
}
module "k8s_iam_roles" {
  source = "iam-roles"
}

module "k8s_elbs" {
  source = "elbs"
  k8s_elb_api_security_group = "${module.k8s_security_groups.api-elb-security_group_ids}"
  k8s_elb_subnet = "${var.k8s_aws_external_master_subnet_ids}"
  k8s_hosted_zone_id = "${var.k8s_hosted_zone_id}"
  k8s_cluster_name = "${var.k8s_cluster_name}"
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
    "${var.k8s_aws_external_master_subnet_ids}"]

  tag = {
    key = "KubernetesCluster"
    value = "haystack-k8s"
    propagate_at_launch = true
  }

  tag = {
    key = "Name"
    value = "master-1.masters.haystack-k8s"
    propagate_at_launch = true
  }

  tag = {
    key = "k8s.io/cluster-autoscaler/node-template/label/kops.k8s.io/instancegroup"
    value = "master-1"
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
    "${var.k8s_aws_external_master_subnet_ids}"]

  tag = {
    key = "KubernetesCluster"
    value = "haystack-k8s"
    propagate_at_launch = true
  }

  tag = {
    key = "Name"
    value = "master-2.masters.haystack-k8s"
    propagate_at_launch = true
  }

  tag = {
    key = "k8s.io/cluster-autoscaler/node-template/label/kops.k8s.io/instancegroup"
    value = "master-2"
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
    "${var.k8s_aws_external_master_subnet_ids}"]

  tag = {
    key = "KubernetesCluster"
    value = "haystack-k8s"
    propagate_at_launch = true
  }

  tag = {
    key = "Name"
    value = "master-3.masters.haystack-k8s"
    propagate_at_launch = true
  }

  tag = {
    key = "k8s.io/cluster-autoscaler/node-template/label/kops.k8s.io/instancegroup"
    value = "master-3"
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
    "${var.k8s_aws_external_worker_subnet_ids}"]

  tag = {
    key = "KubernetesCluster"
    value = "haystack-k8s"
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


resource "aws_launch_configuration" "master-1-masters-haystack-k8s" {
  name_prefix = "master-1.masters.haystack-k8s"
  image_id = "${var.k8s_master_ami}"
  instance_type = "${var.k8s_master_instance_type}"
  key_name = "${var.k8s_aws_ssh_key}"
  iam_instance_profile = "${module.k8s_iam_roles.masters_iam-instance-profile_arn}"
  security_groups = [
    "${module.k8s_security_groups.master_security_group_ids}"]
  associate_public_ip_address = false
  user_data = "${file("${path.module}/data/aws_launch_configuration_master-1.masters.haystack-k8s_user_data")}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = 64
    delete_on_termination = true
  }

  lifecycle = {
    create_before_destroy = true
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
  user_data = "${file("${path.module}/data/aws_launch_configuration_master-2.masters.haystack-k8s_user_data")}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = 64
    delete_on_termination = true
  }


  lifecycle = {
    create_before_destroy = true
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
  user_data = "${file("${path.module}/data/aws_launch_configuration_master-3.masters.haystack-k8s_user_data")}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = 64
    delete_on_termination = true
  }
  lifecycle = {
    create_before_destroy = true
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
  user_data = "${file("${path.module}/data/aws_launch_configuration_nodes.haystack-k8s_user_data")}"

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
