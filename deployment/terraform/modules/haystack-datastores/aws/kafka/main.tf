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
  zookeeper_cname = "${var.haystack_cluster_name}-zookeeper"
  kafka_cname = "${var.haystack_cluster_name}-kafka"
  kafka_port = "9092"
}

module "kafka-security-groups" {
  source = "security_groups"
  aws_vpc_id = "${var.aws_vpc_id}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}

resource "aws_iam_role" "haystack-zookeeper-role" {
  name = "${var.haystack_cluster_name}-zookeeper-role"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": { "Service": "ec2.amazonaws.com"},
      "Action": "sts:AssumeRole"
    }
  ]
}
  EOF
}

resource "aws_iam_role_policy" "zookeeper-policy" {
  name = "zookeeper-policy"
  role = "${aws_iam_role.haystack-zookeeper-role.name}"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "zookeeperRoute53ListZones",
      "Effect": "Allow",
      "Action": [
        "ec2:DescribeInstances"
      ],
      "Resource": [
        "*"
      ]
    }
  ]
}
  EOF
}

resource "aws_iam_instance_profile" "haystack-zookeeper-profile" {
  name = "${var.haystack_cluster_name}-zookeeper"
  role = "${aws_iam_role.haystack-zookeeper-role.name}"
}

data "template_file" "zookeeper_user_data" {
  template = "${file("${path.module}/data/zookeeper_user_data_sh.tpl")}"

  vars {
    role = "${var.haystack_cluster_name}-kafka-zookeeper"
    zk_node_count = "${var.zookeeper_count}"
    haystack_graphite_host = "${var.aws_graphite_host}"
    haystack_graphite_port = "${var.aws_graphite_port}"
  }
}

// create zookeeper cluster
resource "aws_instance" "haystack-zookeeper-nodes" {
  count = "${var.zookeeper_count}"
  ami = "${local.kafka_broker_ami}"
  instance_type = "${var.broker_instance_type}"
  #subnet_id = "${var.aws_subnet}"
  subnet_id = "${element(var.aws_subnets, count.index)}"
  vpc_security_group_ids = [ "${module.kafka-security-groups.kafka_broker_security_group_ids}"]
  associate_public_ip_address = false
  key_name = "${var.aws_ssh_key_pair_name}"
  iam_instance_profile = "${aws_iam_instance_profile.haystack-zookeeper-profile.name}"

  tags = {
    Product = "Haystack"
    Component = "Kafka"
    ClusterName = "${var.haystack_cluster_name}"
    Role = "${var.haystack_cluster_name}-kafka-zookeeper"
    Name = "${var.haystack_cluster_name}-kafka-zookeeper-${count.index}"
  }

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.zookeeper_volume_size}"
    delete_on_termination = false
  }

  lifecycle {
    ignore_changes = ["ami"]
  }

  user_data = "${data.template_file.zookeeper_user_data.rendered}"
}

data "template_file" "kafka_broker_user_data" {
  template = "${file("${path.module}/data/kafka_broker_user_data_sh.tpl")}"

  vars {
    haystack_graphite_host = "${var.aws_graphite_host}"
    haystack_graphite_port = "${var.aws_graphite_port}"
    zookeeper_hosts = "${join(",", formatlist("%s:2181", aws_instance.haystack-zookeeper-nodes.*.private_ip))}"
    num_partitions = "${var.default_partition_count}"
    retention_hours = "24"
    retention_bytes = "1073741824"
  }
}

// create kafka brokers
resource "aws_instance" "haystack-kafka-broker" {
  count = "${var.broker_count}"
  ami = "${local.kafka_broker_ami}"
  instance_type = "${var.broker_instance_type}"
  #subnet_id = "${var.aws_subnet}"
  subnet_id = "${element(var.aws_subnets, count.index)}"
  vpc_security_group_ids = [ "${module.kafka-security-groups.kafka_broker_security_group_ids}"]
  key_name = "${var.aws_ssh_key_pair_name}"
  associate_public_ip_address = false
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

  lifecycle {
    ignore_changes = ["ami"]
  }

  user_data = "${data.template_file.kafka_broker_user_data.rendered}"
}

// create cname for newly created zookeeper cluster
resource "aws_route53_record" "haystack-zookeeper-cname" {
  zone_id = "${var.aws_hosted_zone_id}"
  name    = "${local.zookeeper_cname}"
  type    = "A"
  ttl     = "300"
  records = ["${aws_instance.haystack-zookeeper-nodes.*.private_ip}"]
}

// create cname for newly created kafka cluster
resource "aws_route53_record" "haystack-kafka-cname" {
  zone_id = "${var.aws_hosted_zone_id}"
  name    = "${local.kafka_cname}"
  type    = "A"
  ttl     = "300"
  records = ["${aws_instance.haystack-kafka-broker.*.private_ip}"]
}
