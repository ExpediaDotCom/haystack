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
  owners = ["self"]
  most_recent = true
}

locals {
  kafka_broker_ami = "${var.kafka["broker_image"] == "" ? data.aws_ami.haystack-kafka-base-ami.image_id : var.kafka["broker_image"] }"
  zookeeper_cname = "${var.cluster["name"]}-zookeeper"
  kafka_cname = "${var.cluster["name"]}-kafka"
  kafka_port = "9092"
}

module "kafka-security-groups" {
  source = "security_groups"
  cluster = "${var.cluster}"
}

resource "aws_iam_role" "haystack-zookeeper-role" {
  name = "${var.cluster["name"]}-zookeeper-role"
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
      "Sid": "zkProvidesAccessToSSM",
      "Effect": "Allow",
      "Action": [
        "cloudwatch:PutMetricData",
        "ds:CreateComputer",
        "ds:DescribeDirectories",
        "ec2:DescribeInstanceStatus",
        "ec2:DescribeInstances",
        "logs:*",
        "ssm:*",
        "ec2messages:*"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": "iam:CreateServiceLinkedRole",
      "Resource": "arn:aws:iam::*:role/aws-service-role/ssm.amazonaws.com/AWSServiceRoleForAmazonSSM*",
      "Condition": {
        "StringLike": {
          "iam:AWSServiceName": "ssm.amazonaws.com"
        }
      }
    },
    {
      "Effect": "Allow",
      "Action": [
        "iam:DeleteServiceLinkedRole",
        "iam:GetServiceLinkedRoleDeletionStatus"
      ],
      "Resource": "arn:aws:iam::*:role/aws-service-role/ssm.amazonaws.com/AWSServiceRoleForAmazonSSM*"
    }
  ]
}
  EOF
}

resource "aws_iam_instance_profile" "haystack-zookeeper-profile" {
  name = "${var.cluster["name"]}-zookeeper"
  role = "${aws_iam_role.haystack-zookeeper-role.name}"
}

data "template_file" "zookeeper_user_data" {
  count = "${var.kafka["zookeeper_count"]}"
  template = "${file("${path.module}/data/zookeeper_user_data_sh.tpl")}"

  vars {
    role = "${var.cluster["role_prefix"]}-kafka-zookeeper"
    cluster_name = "${var.cluster["name"]}"
    zk_a_name = "${var.cluster["name"]}-zookeeper-"
    zk_node_count = "${var.kafka["zookeeper_count"]}"
    index = "${count.index}"
    haystack_graphite_host = "${var.aws_graphite_host}"
    haystack_graphite_port = "${var.aws_graphite_port}"
  }
}

// create zookeeper cluster
resource "aws_instance" "haystack-zookeeper-nodes" {
  count = "${var.kafka["zookeeper_count"]}"
  ami = "${local.kafka_broker_ami}"
  instance_type = "${var.kafka["broker_instance_type"]}"
  subnet_id = "${element(var.aws_subnets, count.index)}"
  vpc_security_group_ids = [ "${module.kafka-security-groups.kafka_broker_security_group_ids}"]
  associate_public_ip_address = false
  key_name = "${var.cluster["aws_ssh_key"]}"
  iam_instance_profile = "${aws_iam_instance_profile.haystack-zookeeper-profile.name}"

  tags = {
    Product = "Haystack"
    Component = "Kafka"
    ClusterName = "${var.cluster["name"]}"
    Role = "${var.cluster["role_prefix"]}-kafka-zookeeper"
    Name = "${var.cluster["name"]}-kafka-zookeeper-${count.index}"
  }

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.kafka["zookeeper_volume_size"]}"
    delete_on_termination = false
  }
  lifecycle {
    ignore_changes = ["ami", "user_data","subnet_id"]
  }

  user_data = "${data.template_file.zookeeper_user_data.*.rendered[count.index]}"
}

resource "aws_iam_role" "haystack-kafka-role" {
  name = "${var.cluster["name"]}-kafka-role"
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

resource "aws_iam_role_policy" "kafka-policy" {
  name = "kafka-policy"
  role = "${aws_iam_role.haystack-kafka-role.name}"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "kafkaProvidesAccessToSSM",
      "Effect": "Allow",
      "Action": [
        "cloudwatch:PutMetricData",
        "ds:CreateComputer",
        "ds:DescribeDirectories",
        "ec2:DescribeInstanceStatus",
        "logs:*",
        "ssm:*",
        "ec2messages:*"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": "iam:CreateServiceLinkedRole",
      "Resource": "arn:aws:iam::*:role/aws-service-role/ssm.amazonaws.com/AWSServiceRoleForAmazonSSM*",
      "Condition": {
        "StringLike": {
          "iam:AWSServiceName": "ssm.amazonaws.com"
        }
      }
    },
    {
      "Effect": "Allow",
      "Action": [
        "iam:DeleteServiceLinkedRole",
        "iam:GetServiceLinkedRoleDeletionStatus"
      ],
      "Resource": "arn:aws:iam::*:role/aws-service-role/ssm.amazonaws.com/AWSServiceRoleForAmazonSSM*"
    }
  ]
}
  EOF
}

resource "aws_iam_instance_profile" "haystack-kafka-profile" {
  name = "${var.cluster["name"]}-kafka"
  role = "${aws_iam_role.haystack-kafka-role.name}"
}

data "template_file" "kafka_broker_user_data" {
  count = "${var.kafka["broker_count"]}"
  template = "${file("${path.module}/data/kafka_broker_user_data_sh.tpl")}"

  vars {
    haystack_graphite_host = "${var.aws_graphite_host}"
    haystack_graphite_port = "${var.aws_graphite_port}"
    zookeeper_hosts = "${join(",", formatlist("%s:2181", aws_route53_record.haystack-zookeeper-a-records.*.name))}"
    num_partitions = "${var.kafka["default_partition_count"]}"
    retention_hours = "24"
    retention_bytes = "1073741824"
    broker_rack = "${element(var.aws_subnets, count.index)}"
  }
}

// create kafka brokers
resource "aws_instance" "haystack-kafka-broker" {
  count = "${var.kafka["broker_count"]}"
  ami = "${local.kafka_broker_ami}"
  instance_type = "${var.kafka["broker_instance_type"]}"
  subnet_id = "${element(var.aws_subnets, count.index)}"
  vpc_security_group_ids = [ "${module.kafka-security-groups.kafka_broker_security_group_ids}"]
  key_name = "${var.cluster["aws_ssh_key"]}"
  associate_public_ip_address = false
  iam_instance_profile = "${aws_iam_instance_profile.haystack-kafka-profile.name}"
  tags = {
    Product = "Haystack"
    Component = "Kafka"
    ClusterName = "${var.cluster["name"]}"
    Role = "${var.cluster["role_prefix"]}-kafka-brokers"
    Name = "${var.cluster["name"]}-kafka-brokers-${count.index}"
  }
  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.kafka["broker_volume_size"]}"
    delete_on_termination = false
  }
  lifecycle {
    ignore_changes = ["ami", "user_data","subnet_id"]
  }

  user_data = "${data.template_file.kafka_broker_user_data.*.rendered[count.index]}"
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

// create A records for zookeeper hosts
resource "aws_route53_record" "haystack-zookeeper-a-records" {
  count   = "${var.kafka["zookeeper_count"]}"
  zone_id = "${var.aws_hosted_zone_id}"
  name    = "${var.cluster["name"]}-zookeeper-${count.index}"
  type    = "A"
  ttl     = "300"
  records = ["${element(aws_instance.haystack-zookeeper-nodes.*.private_ip, count.index)}"]
}
