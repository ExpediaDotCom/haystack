resource "aws_elb" "k8s-api-elb" {
  name = "haystack-k8s-api-elb"

  listener = {
    instance_port = 443
    instance_protocol = "TCP"
    lb_port = 443
    lb_protocol = "TCP"
  }

  security_groups = [
    "${var.k8s_elb_api_security_groups}"]
  subnets = [
    "${var.k8s_elb_subnet}"]
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
    KubernetesCluster = "${var.k8s_cluster_name}"
    Name = "api.${var.k8s_cluster_name}"
  }
}


resource "aws_elb" "k8s-nodes-elb" {
  name = "haystack-k8s-nodes-elb"

  listener = {
    instance_port = "${var.reverse_proxy_port}"
    instance_protocol = "HTTP"
    lb_port = 80
    lb_protocol = "HTTP"
  }

  security_groups = [
    "${var.k8s_nodes_api_security_groups}"]
  subnets = [
    "${var.k8s_elb_subnet}"]
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
    KubernetesCluster = "${var.k8s_cluster_name}"
    Name = "nodes.${var.k8s_cluster_name}"
  }
}


resource "aws_route53_record" "k8s-api-elb-route53" {
  name = "api.${var.k8s_cluster_name}"
  type = "CNAME"
  records = [
    "${aws_elb.k8s-api-elb.dns_name}"]
  ttl = 300
  zone_id = "/hostedzone/${var.k8s_hosted_zone_id}"
  //this would ensure that the cluster is up and configured correctly
  provisioner "local-exec" {
    command = "for i in {1..50}; do ${var.kubectl_executable_name} get nodes --context ${var.k8s_cluster_name} -- && break || sleep 15; done"
  }
}
resource "aws_route53_record" "k8s-nodes-elb-route53" {
  name = "${var.k8s_cluster_name}"
  type = "CNAME"
  records = [
    "${aws_elb.k8s-nodes-elb.dns_name}"]
  ttl = 300
  zone_id = "/hostedzone/${var.k8s_hosted_zone_id}"
  depends_on = [
    "aws_autoscaling_attachment.master-1-masters-haystack-k8s",
    "aws_autoscaling_attachment.master-2-masters-haystack-k8s",
    "aws_autoscaling_attachment.master-3-masters-haystack-k8s",
    "aws_route53_record.k8s-api-elb-route53",
    "aws_autoscaling_attachment.nodes-haystack-k8s"]
}


resource "aws_autoscaling_attachment" "master-1-masters-haystack-k8s" {
  elb = "${aws_elb.k8s-api-elb.id}"
  autoscaling_group_name = "${var.master-1_asg_id}"
}

resource "aws_autoscaling_attachment" "master-2-masters-haystack-k8s" {
  elb = "${aws_elb.k8s-api-elb.id}"
  autoscaling_group_name = "${var.master-2_asg_id}"
}

resource "aws_autoscaling_attachment" "master-3-masters-haystack-k8s" {
  elb = "${aws_elb.k8s-api-elb.id}"
  autoscaling_group_name = "${var.master-3_asg_id}"
}


resource "aws_autoscaling_attachment" "nodes-haystack-k8s" {
  elb = "${aws_elb.k8s-nodes-elb.id}"
  autoscaling_group_name = "${var.nodes_asg_id}"
}
