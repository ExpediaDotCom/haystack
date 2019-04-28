locals {
    allowed_principals = "${compact(split(",", var.cluster["vpce-allowed_principals"]))}"
}

resource "aws_vpc_endpoint_service" "nodes-vpce" {
  count = "${var.cluster["vpce-svc_enabled"]}"
  acceptance_required        = "${var.cluster["vpce-acceptance_required"]}"
  network_load_balancer_arns = "${var.nodes-nlb-arn}"
}

resource "aws_vpc_endpoint_service_allowed_principal" "nodes-vpce" {
  vpc_endpoint_service_id = "${aws_vpc_endpoint_service.nodes-vpce.id}"
  count                   = "${var.cluster["vpce-svc_enabled"]?length(local.allowed_principals):0}"
  principal_arn           = "${element(local.allowed_principals, count.index)}"
}