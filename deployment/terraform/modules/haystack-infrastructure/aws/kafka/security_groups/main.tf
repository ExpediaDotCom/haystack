resource "aws_security_group" "haystack-kafka" {
  name = "haystack-kafka-brokers-sg"
  vpc_id = "${var.kafka_aws_vpc_id}"
  description = "Security group for haystack kafka brokers"

  tags = {
    Name = "haystack-kafka-brokers"
  }
}

resource "aws_security_group_rule" "haytack-kafka-broker-ssh-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.haystack-kafka.id}"
  from_port = 22
  to_port = 22
  protocol = "tcp"
  cidr_blocks = ["0.0.0.0/0"]
}

resource "aws_security_group_rule" "haytack-kafka-broker-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.haystack-kafka.id}"
  from_port = 9092
  to_port = 9092
  protocol = "tcp"
  cidr_blocks = ["0.0.0.0/0"]
}

resource "aws_security_group_rule" "haytack-kafka-broker-egress" {
  type = "egress"
  security_group_id = "${aws_security_group.haystack-kafka.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
  cidr_blocks = ["0.0.0.0/0"]
}
