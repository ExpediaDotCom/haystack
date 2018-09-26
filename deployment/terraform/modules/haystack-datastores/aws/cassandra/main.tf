// using data-source to find latest cassandra image
data "aws_ami" "haystack-cassandra-base-ami" {
  filter {
    name   = "state"
    values = ["available"]
  }

  filter {
    name   = "tag:type"
    values = ["haystack-cassandra-base"]
  }

  most_recent = true
}

locals {
  cassandra_ami = "${var.node_image == "" ? data.aws_ami.haystack-cassandra-base-ami.image_id : var.node_image }"
  cassandra_config_yaml_path = "/etc/cassandra/default.conf/cassandra.yaml"
  cassandra_cname = "${var.haystack_cluster_name}-cassandra"
  cassandra_ssh_user = "ec2-user"
}

module "cassandra-security-groups" {
  source = "security_groups"
  aws_vpc_id= "${var.aws_vpc_id}"
  haystack_cluster_name = "${var.haystack_cluster_name}"
}

data "template_file" "cassandra_user_data" {
  template = "${file("${path.module}/data/node_user_data_sh.tpl")}"

  vars {
    seed_node_count = "${var.seed_node_count}"
    clusterRole = "${var.haystack_cluster_name}-cassandra-seed"
    haystack_graphite_host = "${var.graphite_host}"
    haystack_graphite_port = "${var.graphite_port}"
  }
}

resource "aws_iam_role" "haystack-cassandra-role" {
  name = "${var.haystack_cluster_name}-cassandra-nodes-role"
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

resource "aws_iam_role_policy" "cassandra-policy" {
  name = "cassandra-policy"
  role = "${aws_iam_role.haystack-cassandra-role.name}"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "cassandraRoute53ListZones",
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

resource "aws_iam_instance_profile" "haystack-cassandra-nodes-profile" {
  name = "${var.haystack_cluster_name}-cassandra-nodes"
  role = "${aws_iam_role.haystack-cassandra-role.name}"
}

resource "aws_instance" "haystack-cassandra-seed-nodes" {
  count = "${var.seed_node_count}"
  ami = "${local.cassandra_ami}"
  instance_type = "${var.seed_node_instance_type}"
  subnet_id = "${var.aws_subnet}"
  iam_instance_profile = "${aws_iam_instance_profile.haystack-cassandra-nodes-profile.name}"
  vpc_security_group_ids = [ "${module.cassandra-security-groups.nodes_security_group_ids}"]
  associate_public_ip_address = false
  key_name = "${var.aws_ssh_key_pair_name}"

  tags {
    Product = "Haystack"
    Component = "Cassandra"
    ClusterName = "${var.haystack_cluster_name}"
    Role = "${var.haystack_cluster_name}-cassandra"
    Name = "${var.haystack_cluster_name}-cassandra-${count.index}"
    ClusterRole = "${var.haystack_cluster_name}-cassandra-seed"
    isSeed = "true"
  }

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.seed_node_volume_size}"
    delete_on_termination = false
  }
  lifecycle {
    ignore_changes = ["ami", "user_data","subnet_id"]
  }
  
  user_data = "${data.template_file.cassandra_user_data.rendered}"
}

resource "aws_instance" "haystack-cassandra-non-seed-nodes" {
  count = "${var.non_seed_node_count}"
  ami = "${local.cassandra_ami}"
  instance_type = "${var.non_seed_node_instance_type}"
  subnet_id = "${var.aws_subnet}"
  iam_instance_profile = "${aws_iam_instance_profile.haystack-cassandra-nodes-profile.name}"
  vpc_security_group_ids = [ "${module.cassandra-security-groups.nodes_security_group_ids}"]
  associate_public_ip_address = false
  key_name = "${var.aws_ssh_key_pair_name}"



  tags {
    Product = "Haystack"
    Component = "Cassandra"
    ClusterName = "${var.haystack_cluster_name}"
    Role = "${var.haystack_cluster_name}-cassandra"
    Name = "${var.haystack_cluster_name}-cassandra-${var.seed_node_count + count.index}"
    ClusterRole = "${var.haystack_cluster_name}-cassandra-non-seed"
    isSeed = "false"
  }

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.non_seed_node_volume_size}"
    delete_on_termination = false
  }

  user_data = "${data.template_file.cassandra_user_data.rendered}"
}

// create cname for newly created cassandra cluster
resource "aws_route53_record" "haystack-cassandra-cname" {
  zone_id = "${var.aws_hosted_zone_id}"
  name    = "${local.cassandra_cname}"
  type    = "A"
  ttl     = "300"
  records = ["${concat(aws_instance.haystack-cassandra-seed-nodes.*.private_ip, aws_instance.haystack-cassandra-non-seed-nodes.*.private_ip)}"]
}
