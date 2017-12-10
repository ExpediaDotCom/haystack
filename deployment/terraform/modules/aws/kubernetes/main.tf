module "k8s_security_groups" {
  source = "security-groups"
  k8s_vpc_id = "${var.k8s_aws_vpc_id}"
}
module "k8s_iam_roles" {
  source = "iam-roles"
}


resource "aws_autoscaling_attachment" "master-1-masters-haystack-k8s" {
  elb = "${aws_elb.api-haystack-k8s.id}"
  autoscaling_group_name = "${aws_autoscaling_group.master-1-masters-haystack-k8s.id}"
}

resource "aws_autoscaling_attachment" "master-2-masters-haystack-k8s" {
  elb = "${aws_elb.api-haystack-k8s.id}"
  autoscaling_group_name = "${aws_autoscaling_group.master-2-masters-haystack-k8s.id}"
}

resource "aws_autoscaling_attachment" "master-3-masters-haystack-k8s" {
  elb = "${aws_elb.api-haystack-k8s.id}"
  autoscaling_group_name = "${aws_autoscaling_group.master-3-masters-haystack-k8s.id}"
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
  max_size = 5
  min_size = 5
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

resource "aws_elb" "api-haystack-k8s" {
  name = "api-haystack-k8s-7kferu"

  listener = {
    instance_port = 443
    instance_protocol = "TCP"
    lb_port = 443
    lb_protocol = "TCP"
  }

  security_groups = [
    "${module.k8s_security_groups.api-elb-security_group_ids}"]
  subnets = [
    "${var.k8s_aws_external_master_subnet_ids}"]
  internal = true

  health_check = {
    target = "SSL:443"
    healthy_threshold = 2
    unhealthy_threshold = 2
    interval = 10
    timeout = 5
  }

  idle_timeout = 300

  tags = {
    KubernetesCluster = "haystack-k8s"
    Name = "api.haystack-k8s"
  }
}


resource "aws_launch_configuration" "master-1-masters-haystack-k8s" {
  name_prefix = "master-1.masters.haystack-k8s"
  image_id = "ami-06a57e7e"
  instance_type = "m3.medium"
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

  ephemeral_block_device = {
    device_name = "/dev/sdc"
    virtual_name = "ephemeral0"
  }

  lifecycle = {
    create_before_destroy = true
  }
}

resource "aws_launch_configuration" "master-2-masters-haystack-k8s" {
  name_prefix = "master-2.masters.haystack-k8s"
  image_id = "ami-06a57e7e"
  instance_type = "m3.medium"
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

  ephemeral_block_device = {
    device_name = "/dev/sdc"
    virtual_name = "ephemeral0"
  }

  lifecycle = {
    create_before_destroy = true
  }
}

resource "aws_launch_configuration" "master-3-masters-haystack-k8s" {
  name_prefix = "master-3.masters.haystack-k8s"
  image_id = "ami-06a57e7e"
  instance_type = "m3.medium"
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

  ephemeral_block_device = {
    device_name = "/dev/sdc"
    virtual_name = "ephemeral0"
  }

  lifecycle = {
    create_before_destroy = true
  }
}

resource "aws_launch_configuration" "nodes-haystack-k8s" {
  name_prefix = "nodes.haystack-k8s"
  image_id = "ami-06a57e7e"
  instance_type = "t2.medium"
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


resource "aws_route53_record" "api-haystack-k8s" {
  name = "api.haystack-k8s.${var.k8s_base_domain_name}"
  type = "A"

  alias = {
    name = "${aws_elb.api-haystack-k8s.dns_name}"
    zone_id = "${aws_elb.api-haystack-k8s.zone_id}"
    evaluate_target_health = false
  }

  zone_id = "/hostedzone/Z3HD2JQ3E1K3F8"
}

terraform = {
  required_version = ">= 0.9.3"
}
