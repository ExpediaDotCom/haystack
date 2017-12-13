
resource "aws_ebs_volume" "1-etcd-events" {
  availability_zone = "us-west-2c"
  size              = 20
  type              = "gp2"
  encrypted         = false

  tags = {
    KubernetesCluster    = "${var.k8s_cluster_name}"
    Name                 = "1.etcd-events.${var.k8s_cluster_name}"
    "k8s.io/etcd/events" = "1/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "1-etcd-main" {
  availability_zone = "us-west-2c"
  size              = 20
  type              = "gp2"
  encrypted         = false

  tags = {
    KubernetesCluster    = "${var.k8s_cluster_name}"
    Name                 = "1.etcd-main.${var.k8s_cluster_name}"
    "k8s.io/etcd/main"   = "1/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "2-etcd-events" {
  availability_zone = "us-west-2c"
  size              = 20
  type              = "gp2"
  encrypted         = false

  tags = {
    KubernetesCluster    = "${var.k8s_cluster_name}"
    Name                 = "2.etcd-events.${var.k8s_cluster_name}"
    "k8s.io/etcd/events" = "2/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "2-etcd-main" {
  availability_zone = "us-west-2c"
  size              = 20
  type              = "gp2"
  encrypted         = false

  tags = {
    KubernetesCluster    = "${var.k8s_cluster_name}"
    Name                 = "2.etcd-main.${var.k8s_cluster_name}"
    "k8s.io/etcd/main"   = "2/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "3-etcd-events" {
  availability_zone = "us-west-2c"
  size              = 20
  type              = "gp2"
  encrypted         = false

  tags = {
    KubernetesCluster    = "${var.k8s_cluster_name}"
    Name                 = "3.etcd-events.${var.k8s_cluster_name}"
    "k8s.io/etcd/events" = "3/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "3-etcd-main" {
  availability_zone = "us-west-2c"
  size              = 20
  type              = "gp2"
  encrypted         = false

  tags = {
    KubernetesCluster    = "${var.k8s_cluster_name}"
    Name                 = "3.etcd-main.${var.k8s_cluster_name}"
    "k8s.io/etcd/main"   = "3/1,2,3"
    "k8s.io/role/master" = "1"
  }
}
