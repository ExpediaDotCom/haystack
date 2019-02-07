data "template_file" "app-nodes-user-data" {
  template = "${file("${path.module}/templates/k8s_nodes_user-data.tpl")}"
  vars {
    certificate-authority-data = "${var.certificate-authority-data}"
    aws_eks_cluster_endpoint = "${var.aws_eks_cluster_endpoint}"
    cluster_name = "${var.cluster_name}"
    aws_region = "${var.aws_region}"
  }
}

resource "aws_launch_configuration" "app-nodes" {
  associate_public_ip_address = false
  iam_instance_profile = "${var.nodes_iam-instance-profile_arn}"
  image_id = "${var.app-node_ami}"
  instance_type = "${var.app-node_instance_type}"
  key_name = "${var.aws_ssh_key}"
  name_prefix = "app-nodes"
  security_groups = [
    "${var.nodes_security_group_ids}"]
  user_data_base64 = "${base64encode(data.template_file.app-nodes-user-data.rendered)}"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_autoscaling_group" "app-nodes" {
  desired_capacity = "${var.app-node_min_instance_count}"
  launch_configuration = "${aws_launch_configuration.app-nodes.id}"
  max_size = "${var.app-node_max_instance_count}"
  min_size = "${var.app-node_min_instance_count}"
  name = "${var.cluster_name}-eks-nodes"
  vpc_zone_identifier = [
    "${var.aws_subnet_ids}"]

  tag {
    key = "Name"
    value = "${var.cluster_name}-eks-nodes"
    propagate_at_launch = true
  }

  tag {
    key = "kubernetes.io/cluster/${var.cluster_name}"
    value = "owned"
    propagate_at_launch = true
  }

  tag {
    key = "Role"
    value = "${var.cluster_name}-k8s-app-nodes"
    propagate_at_launch = true
  }

  tag {
      key = "kubernetes.io/role"
      value = "app-node"
      propagate_at_launch = true
    }

  tag {
      key = "k8s.io/cluster-autoscaler/enabled"
      value = "true"
      propagate_at_launch = true
    }

  tag {
      key = "k8s.io/cluster-autoscaler/${var.cluster_name}"
      value = "true"
      propagate_at_launch = true
    }

}

data "template_file" "monitoring-nodes-user-data" {
  template = "${file("${path.module}/templates/k8s_monitoring_nodes_user-data.tpl")}"
  vars {
    certificate-authority-data = "${var.certificate-authority-data}"
    aws_eks_cluster_endpoint = "${var.aws_eks_cluster_endpoint}"
    cluster_name = "${var.cluster_name}"
    aws_region = "${var.aws_region}"
  }
}

resource "aws_launch_configuration" "monitoring-nodes" {
  associate_public_ip_address = false
  iam_instance_profile = "${var.nodes_iam-instance-profile_arn}"
  image_id = "${var.app-node_ami}"
  instance_type = "${var.monitoring-node_instance_type}"
  key_name = "${var.aws_ssh_key}"
  name_prefix = "monitoring-nodes"
  security_groups = [
    "${var.nodes_security_group_ids}"]
  user_data_base64 = "${base64encode(data.template_file.monitoring-nodes-user-data.rendered)}"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_autoscaling_group" "monitoring-nodes" {
  desired_capacity = "${var.monitoring-node_instance_count}"
  launch_configuration = "${aws_launch_configuration.monitoring-nodes.id}"
  max_size = "${var.monitoring-node_instance_count}"
  min_size = "${var.monitoring-node_instance_count}"
  name = "${var.cluster_name}-eks-monitoring-nodes"
  vpc_zone_identifier = [
    "${var.aws_subnet_ids}"]

  tag {
    key = "Name"
    value = "${var.cluster_name}-eks-monitoring-nodes"
    propagate_at_launch = true
  }

  tag {
    key = "kubernetes.io/cluster/${var.cluster_name}"
    value = "owned"
    propagate_at_launch = true
  }

  tag {
    key = "Role"
    value = "${var.cluster_name}-k8s-app-nodes"
    propagate_at_launch = true
  }
    tag {
          key = "kubernetes.io/role"
          value = "monitoring-node"
          propagate_at_launch = true
        }
}

