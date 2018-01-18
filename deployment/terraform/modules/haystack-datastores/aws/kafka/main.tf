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
  kafka_broker_ami = "${var.broker_image == "" ? data.aws_ami.haystack-kafka-base-ami.image_id : var.broker_image }"
  kafka_cname = "${var.haystack_cluster_name}-kafka"
  kafka_port = "9092"
}

module "kafka-security-groups" {
  source = "security_groups"
  aws_vpc_id= "${var.aws_vpc_id}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}

data "template_file" "kafka_broker_user_data" {
  template = "${file("${path.module}/data/kafka_broker_user_data_sh.tpl")}"

  vars {
    haystack_graphite_host = "${var.aws_graphite_host}"
    haystack_graphite_port = "${var.aws_graphite_port}"
    zookeeper_hosts = "localhost:2181"
    num_partitions = "96"
    retention_hours = "24"
    retention_bytes = "${var.broker_volume_size * 805306368}"
  }
}

// create kafka brokers
resource "aws_instance" "haystack-kafka-broker" {
  count = "${var.broker_count}"
  ami = "${local.kafka_broker_ami}"
  instance_type = "${var.broker_instance_type}"
  subnet_id = "${var.aws_subnet}"
  security_groups = [ "${module.kafka-security-groups.kafka_broker_security_group_ids}"]
  key_name = "${var.aws_ssh_key_pair_name}"

  tags = {
    Product = "Haystack"
    Component = "Kafka"
    ClusterName = "${var.haystack_cluster_name}"
    Role = "${var.haystack_cluster_name}-kafka-brokers"
    Name = "${var.haystack_cluster_name}-kafka-brokers-${count.index}"
  }
  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.broker_volume_size}"
    delete_on_termination = false
  }

  user_data = "${data.template_file.kafka_broker_user_data.rendered}"
}

// create cname for newly created kafka cluster
resource "aws_route53_record" "haystack-kafka-cname" {
  zone_id = "${var.aws_hosted_zone_id}"
  name    = "${local.kafka_cname}"
  type    = "A"
  ttl     = "300"
  records = ["${aws_instance.haystack-kafka-broker.private_ip}"]
}
