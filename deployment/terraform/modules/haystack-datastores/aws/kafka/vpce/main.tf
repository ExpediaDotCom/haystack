data "aws_caller_identity" "current" {}

resource "aws_lb" "kafka_nlb" {
  count = "${var.kafka["vpce_enabled"]?1:0}"
  name = "nlb-${var.cluster["name"]}-kafka-brokers"

  subnets = "${var.subnets}"
  internal = "true"

  load_balancer_type = "network"

  idle_timeout = 400

  tags = "${merge(var.common_tags, map(
    "ClusterName", "${var.cluster["name"]}",
    "Role", "${var.cluster["role_prefix"]}-kafka-brokers",
    "Name", "nlb-${var.cluster["name"]}-kafka-brokers",
    "Component", "Kafka"
  ))}"
}

resource "aws_lb_target_group" "kafka_nlb_target_group" {
  count = "${var.kafka["vpce_enabled"]?1:0}"
  name = "nlb-tg-${var.cluster["name"]}-kafka-brokers"
  port = "${var.kafka["kafka_port"]}"
  vpc_id = "${var.cluster["aws_vpc_id"]}"
  deregistration_delay = 120
  protocol = "TCP"

  lifecycle {
    create_before_destroy = true
  }

  health_check {
    protocol = "TCP"
    healthy_threshold = 2
    unhealthy_threshold = 2
    interval = 10
    path = ""
    matcher = ""
  }

  tags = "${merge(var.common_tags, map(
    "ClusterName", "${var.cluster["name"]}",
    "Role", "${var.cluster["role_prefix"]}-kafka-brokers",
    "Name", "nlb-tg-${var.cluster["name"]}-kafka-brokers",
    "Component", "Kafka"
  ))}"
}

resource "aws_lb_target_group_attachment" "kafka_nlb_target_group_attachment" {
  count = "${var.kafka["vpce_enabled"]?length(var.kafka_instance_ids):0}"
  target_group_arn = "${aws_lb_target_group.kafka_nlb_target_group.arn}"
  target_id = "${element(var.kafka_instance_ids, count.index)}"
  port = "${var.kafka["kafka_port"]}"
}

resource "aws_lb_listener" "kafka_nlb_listener" {
  count = "${var.kafka["vpce_enabled"]?1:0}"
  load_balancer_arn = "${aws_lb.kafka_nlb.arn}"
  port = "${var.kafka["kafka_port"]}"
  protocol = "TCP"

  default_action {
    target_group_arn = "${aws_lb_target_group.kafka_nlb_target_group.arn}"
    type = "forward"
  }
}

resource "aws_vpc_endpoint_service" "vpce_provider" {
  count = "${var.kafka["vpce_enabled"]?1:0}"
  acceptance_required = false
  network_load_balancer_arns = ["${aws_lb.kafka_nlb.arn}"]

  lifecycle {
    ignore_changes = [
      "allowed_principals"
    ]
  }
}

resource "aws_vpc_endpoint_service_allowed_principal" "current_account_whitelisted" {
  count = "${var.kafka["vpce_enabled"]?1:0}"
  vpc_endpoint_service_id = "${aws_vpc_endpoint_service.vpce_provider.id}"
  principal_arn = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
}

resource "aws_vpc_endpoint_service_allowed_principal" "kafka_vpce_allowed_principals" {
  count = "${var.kafka["vpce_enabled"]?length(var.whitelisted_accounts):0}"
  vpc_endpoint_service_id = "${aws_vpc_endpoint_service.vpce_provider.id}"
  principal_arn = "arn:aws:iam::${element(var.whitelisted_accounts, count.index)}:root"
}