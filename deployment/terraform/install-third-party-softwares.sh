#!/bin/bash

set -e

DIR=`pwd`
INSTALL_DIR=$DIR/third_party_softwares

case "$(uname -s)" in
   Darwin)
     echo 'Installing binaries for Mac/OS'
     mkdir -p $INSTALL_DIR/mac/x64
     curl -L https://releases.hashicorp.com/terraform/0.11.1/terraform_0.11.1_darwin_amd64.zip -o $INSTALL_DIR/mac/x64/terraform.zip;
     unzip -o $INSTALL_DIR/mac/x64/terraform.zip  -d $INSTALL_DIR/mac/x64/ ;
     rm $INSTALL_DIR/mac/x64/terraform.zip ;
     curl -L https://storage.googleapis.com/kubernetes-release/release/v1.8.0/bin/darwin/amd64/kubectl -o $INSTALL_DIR/mac/x64/kubectl
     curl -L https://github.com/kubernetes/kops/releases/download/1.8.0/kops-darwin-amd64 -o $INSTALL_DIR/mac/x64/kops
     ;;

   Linux)
     echo 'Installing binaries for Linux'
     mkdir -p $INSTALL_DIR/linux/x64
     curl -L https://releases.hashicorp.com/terraform/0.11.1/terraform_0.11.1_linux_amd64.zip -o $INSTALL_DIR/linux/x64/terraform.zip
     unzip -o $INSTALL_DIR/linux/x64/terraform.zip  -d $INSTALL_DIR/linux/x64/ ;
     rm $INSTALL_DIR/linux/x64/terraform.zip;
     curl -L https://storage.googleapis.com/kubernetes-release/release/v1.8.0/bin/linux/amd64/kubectl -o $INSTALL_DIR/linux/x64/kubectl
     curl -L https://github.com/kubernetes/kops/releases/download/1.8.0/kops-linux-amd64 -o $INSTALL_DIR/linux/x64/kops

     ;;

   *)
     echo 'this OS is not supported yet !' 
    ;;
esac
find $INSTALL_DIR -type f -exec chmod +x {} \;
