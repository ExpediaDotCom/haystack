resource "aws_iam_instance_profile" "masters-profile" {
  name = "${var.cluster_name}-eks-masters"
  role = "${aws_iam_role.masters-role.name}"
}

resource "aws_iam_instance_profile" "nodes-profile" {
  name = "${var.cluster_name}-eks-nodes"
  role = "${aws_iam_role.nodes-role.name}"
}

resource "aws_iam_role" "masters-role" {
  name = "${var.cluster_name}-eks-masters"
  assume_role_policy = "${file("${path.module}/manifests/masters_iam-role.json")}"
}

resource "aws_iam_role" "nodes-role" {
  name = "${var.cluster_name}-eks-nodes"
  assume_role_policy = "${file("${path.module}/manifests/nodes_iam-role.json")}"
}

resource "aws_iam_role_policy_attachment" "master-AmazonEKSClusterPolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = "${aws_iam_role.masters-role.name}"
}

resource "aws_iam_role_policy_attachment" "master-AmazonEKSServicePolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSServicePolicy"
  role       = "${aws_iam_role.masters-role.name}"
}

resource "aws_iam_role_policy_attachment" "nodes-AmazonEKSWorkerNodePolicy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = "${aws_iam_role.nodes-role.name}"
}
resource "aws_iam_role_policy_attachment" "nodes-SecretsManagerPolicy" {
  policy_arn = "arn:aws:iam::aws:policy/SecretsManagerReadWrite"
  role       = "${aws_iam_role.nodes-role.name}"
}

resource "aws_iam_role_policy_attachment" "nodes-AmazonEKS_CNI_Policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = "${aws_iam_role.nodes-role.name}"
}

resource "aws_iam_role_policy_attachment" "demo-node-AmazonEC2ContainerRegistryReadOnly" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = "${aws_iam_role.nodes-role.name}"
}

resource "aws_iam_role_policy" "worker-node-assume-role" {
  name = "worker-node-assume-role"
  role = "${aws_iam_role.nodes-role.id}"
  policy ="${file("${path.module}/manifests/nodes_iam-policy.json")}"
}