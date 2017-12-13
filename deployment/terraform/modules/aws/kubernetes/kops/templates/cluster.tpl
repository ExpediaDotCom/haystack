############################################
# NOTE: This file is managed by Terraform  #
# Don't make any manual modification to it #
############################################
apiVersion: kops/v1alpha2
kind: Cluster
metadata:
  creationTimestamp: 2017-12-11T07:32:25Z
  name: ${cluster_name}
spec:
  api:
    loadBalancer:
      type: Internal
  authorization:
    alwaysAllow: {}
  channel: stable
  cloudProvider: aws
  configBase: s3://${s3_bucket_name}/${cluster_name}
  dnsZone: ${aws_dns_zone_id}
  etcdClusters:
  - etcdMembers:
    - instanceGroup: master-${aws_zone}-1
      name: "1"
    - instanceGroup: master-${aws_zone}-2
      name: "2"
    - instanceGroup: master-${aws_zone}-3
      name: "3"
    name: main
  - etcdMembers:
    - instanceGroup: master-${aws_zone}-1
      name: "1"
    - instanceGroup: master-${aws_zone}-2
      name: "2"
    - instanceGroup: master-${aws_zone}-3
      name: "3"
    name: events
  iam:
    allowContainerRegistry: true
    legacy: false
  kubernetesApiAccess:
  - 0.0.0.0/0
  kubernetesVersion: ${k8s_version}
  masterPublicName: api.${cluster_name}
  networkID: ${aws_vpc_id}
  networking:
    calico: {}
  nonMasqueradeCIDR: 100.64.0.0/10
  sshAccess:
  - 0.0.0.0/0
  subnets:
  - name: ${aws_zone}
    type: Private
    zone: ${aws_zone}
    id: ${aws_subnet}
  topology:
    dns:
      type: Private
    masters: private
    nodes: private

---

apiVersion: kops/v1alpha2
kind: InstanceGroup
metadata:
  creationTimestamp: 2017-12-11T07:32:30Z
  labels:
    kops.k8s.io/cluster: ${cluster_name}
  name: master-${aws_zone}-1
spec:
  associatePublicIp: false
  image: kope.io/k8s-1.8-debian-jessie-amd64-hvm-ebs-2017-12-02
  machineType: ${master_instance_type}
  maxSize: 1
  minSize: 1
  nodeLabels:
    kops.k8s.io/instancegroup: master-${aws_zone}-1
  role: Master
  subnets:
  - ${aws_zone}

---

apiVersion: kops/v1alpha2
kind: InstanceGroup
metadata:
  creationTimestamp: 2017-12-11T07:32:31Z
  labels:
    kops.k8s.io/cluster: ${cluster_name}
  name: master-${aws_zone}-2
spec:
  associatePublicIp: false
  image: kope.io/k8s-1.8-debian-jessie-amd64-hvm-ebs-2017-12-02
  machineType: ${master_instance_type}
  maxSize: 1
  minSize: 1
  nodeLabels:
    kops.k8s.io/instancegroup: master-${aws_zone}-2
  role: Master
  subnets:
  - ${aws_zone}

---

apiVersion: kops/v1alpha2
kind: InstanceGroup
metadata:
  creationTimestamp: 2017-12-11T07:32:32Z
  labels:
    kops.k8s.io/cluster: ${cluster_name}
  name: master-${aws_zone}-3
spec:
  associatePublicIp: false
  image: kope.io/k8s-1.8-debian-jessie-amd64-hvm-ebs-2017-12-02
  machineType: ${master_instance_type}
  maxSize: 1
  minSize: 1
  nodeLabels:
    kops.k8s.io/instancegroup: master-${aws_zone}-3
  role: Master
  subnets:
  - ${aws_zone}

---

apiVersion: kops/v1alpha2
kind: InstanceGroup
metadata:
  creationTimestamp: 2017-12-11T07:32:33Z
  labels:
    kops.k8s.io/cluster: ${cluster_name}
  name: nodes
spec:
  associatePublicIp: false
  image: kope.io/k8s-1.8-debian-jessie-amd64-hvm-ebs-2017-12-02
  machineType: ${node_instance_type}
  maxSize: ${node_instance_count}
  minSize: ${node_instance_count}
  nodeLabels:
    kops.k8s.io/instancegroup: nodes
  role: Node
  subnets:
  - ${aws_zone}
