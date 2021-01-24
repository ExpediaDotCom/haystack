//node instance security group

resource "aws_security_group" "nodes" {
  name = "nodes.${var.cluster_name}"
  vpc_id = "${var.aws_vpc_id}"
  description = "Security group for nodes"

  tags = {
    Role = "${var.role_name}"
    Name = "${var.cluster_name}-eks-nodes"
  }
}


resource "aws_security_group_rule" "all-master-to-node" {
  type = "ingress"
  security_group_id = "${aws_security_group.nodes.id}"
  source_security_group_id = "${aws_security_group.masters.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
}
resource "aws_security_group_rule" "all-node-to-node" {
  type = "ingress"
  security_group_id = "${aws_security_group.nodes.id}"
  source_security_group_id = "${aws_security_group.nodes.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
}


resource "aws_security_group_rule" "ssh-external-to-node-0-0-0-0--0" {
  type = "ingress"
  security_group_id = "${aws_security_group.nodes.id}"
  from_port = 22
  to_port = 22
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}

resource "aws_security_group_rule" "reverse_proxy-to-node" {
  type = "ingress"
  security_group_id = "${aws_security_group.nodes.id}"
  source_security_group_id = "${aws_security_group.nodes-elb.id}"
  from_port = "${var.reverse_proxy_port}"
  to_port = "${var.reverse_proxy_port}"
  protocol = "tcp"
}

resource "aws_security_group_rule" "node-egress" {
  type = "egress"
  security_group_id = "${aws_security_group.nodes.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
  cidr_blocks = [
    "0.0.0.0/0"]
}


//master instance security group

resource "aws_security_group" "masters" {
  name = "masters.${var.cluster_name}"
  vpc_id = "${var.aws_vpc_id}"
  description = "Security group for masters"

  tags = {
    Role = "${var.role_name}"
    Name = "${var.cluster_name}-eks-masters"
  }
}

resource "aws_security_group_rule" "master-ingress-https" {
  description              = "Allow clients to communicate with the cluster API Server"
  type                     = "ingress"
  from_port                = 443
  protocol                 = "tcp"
  security_group_id        = "${aws_security_group.masters.id}"
  cidr_blocks = [
    "0.0.0.0/0"]
  to_port                  = 443
}
resource "aws_security_group_rule" "master-egress" {
  type = "egress"
  security_group_id = "${aws_security_group.masters.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
  cidr_blocks = [
    "0.0.0.0/0"]
}


//node elb security group
resource "aws_security_group" "nodes-elb" {
  name = "nodes-elb.${var.cluster_name}"
  vpc_id = "${var.aws_vpc_id}"
  description = "Security group for nodes ELB"
  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"
    security_groups = [
      "${aws_security_group.nodes.id}"
    ]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  tags = {
    Role = "${var.role_name}"
    Name = "${var.cluster_name}-eks-nodes-elb"
  }
}

