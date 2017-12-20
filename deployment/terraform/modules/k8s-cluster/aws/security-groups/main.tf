
resource "aws_security_group" "api-elb-haystack-k8s" {
  name        = "api-elb.haystack-k8s"
  vpc_id      = "${var.k8s_vpc_id}"
  description = "Security group for api ELB"

  tags = {
    KubernetesCluster = "haystack-k8s"
    Name              = "api-elb.haystack-k8s"
  }
}


resource "aws_security_group" "nodes-api-elb-haystack-k8s" {
  name        = "nodes-api-elb.haystack-k8s"
  vpc_id      = "${var.k8s_vpc_id}"
  description = "Security group for api ELB"

  tags = {
    KubernetesCluster = "haystack-k8s"
    Name              = "nodes-api-elb.haystack-k8s"
  }
}

resource "aws_security_group" "masters-haystack-k8s" {
  name        = "masters.haystack-k8s"
  vpc_id      = "${var.k8s_vpc_id}"
  description = "Security group for masters"

  tags = {
    KubernetesCluster = "haystack-k8s"
    Name              = "masters.haystack-k8s"
  }
}

resource "aws_security_group" "nodes-haystack-k8s" {
  name        = "nodes.haystack-k8s"
  vpc_id      = "${var.k8s_vpc_id}"
  description = "Security group for nodes"

  tags = {
    KubernetesCluster = "haystack-k8s"
    Name              = "nodes.haystack-k8s"
  }
}

resource "aws_security_group_rule" "all-master-to-master" {
  type                     = "ingress"
  security_group_id        = "${aws_security_group.masters-haystack-k8s.id}"
  source_security_group_id = "${aws_security_group.masters-haystack-k8s.id}"
  from_port                = 0
  to_port                  = 0
  protocol                 = "-1"
}

resource "aws_security_group_rule" "all-master-to-node" {
  type                     = "ingress"
  security_group_id        = "${aws_security_group.nodes-haystack-k8s.id}"
  source_security_group_id = "${aws_security_group.masters-haystack-k8s.id}"
  from_port                = 0
  to_port                  = 0
  protocol                 = "-1"
}

resource "aws_security_group_rule" "all-node-to-node" {
  type                     = "ingress"
  security_group_id        = "${aws_security_group.nodes-haystack-k8s.id}"
  source_security_group_id = "${aws_security_group.nodes-haystack-k8s.id}"
  from_port                = 0
  to_port                  = 0
  protocol                 = "-1"
}

resource "aws_security_group_rule" "api-elb-egress" {
  type              = "egress"
  security_group_id = "${aws_security_group.api-elb-haystack-k8s.id}"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
}


resource "aws_security_group_rule" "nodes-api-elb-egress" {
  type              = "egress"
  security_group_id = "${aws_security_group.nodes-api-elb-haystack-k8s.id}"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
}



resource "aws_security_group_rule" "http-nodes-elb-0-0-0-0--0" {
  type              = "ingress"
  security_group_id = "${aws_security_group.nodes-api-elb-haystack-k8s.id}"
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
}

resource "aws_security_group_rule" "https-api-elb-0-0-0-0--0" {
  type              = "ingress"
  security_group_id = "${aws_security_group.api-elb-haystack-k8s.id}"
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
}

resource "aws_security_group_rule" "https-elb-to-master" {
  type                     = "ingress"
  security_group_id        = "${aws_security_group.masters-haystack-k8s.id}"
  source_security_group_id = "${aws_security_group.api-elb-haystack-k8s.id}"
  from_port                = 443
  to_port                  = 443
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "master-egress" {
  type              = "egress"
  security_group_id = "${aws_security_group.masters-haystack-k8s.id}"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
}

resource "aws_security_group_rule" "node-egress" {
  type              = "egress"
  security_group_id = "${aws_security_group.nodes-haystack-k8s.id}"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
}

resource "aws_security_group_rule" "node-to-master-protocol-ipip" {
  type                     = "ingress"
  security_group_id        = "${aws_security_group.masters-haystack-k8s.id}"
  source_security_group_id = "${aws_security_group.nodes-haystack-k8s.id}"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "4"
}

resource "aws_security_group_rule" "node-to-master-tcp-1-2379" {
  type                     = "ingress"
  security_group_id        = "${aws_security_group.masters-haystack-k8s.id}"
  source_security_group_id = "${aws_security_group.nodes-haystack-k8s.id}"
  from_port                = 1
  to_port                  = 2379
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "node-to-master-tcp-2382-4001" {
  type                     = "ingress"
  security_group_id        = "${aws_security_group.masters-haystack-k8s.id}"
  source_security_group_id = "${aws_security_group.nodes-haystack-k8s.id}"
  from_port                = 2382
  to_port                  = 4001
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "node-to-master-tcp-4003-65535" {
  type                     = "ingress"
  security_group_id        = "${aws_security_group.masters-haystack-k8s.id}"
  source_security_group_id = "${aws_security_group.nodes-haystack-k8s.id}"
  from_port                = 4003
  to_port                  = 65535
  protocol                 = "tcp"
}

resource "aws_security_group_rule" "node-to-master-udp-1-65535" {
  type                     = "ingress"
  security_group_id        = "${aws_security_group.masters-haystack-k8s.id}"
  source_security_group_id = "${aws_security_group.nodes-haystack-k8s.id}"
  from_port                = 1
  to_port                  = 65535
  protocol                 = "udp"
}

resource "aws_security_group_rule" "ssh-external-to-master-0-0-0-0--0" {
  type              = "ingress"
  security_group_id = "${aws_security_group.masters-haystack-k8s.id}"
  from_port         = 22
  to_port           = 22
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
}

resource "aws_security_group_rule" "ssh-external-to-node-0-0-0-0--0" {
  type              = "ingress"
  security_group_id = "${aws_security_group.nodes-haystack-k8s.id}"
  from_port         = 22
  to_port           = 22
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
}