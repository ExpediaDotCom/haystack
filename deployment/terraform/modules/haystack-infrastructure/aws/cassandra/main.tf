// using data-source to find latest cassandra image
data "aws_ami" "haystack-cassandra-base-ami" {
  filter {
    name   = "state"
    values = ["available"]
  }

  filter {
    name   = "tag:ami_base_name"
    values = ["haystack-cassandra-base"]
  }

  most_recent = true
}

locals {
  cassandra_ami = "${var.cassandra_node_image == "" ? data.aws_ami.haystack-cassandra-base-ami.image_id : var.cassandra_node_image }"
  cassandra_config_yaml_path = "/etc/cassandra/cassandra.yaml"
  cassandra_non_seed_node_count = "${var.cassandra_node_count - 1}"
  cassandra_cname = "haystack-cassandra"
}

data "template_file" "cassandra_seed_config" {
  template = "${file("${path.module}/data/cassandra_yaml.tpl")}"

  vars {
    seed_ip = "127.0.0.1"
  }
}

// create seed node
resource "aws_instance" "haystack-cassandra-seed-node" {
  ami = "${local.cassandra_ami}"
  instance_type = "${var.cassandra_node_instance_type}"
  subnet_id = "${var.cassandra_aws_subnet}"
  security_groups = ["${var.cassandra_security_group}"]
  key_name = "${var.cassandra_ssh_key_pair_name}"

  tags {
    Name = "haystack-cassandra-instance"
    NodeType = "seed"
  }

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.cassandra_node_volume_size}"
    delete_on_termination = false
  }

  provisioner "remote-exec" {
    inline = [
      "cat > ${local.cassandra_config_yaml_path} <<EOL\n${data.template_file.cassandra_seed_config.rendered}EOL",
      "sudo service cassandra start"
    ]
  }
}

data "template_file" "cassandra_non_seed_config" {
  template = "${file("${path.module}/data/cassandra_yaml.tpl")}"

  vars {
    seed_ip = "${aws_instance.haystack-cassandra-seed-node.private_ip}"
  }
}

// use seed node ip to create & initialize non-seed nodes
resource "aws_instance" "haystack-cassandra-non-seed-nodes" {
  count = "${local.cassandra_non_seed_node_count}"
  ami = "${local.cassandra_ami}"
  instance_type = "${var.cassandra_node_instance_type}"
  subnet_id = "${var.cassandra_aws_subnet}"
  key_name = "${var.cassandra_ssh_key_pair_name}"

  tags {
    Name = "haystack-cassandra-instance"
    NodeType = "non-seed"
    Seed = "${aws_instance.haystack-cassandra-seed-node.private_ip}"
  }

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.cassandra_node_volume_size}"
    delete_on_termination = false
  }

  provisioner "remote-exec" {
    inline = [
      "cat > ${local.cassandra_config_yaml_path} <<EOL\n${data.template_file.cassandra_non_seed_config.rendered}EOL",
      "sudo service cassandra start"
    ]
  }
}

// create cname for newly created cassandra cluster
resource "aws_route53_record" "haystack-cassandra-cname" {
  zone_id = "${var.cassandra_hosted_zone_id}"
  name    = "${local.cassandra_cname}"
  type    = "A"
  ttl     = "300"
  records = ["${aws_instance.haystack-cassandra-seed-node.private_ip}","${aws_instance.haystack-cassandra-non-seed-nodes.private_ip}"]
}
