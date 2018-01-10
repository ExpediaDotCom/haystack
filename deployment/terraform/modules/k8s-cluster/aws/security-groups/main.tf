//api elb security group
resource "aws_security_group" "api-elb-haystack-k8s" {
  name = "api-elb.haystack-k8s"
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
    KubernetesCluster = "haystack-k8s"
    Name = "api-elb.haystack-k8s"
  }

}


//node elb security group
resource "aws_security_group" "nodes-api-elb-haystack-k8s" {
  name = "nodes-api-elb.haystack-k8s"
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
    KubernetesCluster = "haystack-k8s"
    Name = "nodes-api-elb.haystack-k8s"
  }
}


//node instance security group

//This is prevent the cyclic dependency
resource "aws_security_group_rule" "all-master-to-node" {
  type                     = "ingress"
  security_group_id        = "${aws_security_group.nodes-haystack-k8s.id}"
  source_security_group_id = "${aws_security_group.masters-haystack-k8s.id}"
  from_port                = 0
  to_port                  = 0
  protocol                 = "-1"
}

resource "aws_security_group" "nodes-haystack-k8s" {
  name = "nodes.haystack-k8s"
  vpc_id = "${var.k8s_vpc_id}"
  description = "Security group for nodes"

  ingress {
    from_port = "${var.reverse_proxy_port}"
    to_port = "${var.reverse_proxy_port}"
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.nodes-api-elb-haystack-k8s.id}"]
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
    KubernetesCluster = "haystack-k8s"
    Name = "nodes.haystack-k8s"
  }
}


//master instance security group
resource "aws_security_group_rule" "all-master-to-master" {
  type                     = "ingress"
  security_group_id        = "${aws_security_group.masters-haystack-k8s.id}"
  source_security_group_id = "${aws_security_group.masters-haystack-k8s.id}"
  from_port                = 0
  to_port                  = 0
  protocol                 = "-1"
}
resource "aws_security_group" "masters-haystack-k8s" {
  name = "masters.haystack-k8s"
  vpc_id = "${var.k8s_vpc_id}"
  description = "Security group for masters"

  ingress {
    from_port = "443"
    to_port = "443"
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.api-elb-haystack-k8s.id}"]
  }
  ingress {
    from_port = 0
    to_port = 65535
    protocol = "4"
    security_groups = [
      "${aws_security_group.nodes-haystack-k8s.id}"]
  }

  ingress {
    from_port = 1
    to_port = 2379
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.nodes-haystack-k8s.id}"]
  }
  ingress {
    from_port = 2382
    to_port = 4001
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.nodes-haystack-k8s.id}"]
  }

  ingress {
    from_port = 4003
    to_port = 65535
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.nodes-haystack-k8s.id}"]
  }
  ingress {
    from_port = 1
    to_port = 65535
    protocol = "udp"
    security_groups = [
      "${aws_security_group.nodes-haystack-k8s.id}"]
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
    KubernetesCluster = "haystack-k8s"
    Name = "masters.haystack-k8s"
  }
}

