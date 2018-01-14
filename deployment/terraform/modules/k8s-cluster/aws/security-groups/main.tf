//api elb security group
resource "aws_security_group" "api-elb" {
  name = "api-elb.${var.k8s_cluster_name}"
  vpc_id = "${var.k8s_vpc_id}"
  description = "Security group for api ELB"

  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  tags = {
    KubernetesCluster = "${var.k8s_cluster_name}"
  }

}


//node elb security group
resource "aws_security_group" "nodes-elb" {
  name = "nodes-elb.${var.k8s_cluster_name}"
  vpc_id = "${var.k8s_vpc_id}"
  description = "Security group for nodes ELB"
  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  tags = {
    KubernetesCluster = "${var.k8s_cluster_name}"
  }
}


//node instance security group

//This is prevent the cyclic dependency
resource "aws_security_group_rule" "all-master-to-node" {
  type = "ingress"
  security_group_id = "${aws_security_group.nodes.id}"
  source_security_group_id = "${aws_security_group.masters.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
}
resource "aws_security_group_rule" "all-master-to-node" {
  type = "ingress"
  security_group_id = "${aws_security_group.nodes.id}"
  source_security_group_id = "${aws_security_group.masters.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
}

resource "aws_security_group" "nodes" {
  name = "nodes.${var.k8s_cluster_name}"
  vpc_id = "${var.k8s_vpc_id}"
  description = "Security group for nodes"

  ingress {
    from_port = "${var.reverse_proxy_port}"
    to_port = "${var.reverse_proxy_port}"
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.nodes-elb.id}"]
  }

  ingress {
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  tags = {
    KubernetesCluster = "${var.k8s_cluster_name}"
  }
}


//master instance security group
resource "aws_security_group_rule" "all-master-to-master" {
  type = "ingress"
  security_group_id = "${aws_security_group.masters.id}"
  source_security_group_id = "${aws_security_group.masters.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
}
resource "aws_security_group" "masters" {
  name = "masters.${var.k8s_cluster_name}"
  vpc_id = "${var.k8s_vpc_id}"
  description = "Security group for masters"

  ingress {
    from_port = "443"
    to_port = "443"
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.api-elb.id}"]
  }
  ingress {
    from_port = 0
    to_port = 65535
    protocol = "4"
    security_groups = [
      "${aws_security_group.nodes.id}"]
  }

  ingress {
    from_port = 1
    to_port = 2379
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.nodes.id}"]
  }
  ingress {
    from_port = 2382
    to_port = 4001
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.nodes.id}"]
  }

  ingress {
    from_port = 4003
    to_port = 65535
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.nodes.id}"]
  }
  ingress {
    from_port = 1
    to_port = 65535
    protocol = "udp"
    security_groups = [
      "${aws_security_group.nodes.id}"]
  }
  ingress {
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  tags = {
    KubernetesCluster = "${var.k8s_cluster_name}"
    Name = "Haystack"
  }
}

