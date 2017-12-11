module "kafka-security-groups" {
  source = "security_groups"
  kafka_aws_vpc_id = "${var.kafka_aws_vpc_id}"
}
module "kafka-iam-roles" {
  source = "iam_role"
}


resource "aws_autoscaling_group" "brokers-haystack-kafka" {
  name = "brokers.haystack-k8s"
  launch_configuration = "${aws_launch_configuration.broker-haystack-kafka.id}"
  max_size = "${var.kafka_broker_count}"
  min_size = "${var.kafka_broker_count}"
  vpc_zone_identifier = [
    "${var.kafka_aws_subnet}"]

  tag = {
    key = "Name"
    value = "haystack-kafka-asg"
    propagate_at_launch = true
  }
}


resource "aws_launch_configuration" "broker-haystack-kafka" {
  name_prefix = "broker.haystack-kafka"
  image_id = "${var.kafka_base_ami["${var.kafka_aws_region}"]}"
  instance_type = "${var.kafka_broker_instance_type}"
  key_name = "${var.kafka_aws_ssh_key}"
  iam_instance_profile = "${module.kafka-iam-roles.iam-instance-profile_arn}"
  security_groups = [
    "${module.kafka-security-groups.broker_security_group_ids}"]
  associate_public_ip_address = false
  user_data = "${file("${path.module}/data/aws_launch_configuration_brokers.haystack-kafka_user_data")}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = 128
    delete_on_termination = true
  }

  lifecycle = {
    create_before_destroy = true
  }
}
