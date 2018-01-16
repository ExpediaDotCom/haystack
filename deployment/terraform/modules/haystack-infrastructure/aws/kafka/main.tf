// using data-source to find latest kafka image
data "aws_ami" "haystack-kafka-base-ami" {
  filter {
    name   = "state"
    values = ["available"]
  }

  filter {
    name   = "tag:type"
    values = ["haystack-kafka-base"]
  }

  most_recent = true
}

locals {
  kafka_broker_ami = "${var.kafka_broker_image == "" ? data.aws_ami.haystack-kafka-base-ami.image_id : var.kafka_broker_image }"
  kafka_cname = "haystack-kafka"
  kafka_port = "9092"
}

module "kafka-security-groups" {
  source = "security_groups"
  kafka_aws_vpc_id= "${var.kafka_aws_vpc_id}"
}

data "template_file" "zookeeper_user_data" {
  template = "${file("${path.module}/data/zookeeper_user_data_sh.tpl")}"

  vars {
    haystack_graphite_host = "${var.kafka_graphite_host}"
    haystack_graphite_port = "${var.kafka_graphite_port}"
  }
}

// create zookeeper instance
resource "aws_instance" "zookeeper_instance" {
  count = "1"
  ami = "${local.kafka_broker_ami}"
  instance_type = "${var.kafka_broker_instance_type}"
  subnet_id = "${var.kafka_aws_subnet}"
  security_groups = [ "${module.kafka-security-groups.kafka_broker_security_group_ids}"]
  key_name = "${var.kafka_ssh_key_pair_name}"

  tags {
    Name = "haystack-zookeeper-instance"
    NodeType = "zookeeper"
  }

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.kafka_broker_volume_size}"
    delete_on_termination = false
  }

  user_data = "${data.template_file.zookeeper_user_data.rendered}"
}

data "template_file" "kafka_broker_user_data" {
  template = "${file("${path.module}/data/kafka_broker_user_data_sh.tpl")}"

  vars {
    haystack_graphite_host = "${var.kafka_graphite_host}"
    haystack_graphite_port = "${var.kafka_graphite_port}"
    zookeeper_hosts = "${aws_instance.zookeeper_instance.private_ip}:2181"
    num_partitions = "96"
    retention_hours = "24"
    retention_bytes = "${var.kafka_broker_volume_size * 805306368}"
  }
}

// create kafka brokers
resource "aws_instance" "haystack-kafka-broker" {
  count = "${var.kafka_broker_count}"
  ami = "${local.kafka_broker_ami}"
  instance_type = "${var.kafka_broker_instance_type}"
  subnet_id = "${var.kafka_aws_subnet}"
  security_groups = [ "${module.kafka-security-groups.kafka_broker_security_group_ids}"]
  key_name = "${var.kafka_ssh_key_pair_name}"

  tags {
    Name = "haystack-kafka-instance"
    NodeType = "broker"
  }

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.kafka_broker_volume_size}"
    delete_on_termination = false
  }

  user_data = "${data.template_file.kafka_broker_user_data.rendered}"
}

// create cname for newly created kafka cluster
resource "aws_route53_record" "haystack-kafka-cname" {
  zone_id = "${var.kafka_hosted_zone_id}"
  name    = "${local.kafka_cname}"
  type    = "A"
  ttl     = "300"
  records = ["${aws_instance.haystack-kafka-broker.*.private_ip}"]
}
