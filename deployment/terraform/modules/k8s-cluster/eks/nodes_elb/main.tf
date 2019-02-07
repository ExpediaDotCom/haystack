resource "aws_elb" "nodes-elb" {
  name = "${var.cluster_name}-nodes-elb"

  listener = {
    instance_port = "${var.reverse_proxy_port}"
    instance_protocol = "http"
    lb_port = 443
    lb_protocol = "https"
    ssl_certificate_id = "${var.nodes_api_ssl_cert}"

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
    Role = "${var.role_name}"
    Name = "${var.cluster_name}-eks-worker-nodes-elb"
  }
}

resource "aws_autoscaling_attachment" "nodes-api" {
  elb = "${aws_elb.nodes-elb.id}"
  autoscaling_group_name = "${var.app-nodes_asg_id}"
}
