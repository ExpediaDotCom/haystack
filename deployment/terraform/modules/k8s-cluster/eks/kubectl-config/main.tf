locals {
  kubeconfig = "${data.template_file.kubeconfig.rendered}"
  config_map_aws_auth = "${data.template_file.config_map_aws_auth.rendered}"
  rendered_kube_config_path = "${path.module}/templates/cluster-config.yaml"

}

data "template_file" "kubeconfig" {
  template = "${file("${path.module}/templates/kubeconfig.tpl")}"
  vars {
    aws_eks_cluster_endpoint = "${var.aws_eks_cluster_endpoint}"
    cluster_name = "${var.cluster_name}"
    certificate-authority-data = "${var.certificate-authority-data}"
    aws_iam_authenticator = "${var.aws_iam_authenticator_executable_name}"
  }
}

data "template_file" "config_map_aws_auth" {
  template = "${file("${path.module}/templates/configmap-awsauth.tpl")}"
  vars {
    nodes_role_arn = "${var.nodes_role_arn}"
  }
}


resource "null_resource" "create_kubectl_configuration" {
  provisioner "local-exec" {
    command = "cat > ${local.rendered_kube_config_path} <<EOL\n${local.kubeconfig}EOL"
  }
  triggers {
    uuid = "${uuid()}" # trigger always
  }
}


resource "null_resource" "config_map_aws_auth" {
  triggers {
    template = "${data.template_file.config_map_aws_auth.rendered}"
  }
  provisioner "local-exec" {
    command = "echo '${data.template_file.config_map_aws_auth.rendered}' | ${var.kubectl_executable_name} create -f - --kubeconfig ${local.rendered_kube_config_path}"
  }
  depends_on = ["null_resource.create_kubectl_configuration"]
}
