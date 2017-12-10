/*
resource "aws_ebs_volume" "1-etcd-events-haystack-k8s-monitoring-test-expedia-com" {
  availability_zone = "us-west-2c"
  size              = 20
  type              = "gp2"
  encrypted         = false

  tags = {
    KubernetesCluster    = "haystack-k8s-monitoring.test.expedia.com"
    Name                 = "1.etcd-events.haystack-k8s-monitoring.test.expedia.com"
    "k8s.io/etcd/events" = "1/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "1-etcd-main-haystack-k8s-monitoring-test-expedia-com" {
  availability_zone = "us-west-2c"
  size              = 20
  type              = "gp2"
  encrypted         = false

  tags = {
    KubernetesCluster    = "haystack-k8s-monitoring.test.expedia.com"
    Name                 = "1.etcd-main.haystack-k8s-monitoring.test.expedia.com"
    "k8s.io/etcd/main"   = "1/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "2-etcd-events-haystack-k8s-monitoring-test-expedia-com" {
  availability_zone = "us-west-2c"
  size              = 20
  type              = "gp2"
  encrypted         = false

  tags = {
    KubernetesCluster    = "haystack-k8s-monitoring.test.expedia.com"
    Name                 = "2.etcd-events.haystack-k8s-monitoring.test.expedia.com"
    "k8s.io/etcd/events" = "2/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "2-etcd-main-haystack-k8s-monitoring-test-expedia-com" {
  availability_zone = "us-west-2c"
  size              = 20
  type              = "gp2"
  encrypted         = false

  tags = {
    KubernetesCluster    = "haystack-k8s-monitoring.test.expedia.com"
    Name                 = "2.etcd-main.haystack-k8s-monitoring.test.expedia.com"
    "k8s.io/etcd/main"   = "2/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "3-etcd-events-haystack-k8s-monitoring-test-expedia-com" {
  availability_zone = "us-west-2c"
  size              = 20
  type              = "gp2"
  encrypted         = false

  tags = {
    KubernetesCluster    = "haystack-k8s-monitoring.test.expedia.com"
    Name                 = "3.etcd-events.haystack-k8s-monitoring.test.expedia.com"
    "k8s.io/etcd/events" = "3/1,2,3"
    "k8s.io/role/master" = "1"
  }
}

resource "aws_ebs_volume" "3-etcd-main-haystack-k8s-monitoring-test-expedia-com" {
  availability_zone = "us-west-2c"
  size              = 20
  type              = "gp2"
  encrypted         = false

  tags = {
    KubernetesCluster    = "haystack-k8s-monitoring.test.expedia.com"
    Name                 = "3.etcd-main.haystack-k8s-monitoring.test.expedia.com"
    "k8s.io/etcd/main"   = "3/1,2,3"
    "k8s.io/role/master" = "1"
  }
}
*/