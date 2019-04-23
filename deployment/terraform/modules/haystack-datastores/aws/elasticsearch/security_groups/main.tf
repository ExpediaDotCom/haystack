resource "aws_security_group" "haystack-es" {
  name = "${var.cluster["name"]}-es"
  vpc_id = "${var.cluster["aws_vpc_id"]}"
  description = "Security group for haystack ES"

  tags = {
    Product = "Haystack"
    Component = "ES"
    ClusterName = "${var.cluster["name"]}"
    Role = "${var.cluster["role_prefix"]}-es"
    Name = "${var.cluster["name"]}-es"
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
    cidr_blocks = ["0.0.0.0/0"]
  }
}