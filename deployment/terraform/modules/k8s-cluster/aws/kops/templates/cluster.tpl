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
      type: Public
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
  networkCIDR: ${aws_network_cidr}
  networking:
    calico: {}
  nonMasqueradeCIDR: 100.64.0.0/10
  sshAccess:
  - 0.0.0.0/0
  subnets:
  - name: ${aws_zone}
    type: Private
    zone: ${aws_zone}
    id: ${aws_node_subnet}
  - name: utility-${aws_zone}
    type: Utility
    zone: us-west-2c
    id: ${aws_utilities_subnet}



  topology:
    dns:
      type: Public
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
  name: app-nodes
spec:
  associatePublicIp: false
  image: kope.io/k8s-1.8-debian-jessie-amd64-hvm-ebs-2017-12-02
  machineType: ${app-node_instance_type}
  maxSize: ${app-node_instance_count}
  minSize: ${app-node_instance_count}
  nodeLabels:
    kops.k8s.io/instancegroup: app-nodes
  role: Node
  subnets:
  - ${aws_zone}

---

apiVersion: kops/v1alpha2
kind: InstanceGroup
metadata:
  creationTimestamp: 2017-12-11T07:32:33Z
  labels:
    kops.k8s.io/cluster: ${cluster_name}
  name: monitoring-nodes
spec:
  associatePublicIp: false
  image: kope.io/k8s-1.8-debian-jessie-amd64-hvm-ebs-2017-12-02
  machineType: ${monitoring-node_instance_type}
  maxSize: ${monitoring-node_instance_count}
  minSize: ${monitoring-node_instance_count}
  nodeLabels:
    kops.k8s.io/instancegroup: monitoring-nodes
  role: Node
  subnets:
  - ${aws_zone}
