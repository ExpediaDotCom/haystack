//we can't currently update the cluster name, TODO: make it configurable
locals {
  k8s_master_1_instance_group_name = "master-${var.aws_zone}-1"
  k8s_master_2_instance_group_name = "master-${var.aws_zone}-2"
  k8s_master_3_instance_group_name = "master-${var.aws_zone}-3"
  k8s_app-nodes_instance_group_name = "app-nodes"
  k8s_monitoring-nodes_instance_group_name = "monitoring-nodes"
}
data "null_data_source" "tags" {
  count = "${length(keys(var.common_tags))}"
  inputs = {
    key                 = "${element(keys(var.common_tags), count.index)}"
    value               = "${element(values(var.common_tags), count.index)}"
    propagate_at_launch = true
  }
}


resource "aws_autoscaling_group" "master-1" {
  name = "${var.haystack_cluster_name}-master-1"
  launch_configuration = "${aws_launch_configuration.master-1.id}"
  max_size = 1
  min_size = 1
  vpc_zone_identifier = [
    "${var.aws_nodes_subnet}"]


  tags = ["${data.null_data_source.tags.*.outputs}"]
    tags = [
      {
      key = "Component"
      value = "K8s"
      propagate_at_launch = true
       },
      {
      key = "Role"
      value = "${var.haystack_cluster_role}-k8s-masters"
      propagate_at_launch = true
      },
      {
      key = "Name"
      value = "${var.haystack_cluster_name}-k8s-masters-1"
      propagate_at_launch = true
      },
      //these tags are required by protokube(kops) to set up kubecfg on that host, change with caution
      {
      key = "KubernetesCluster"
      value = "${var.k8s_cluster_name}"
      propagate_at_launch = true  
      },
      {
      key = "k8s.io/cluster-autoscaler/node-template/label/kops.k8s.io/instancegroup"
      value = "${local.k8s_master_1_instance_group_name}"
      propagate_at_launch = true 
      }
    ]


  depends_on = [
    "aws_ebs_volume.1-etcd-events",
    "aws_ebs_volume.1-etcd-main",
    "aws_ebs_volume.2-etcd-events",
    "aws_ebs_volume.2-etcd-main",
    "aws_ebs_volume.3-etcd-events",
    "aws_ebs_volume.3-etcd-main"]
}

resource "aws_autoscaling_group" "master-2" {
  name = "${var.haystack_cluster_name}-master-2"
  launch_configuration = "${aws_launch_configuration.master-2.id}"
  max_size = 1
  min_size = 1
  vpc_zone_identifier = [
    "${var.aws_nodes_subnet}"]
  tags = ["${data.null_data_source.tags.*.outputs}"]
  tags = [
    {
      key = "Component"
      value = "K8s"
      propagate_at_launch = true
    },
    {
      key = "Role"
      value = "${var.haystack_cluster_role}-k8s-masters"
      propagate_at_launch = true
    },
    {
      key = "Name"
      value = "${var.haystack_cluster_name}-k8s-masters-2"
      propagate_at_launch = true
    },
    //these tags are required by protokube(kops) to set up kubecfg on that host, change with caution
    {
      key = "k8s.io/cluster-autoscaler/node-template/label/kops.k8s.io/instancegroup"
      value = "${local.k8s_master_2_instance_group_name}"
      propagate_at_launch = true
    },
    {
      key = "KubernetesCluster"
      value = "${var.k8s_cluster_name}"
      propagate_at_launch = true
    }
  ]




  depends_on = [
    "aws_ebs_volume.1-etcd-events",
    "aws_ebs_volume.1-etcd-main",
    "aws_ebs_volume.2-etcd-events",
    "aws_ebs_volume.2-etcd-main",
    "aws_ebs_volume.3-etcd-events",
    "aws_ebs_volume.3-etcd-main"]
}

resource "aws_autoscaling_group" "master-3" {
  name = "${var.haystack_cluster_name}-master-3"
  launch_configuration = "${aws_launch_configuration.master-3.id}"
  max_size = 1
  min_size = 1
  vpc_zone_identifier = [
    "${var.aws_nodes_subnet}"]
  tags = ["${data.null_data_source.tags.*.outputs}"]
  tags = [
    {
      key = "Component"
      value = "K8s"
      propagate_at_launch = true
    },
    {
      key = "Role"
      value = "${var.haystack_cluster_role}-k8s-masters"
      propagate_at_launch = true
    },
    {
      key = "Name"
      value = "${var.haystack_cluster_name}-k8s-masters-3"
      propagate_at_launch = true
    },
    //these tags are required by protokube(kops) to set up kubecfg on that host, change with caution
    {
      key = "k8s.io/cluster-autoscaler/node-template/label/kops.k8s.io/instancegroup"
      value = "${local.k8s_master_3_instance_group_name}"
      propagate_at_launch = true
    },
    {
      key = "KubernetesCluster"
      value = "${var.k8s_cluster_name}"
      propagate_at_launch = true
    }
  ]


  depends_on = [
    "aws_ebs_volume.1-etcd-events",
    "aws_ebs_volume.1-etcd-main",
    "aws_ebs_volume.2-etcd-events",
    "aws_ebs_volume.2-etcd-main",
    "aws_ebs_volume.3-etcd-events",
    "aws_ebs_volume.3-etcd-main"]
}

resource "aws_autoscaling_group" "app-nodes" {
  name = "${var.haystack_cluster_name}-app-nodes"
  launch_configuration = "${aws_launch_configuration.app-nodes.id}"
  max_size = "${var.app-nodes_instance_count}"
  min_size = "${var.app-nodes_instance_count}"
  vpc_zone_identifier = [
    "${var.aws_nodes_subnet}"]
  tags = ["${data.null_data_source.tags.*.outputs}"]
  tags = [
    {
      key = "Component"
      value = "K8s"
      propagate_at_launch = true
    },
    {
      key = "Role"
      value = "${var.haystack_cluster_role}-k8s-app-nodes"
      propagate_at_launch = true
    },
    {
      key = "Name"
      value = "${var.haystack_cluster_name}-k8s-app-nodes"
      propagate_at_launch = true
    },
    //these tags are required by protokube(kops) to set up kubecfg on that host, change with caution
    {
      key = "k8s.io/cluster-autoscaler/node-template/label/kops.k8s.io/instancegroup"
      value = "${local.k8s_app-nodes_instance_group_name}"
      propagate_at_launch = true
    },
    {
      key = "KubernetesCluster"
      value = "${var.k8s_cluster_name}"
      propagate_at_launch = true
    }

  ]
}

resource "aws_autoscaling_group" "monitoring-nodes" {
  name = "${var.haystack_cluster_name}-monitoring-nodes"
  launch_configuration = "${aws_launch_configuration.monitoring-nodes.id}"
  max_size = "${var.monitoring-nodes_instance_count}"
  min_size = "${var.monitoring-nodes_instance_count}"
  vpc_zone_identifier = [
    "${var.aws_nodes_subnet}"]
  tags = ["${data.null_data_source.tags.*.outputs}"]
  tags = [
    {
      key = "Component"
      value = "K8s"
      propagate_at_launch = true
    },
    {
      key = "Role"
      value = "${var.haystack_cluster_role}-k8s-monitoring-nodes"
      propagate_at_launch = true
    },
    {
      key = "Name"
      value = "${var.haystack_cluster_name}-k8s-monitoring-nodes"
      propagate_at_launch = true
    },
    //these tags are required by protokube(kops) to set up kubecfg on that host, change with caution
    {
      key = "k8s.io/cluster-autoscaler/node-template/label/kops.k8s.io/instancegroup"
      value = "${local.k8s_monitoring-nodes_instance_group_name}"
      propagate_at_launch = true
    },
    {
      key = "KubernetesCluster"
      value = "${var.k8s_cluster_name}"
      propagate_at_launch = true
    }

  ]
}

data "template_file" "master-1-user-data" {
  template = "${file("${path.module}/templates/k8s_master_user-data.tpl")}"
  vars {
    cluster_name = "${var.k8s_cluster_name}"
    s3_bucket_name = "${var.s3_bucket_name}"
    instance_group_name = "${local.k8s_master_1_instance_group_name}"
  }
}
resource "aws_launch_configuration" "master-1" {
  name_prefix = "${var.haystack_cluster_name}-master-1"
  image_id = "${var.masters_ami}"
  instance_type = "${var.masters_instance_type}"
  key_name = "${var.aws_ssh_key}"
  iam_instance_profile = "${var.masters_iam-instance-profile_arn}"
  security_groups = [
    "${var.masters_security_groups}"]
  associate_public_ip_address = false
  user_data = "${data.template_file.master-1-user-data.rendered}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.master_instance_volume}"
    delete_on_termination = true
  }

  lifecycle = {
    create_before_destroy = true
  }
}

data "template_file" "master-2-user-data" {
  template = "${file("${path.module}/templates/k8s_master_user-data.tpl")}"
  vars {
    cluster_name = "${var.k8s_cluster_name}"
    s3_bucket_name = "${var.s3_bucket_name}"
    instance_group_name = "${local.k8s_master_2_instance_group_name}"
  }
}

resource "aws_launch_configuration" "master-2" {
  name_prefix = "${var.haystack_cluster_name}-master-2"
  image_id = "${var.masters_ami}"
  instance_type = "${var.masters_instance_type}"
  key_name = "${var.aws_ssh_key}"
  iam_instance_profile = "${var.masters_iam-instance-profile_arn}"
  security_groups = [
    "${var.masters_security_groups}"]
  associate_public_ip_address = false
  user_data = "${data.template_file.master-2-user-data.rendered}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.master_instance_volume}"
    delete_on_termination = true
  }


  lifecycle = {
    create_before_destroy = true
  }
}

data "template_file" "master-3-user-data" {
  template = "${file("${path.module}/templates/k8s_master_user-data.tpl")}"
  vars {
    cluster_name = "${var.k8s_cluster_name}"
    s3_bucket_name = "${var.s3_bucket_name}"
    instance_group_name = "${local.k8s_master_3_instance_group_name}"
  }
}

resource "aws_launch_configuration" "master-3" {
  name_prefix = "${var.haystack_cluster_name}-master-3"
  image_id = "${var.masters_ami}"
  instance_type = "${var.masters_instance_type}"
  key_name = "${var.aws_ssh_key}"
  iam_instance_profile = "${var.masters_iam-instance-profile_arn}"
  security_groups = [
    "${var.masters_security_groups}"]
  associate_public_ip_address = false
  user_data = "${data.template_file.master-3-user-data.rendered}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.master_instance_volume}"
    delete_on_termination = true
  }
  lifecycle = {
    create_before_destroy = true
  }
}


data "template_file" "app-nodes-user-data" {
  template = "${file("${path.module}/templates/k8s_nodes_user-data.tpl")}"
  vars {
    cluster_name = "${var.k8s_cluster_name}"
    s3_bucket_name = "${var.s3_bucket_name}"
    instance_group_name = "${local.k8s_app-nodes_instance_group_name}"
    nodes_instance_group = "${local.k8s_app-nodes_instance_group_name}"
  }
}

resource "aws_launch_configuration" "app-nodes" {
  name_prefix = "app-nodes"
  image_id = "${var.nodes_ami}"
  instance_type = "${var.app-nodes_instance_type}"
  key_name = "${var.aws_ssh_key}"
  iam_instance_profile = "${var.nodes_iam-instance-profile_arn}"
  security_groups = [
    "${var.nodes_security_groups}"]
  associate_public_ip_address = false
  user_data = "${data.template_file.app-nodes-user-data.rendered}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.app-nodes_instance_volume}"
    delete_on_termination = true
  }

  lifecycle = {
    create_before_destroy = true
  }
}

data "template_file" "monitoring-nodes-user-data" {
  template = "${file("${path.module}/templates/k8s_nodes_user-data.tpl")}"
  vars {
    cluster_name = "${var.k8s_cluster_name}"
    s3_bucket_name = "${var.s3_bucket_name}"
    instance_group_name = "${local.k8s_app-nodes_instance_group_name}"
    nodes_instance_group = "${local.k8s_monitoring-nodes_instance_group_name}"
  }
}
resource "aws_launch_configuration" "monitoring-nodes" {
  name_prefix = "monitoring-nodes"
  image_id = "${var.nodes_ami}"
  instance_type = "${var.monitoring-nodes_instance_type}"
  key_name = "${var.aws_ssh_key}"
  iam_instance_profile = "${var.nodes_iam-instance-profile_arn}"
  security_groups = [
    "${var.nodes_security_groups}"]
  associate_public_ip_address = false
  user_data = "${data.template_file.monitoring-nodes-user-data.rendered}"

  root_block_device = {
    volume_type = "gp2"
    volume_size = "${var.monitoring-nodes_instance_volume}"
    delete_on_termination = true
  }

  lifecycle = {
    create_before_destroy = true
  }
}


resource "aws_ebs_volume" "1-etcd-events" {
  availability_zone = "${var.aws_zone}"
  size = 20
  type = "gp2"
  encrypted = false


 tags = "${merge(var.common_tags, map(
    "Role", "${var.haystack_cluster_role}-k8s-masters",
    "Name", "${var.haystack_cluster_name}-k8s-events-1",
    "Component", "${var.haystack_cluster_name}",
    "KubernetesCluster", "${var.k8s_cluster_name}",
    "k8s.io/etcd/events", "1/1,2,3",
    "k8s.io/role/master", "1"
  ))}"
}
resource "aws_ebs_volume" "1-etcd-main" {
  availability_zone = "${var.aws_zone}"
  size = 20
  type = "gp2"
  encrypted = false


 tags = "${merge(var.common_tags, map(
    "Role", "${var.haystack_cluster_role}-k8s-masters",
    "Name", "${var.haystack_cluster_name}-k8s-main-1",
    "Component", "K8s",
    "KubernetesCluster", "${var.k8s_cluster_name}",
    "k8s.io/etcd/main", "1/1,2,3",
    "k8s.io/role/master", "1"
  ))}"
}
resource "aws_ebs_volume" "2-etcd-events" {
  availability_zone = "${var.aws_zone}"
  size = 20
  type = "gp2"
  encrypted = false


 tags = "${merge(var.common_tags, map(
    "Role", "${var.haystack_cluster_role}-k8s-masters",
    "Name", "${var.haystack_cluster_name}-k8s-events-2",
    "Component", "K8s",
    "KubernetesCluster", "${var.k8s_cluster_name}",
    "k8s.io/etcd/events", "2/1,2,3",
    "k8s.io/role/master", "1"
  ))}"
}

resource "aws_ebs_volume" "2-etcd-main" {
  availability_zone = "${var.aws_zone}"
  size = 20
  type = "gp2"
  encrypted = false


tags = "${merge(var.common_tags, map(
    "Role", "${var.haystack_cluster_name}-k8s-masters",
    "Name", "${var.haystack_cluster_name}-k8s-main-2",
    "Component", "K8s",
    "KubernetesCluster", "${var.k8s_cluster_name}",
    "k8s.io/etcd/main", "2/1,2,3",
    "k8s.io/role/master", "1"
  ))}"
}

resource "aws_ebs_volume" "3-etcd-events" {
  availability_zone = "${var.aws_zone}"
  size = 20
  type = "gp2"
  encrypted = false


tags = "${merge(var.common_tags, map(
    "Role", "${var.haystack_cluster_role}-k8s-masters",
    "Name", "${var.haystack_cluster_name}-k8s-events-3",
    "Component", "K8s",
    "KubernetesCluster", "${var.k8s_cluster_name}",
    "k8s.io/etcd/events", "3/1,2,3",
    "k8s.io/role/master", "1"
  ))}"
}
resource "aws_ebs_volume" "3-etcd-main" {
  availability_zone = "${var.aws_zone}"
  size = 20
  type = "gp2"
  encrypted = false


tags = "${merge(var.common_tags, map(
    "Role", "${var.haystack_cluster_role}-k8s-masters",
    "Name", "${var.haystack_cluster_name}-k8s-main-3",
    "Component", "K8s",
    "KubernetesCluster", "${var.k8s_cluster_name}",
    "k8s.io/etcd/main", "3/1,2,3",
    "k8s.io/role/master", "1"
  ))}"
}