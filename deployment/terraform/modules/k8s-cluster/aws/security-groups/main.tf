locals {
  node-elb_ingress = "${split(",", var.cluster["node-elb_ingress"])}"
}
//api elb security group
resource "aws_security_group" "api-elb" {
  name = "api-elb.${var.cluster["name"]}"
  vpc_id = "${var.cluster["aws_vpc_id"]}"
  description = "Security group for api ELB"

  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = "${var.cluster["cidr_blocks"]}"
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = "${var.cluster["cidr_blocks"]}"
  }
  tags = {
    Product = "Haystack"
    Component = "K8s"
    ClusterName = "${var.cluster["name"]}"
    Role = "${var.cluster["role_prefix"]}-k8s-masters-elb"
    Name = "${var.cluster["name"]}-k8s-masters-elb"
  }

}


//node elb security group
resource "aws_security_group" "nodes-elb" {
  name = "nodes-elb.${var.cluster["name"]}"
  vpc_id = "${var.cluster["aws_vpc_id"]}"
  description = "Security group for nodes ELB"
  ingress {
    from_port = "${var.nodes_elb_port}"
    to_port = "${var.nodes_elb_port}"
    protocol = "tcp"
    cidr_blocks = [ "${local.node-elb_ingress}" ]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = "${var.cluster["cidr_blocks"]}"
  }
  tags = {
    Product = "Haystack"
    Component = "K8s"
    ClusterName = "${var.cluster["name"]}"
    Role = "${var.cluster["role_prefix"]}-k8s-nodes-elb"
    Name = "${var.cluster["name"]}-k8s-nodes-elb"

  }
}


//node elb security group
resource "aws_security_group" "monitoring-elb" {
  name = "monitoring-elb.${var.cluster["name"]}"
  vpc_id = "${var.cluster["aws_vpc_id"]}"
  description = "Security group for nodes ELB"
  ingress {
    from_port = 2003
    to_port = 2003
    protocol = "tcp"
    cidr_blocks = "${var.cluster["cidr_blocks"]}"
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = "${var.cluster["cidr_blocks"]}"
  }
  tags = {
    Product = "Haystack"
    Component = "K8s"
    ClusterName = "${var.cluster["name"]}"
    Role = "${var.cluster["role_prefix"]}-k8s-monitoring-elb"
    Name = "${var.cluster["name"]}-k8s-monitoring-elb"

  }
}

//node instance security group

resource "aws_security_group" "nodes" {
  name = "nodes.${var.cluster["name"]}"
  vpc_id = "${var.cluster["aws_vpc_id"]}"
  description = "Security group for nodes"

  tags = {
    Product = "Haystack"
    Component = "K8s"
    ClusterName = "${var.cluster["name"]}"
    Role = "${var.cluster["role_prefix"]}-k8s-nodes"
    Name = "${var.cluster["name"]}-k8s-nodes"
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
  cidr_blocks = "${var.cluster["cidr_blocks"]}"
}


resource "aws_security_group_rule" "reverse_proxy-to-node" {
  type = "ingress"
  security_group_id = "${aws_security_group.nodes.id}"
  source_security_group_id = "${aws_security_group.nodes-elb.id}"
  from_port = "${var.cluster["reverse_proxy_port"]}"
  to_port = "${var.cluster["reverse_proxy_port"]}"
  protocol = "tcp"
}

resource "aws_security_group_rule" "graphite_elb-to-node" {
  type = "ingress"
  security_group_id = "${aws_security_group.nodes.id}"
  source_security_group_id = "${aws_security_group.monitoring-elb.id}"
  from_port = "${var.graphite_node_port}"
  to_port = "${var.graphite_node_port}"
  protocol = "tcp"
}

resource "aws_security_group_rule" "node-egress" {
  type = "egress"
  security_group_id = "${aws_security_group.nodes.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
  cidr_blocks = "${var.cluster["cidr_blocks"]}"
}



//master instance security group

resource "aws_security_group" "masters" {
  name = "masters.${var.cluster["name"]}"
  vpc_id = "${var.cluster["aws_vpc_id"]}"
  description = "Security group for masters"

  tags = {
    Product = "Haystack"
    Component = "K8s"
    ClusterName = "${var.cluster["name"]}"
    Role = "${var.cluster["role_prefix"]}-k8s-masters"
    Name = "${var.cluster["name"]}-k8s-masters"
  }
}

resource "aws_security_group_rule" "all-master-to-master" {
  type = "ingress"
  security_group_id = "${aws_security_group.masters.id}"
  source_security_group_id = "${aws_security_group.masters.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
}
resource "aws_security_group_rule" "all-node-to-master" {
  type = "ingress"
  security_group_id = "${aws_security_group.masters.id}"
  source_security_group_id = "${aws_security_group.nodes.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
}

resource "aws_security_group_rule" "ssh-external-to-master-0-0-0-0--0" {
  type = "ingress"
  security_group_id = "${aws_security_group.masters.id}"
  from_port = 22
  to_port = 22
  protocol = "tcp"
  cidr_blocks = "${var.cluster["cidr_blocks"]}"
}

resource "aws_security_group_rule" "https-elb-to-master" {
  type = "ingress"
  security_group_id = "${aws_security_group.masters.id}"
  source_security_group_id = "${aws_security_group.api-elb.id}"
  from_port = 443
  to_port = 443
  protocol = "tcp"
}

resource "aws_security_group_rule" "master-egress" {
  type = "egress"
  security_group_id = "${aws_security_group.masters.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
  cidr_blocks = "${var.cluster["cidr_blocks"]}"
}


