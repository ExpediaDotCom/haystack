resource "aws_security_group" "brokers-haystack-kafka" {
  name = "brokers.haystack-k8s"
  vpc_id = "${var.kafka_aws_vpc_id}"
  description = "Security group for haystack kafka brokers"

  tags = {
    Name = "haystack-kafka"
  }
}

resource "aws_security_group" "workers-haystack-kafka" {
  name = "workers.haystack-k8s"
  vpc_id = "${var.kafka_aws_vpc_id}"
  description = "Security group for haystack kafka workers"

  tags = {
    Name = "haystack-kafka"
  }
}


resource "aws_security_group_rule" "haytack-kafka-worker-control-center-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.workers-haystack-kafka.id}"
  from_port = 9021
  to_port = 9021
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}

resource "aws_security_group_rule" "haytack-kafka-worker-kafka-connect-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.workers-haystack-kafka.id}"
  from_port = 8083
  to_port = 8083
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}


resource "aws_security_group_rule" "haytack-kafka-worker-rest-proxy-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.workers-haystack-kafka.id}"
  from_port = 8082
  to_port = 8082
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}


resource "aws_security_group_rule" "haytack-kafka-worker-schema-registry-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.workers-haystack-kafka.id}"
  from_port = 8081
  to_port = 8081
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}


resource "aws_security_group_rule" "haytack-kafka-worker-ssh-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.workers-haystack-kafka.id}"
  from_port = 22
  to_port = 22
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}


resource "aws_security_group_rule" "haytack-kafka-broker-ssh-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.brokers-haystack-kafka.id}"
  from_port = 22
  to_port = 22
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}


resource "aws_security_group_rule" "haytack-kafka-broker-zookeeper-2888-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.brokers-haystack-kafka.id}"
  source_security_group_id = "${aws_security_group.brokers-haystack-kafka.id}"
  from_port = 2888
  to_port = 2888
  protocol = "tcp"
}

resource "aws_security_group_rule" "haytack-kafka-broker-zookeeper-3888-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.brokers-haystack-kafka.id}"
  source_security_group_id = "${aws_security_group.brokers-haystack-kafka.id}"
  from_port = 3888
  to_port = 3888
  protocol = "tcp"
}

resource "aws_security_group_rule" "haytack-kafka-broker-zookeeper-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.brokers-haystack-kafka.id}"
  from_port = 2181
  to_port = 2181
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}


resource "aws_security_group_rule" "haytack-kafka-broker-ingress" {
  type = "ingress"
  security_group_id = "${aws_security_group.brokers-haystack-kafka.id}"
  from_port = 9092
  to_port = 9093
  protocol = "tcp"
  cidr_blocks = [
    "0.0.0.0/0"]
}

resource "aws_security_group_rule" "kafka-broker-egress" {
  type = "egress"
  security_group_id = "${aws_security_group.brokers-haystack-kafka.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
  cidr_blocks = [
    "0.0.0.0/0"]
}

resource "aws_security_group_rule" "kafka-worker-egress" {
  type = "egress"
  security_group_id = "${aws_security_group.workers-haystack-kafka.id}"
  from_port = 0
  to_port = 0
  protocol = "-1"
  cidr_blocks = [
    "0.0.0.0/0"]
}
