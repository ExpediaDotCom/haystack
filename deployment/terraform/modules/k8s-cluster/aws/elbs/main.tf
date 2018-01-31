resource "aws_elb" "api-elb" {
  name = "${var.haystack_cluster_name}-api-elb"

  listener = {
    instance_port = 443
    instance_protocol = "TCP"
    lb_port = 443
    lb_protocol = "TCP"
  }

  security_groups = [
    "${var.elb_api_security_groups}"]
  subnets = [
    "${var.aws_elb_subnet}"]
  internal = false

  health_check = {
    target = "SSL:443"
    healthy_threshold = 2
    unhealthy_threshold = 2
    interval = 10
    timeout = 5
  }

  idle_timeout = 300

  tags = {
    Product = "Haystack"
    Component = "K8s"
    ClusterName = "${var.haystack_cluster_name}"
    Role = "${var.haystack_cluster_name}-k8s-masters"
    Name = "${var.haystack_cluster_name}-k8s-masters"
  }
}

resource "aws_elb" "monitoring-elb" {
  name = "${var.haystack_cluster_name}-monitoring-elb"

  listener = {
    instance_port = "${var.graphite_node_port}"
    instance_protocol = "TCP"
    lb_port = 2003
    lb_protocol = "TCP"
  }

  security_groups = [
    "${var.monitoring_security_groups}"]
  subnets = [
    "${var.aws_nodes_subnet}"]
  internal = true

  health_check = {
    target = "TCP:${var.graphite_node_port}"
    healthy_threshold = 2
    unhealthy_threshold = 2
    interval = 10
    timeout = 5
  }

  idle_timeout = 300

  tags = {
    Product = "Haystack"
    Component = "K8s"
    ClusterName = "${var.haystack_cluster_name}"
    Role = "${var.haystack_cluster_name}-k8s-monitoring-nodes"
    Name = "${var.haystack_cluster_name}-k8s-monitoring-nodes"
  }
}

resource "aws_elb" "nodes-elb" {
  name = "${var.haystack_cluster_name}-nodes-elb"

  listener = {
    instance_port = "${var.reverse_proxy_port}"
    instance_protocol = "HTTP"
    lb_port = 80
    lb_protocol = "HTTP"
  }

  security_groups = [
    "${var.nodes_api_security_groups}"]
  subnets = [
    "${var.aws_elb_subnet}"]
  internal = false

  health_check = {
    target = "TCP:${var.reverse_proxy_port}"
    healthy_threshold = 2
    unhealthy_threshold = 2
    interval = 10
    timeout = 5
  }

  idle_timeout = 300

  tags = {
    Product = "Haystack"
    Component = "K8s"
    ClusterName = "${var.haystack_cluster_name}"
    Role = "${var.haystack_cluster_name}-k8s-app-nodes"
    Name = "${var.haystack_cluster_name}-k8s-app-nodes"
  }
}


resource "aws_autoscaling_attachment" "master-1" {
  elb = "${aws_elb.api-elb.id}"
  autoscaling_group_name = "${var.master-1_asg_id}"
}

resource "aws_autoscaling_attachment" "master-2" {
  elb = "${aws_elb.api-elb.id}"
  autoscaling_group_name = "${var.master-2_asg_id}"
}

resource "aws_autoscaling_attachment" "master-3" {
  elb = "${aws_elb.api-elb.id}"
  autoscaling_group_name = "${var.master-3_asg_id}"
}


resource "aws_autoscaling_attachment" "nodes-api" {
  elb = "${aws_elb.nodes-elb.id}"
  autoscaling_group_name = "${var.app-nodes_asg_id}"
}

resource "aws_autoscaling_attachment" "nodes-monitoring" {
  elb = "${aws_elb.monitoring-elb.id}"
  autoscaling_group_name = "${var.monitoring-nodes_asg_id}"
}