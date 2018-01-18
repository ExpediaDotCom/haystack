resource "aws_security_group" "haystack-cassandra-nodes" {
  name = "${var.haystack_cluster_name}-cassandra-node-sg"
  vpc_id = "${var.aws_vpc_id}"
  description = "Security group for haystack cassandra nodes"

  tags = {
    Product = "Haystack"
    Component = "Cassandra"
    ClusterName = "${var.haystack_cluster_name}"
    Role = "${var.haystack_cluster_name}-cassandra"
    Name = "${var.haystack_cluster_name}-cassandra"
    NodeType = "seed"
  }
}

resource "aws_security_group_rule" "haytack-cassandra-node-ssh-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.haystack-cassandra-nodes.id}"
  from_port = 22
  to_port = 22
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}

resource "aws_security_group_rule" "haytack-cassandra-node-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.haystack-cassandra-nodes.id}"
  from_port = 9042
  to_port = 9042
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}

resource "aws_security_group_rule" "haytack-cassandra-node-gossip-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.haystack-cassandra-nodes.id}"
  from_port = 7000
  to_port = 7000
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}

resource "aws_security_group_rule" "haytack-cassandra-node-gossip-ssl-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.haystack-cassandra-nodes.id}"
  from_port = 7001
  to_port = 7001
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}

resource "aws_security_group_rule" "haytack-cassandra-node-rpc-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.haystack-cassandra-nodes.id}"
  from_port = 9160
  to_port = 9160
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}

resource "aws_security_group_rule" "haytack-cassandra-node-egress" {
  type = "egress"
  security_group_id = "${aws_security_group.haystack-cassandra-nodes.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
  cidr_blocks = [
    "0.0.0.0/0"]
}