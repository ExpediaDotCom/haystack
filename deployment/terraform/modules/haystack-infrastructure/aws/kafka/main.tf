locals {
  kafka_port = 9092
}
module "kafka-security-groups" {
  source = "security_groups"
  kafka_aws_vpc_id = "${var.aws_vpc_id}"
}
module "kafka-iam-roles" {
  source = "iam_role"
}


resource "aws_autoscaling_group" "brokers-haystack-kafka" {
  name = "brokers.${var.cluster_name}-${count.index}"
  launch_configuration = "${aws_launch_configuration.broker-haystack-kafka.id}"
  max_size = "${var.broker_count}"
  min_size = "${var.broker_count}"
  vpc_zone_identifier = [
    "${var.aws_subnet}"]

  tag = {
    key = "Name"
    value = "haystack-kafka-asg"
    propagate_at_launch = true
  }
}
data "template_file" "kafka_broker_user_data" {
  template = "${file("${path.module}/templates/haystack-kafka_broker_user_data.tpl")}"

  vars {
    cluster_name = "${var.cluster_name}"
  }
}


resource "aws_launch_configuration" "broker-haystack-kafka" {
  name_prefix = "broker.${var.cluster_name}"
  image_id = "${var.kafka_base_ami["${var.aws_region}"]}"
  instance_type = "${var.broker_instance_type}"
  key_name = "${var.aws_ssh_key}"
  iam_instance_profile = "${module.kafka-iam-roles.iam-instance-profile_arn}"
  security_groups = [
    "${module.kafka-security-groups.broker_security_group_ids}"]
  associate_public_ip_address = false
  user_data = "${data.template_file.kafka_broker_user_data.rendered}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = 128
    delete_on_termination = true
  }

  lifecycle = {
    create_before_destroy = true
  }
}
resource "aws_elb" "kafka-broker-elb" {
  name = "haystack-kafka-broker-elb"

  listener = {
    instance_port = "${local.kafka_port}"
    instance_protocol = "TCP"
    lb_port = "${local.kafka_port}"
    lb_protocol = "TCP"
  }

  security_groups = [
    "${module.kafka-security-groups.broker_security_group_ids}"]
  subnets = [
    "${var.aws_subnet}"]

  health_check = {
    target = "SSL:${local.kafka_port}"
    healthy_threshold = 2
    unhealthy_threshold = 2
    interval = 10
    timeout = 5
  }

  idle_timeout = 300

  tags {
    ClusterName = "${var.cluster_name}"
    Product = "Haystack"
  }
}


resource "aws_autoscaling_attachment" "broker-haystack-kafka" {
  elb = "${aws_elb.kafka-broker-elb.id}"
  autoscaling_group_name = "${aws_autoscaling_group.brokers-haystack-kafka.id}"
}

// create cname for newly created kafka cluster
resource "aws_route53_record" "haystack-kafka-cname" {
  zone_id = "${var.aws_hosted_zone_id}"
  name = "${var.cluster_name}"
  type = "A"
  ttl = "300"
  records = [
    "${aws_elb.kafka-broker-elb.dns_name}"]
}
