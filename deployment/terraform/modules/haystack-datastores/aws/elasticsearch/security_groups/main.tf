resource "aws_security_group" "haystack-es" {
  name = "${var.haystack_cluster_name}-es"
  vpc_id = "${var.aws_vpc_id}"
  description = "Security group for haystack ES"

  tags = {
    Product = "Haystack"
    Component = "ES"
    ClusterName = "${var.haystack_cluster_name}"
    Role = "${var.haystack_cluster_name}-es"
    Name = "${var.haystack_cluster_name}-es"
  }
  ingress {
    from_port = 80
    protocol = "tcp"
    to_port = 80
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
}