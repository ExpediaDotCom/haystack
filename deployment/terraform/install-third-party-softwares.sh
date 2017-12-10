#!/bin/bash

# This is a simple utitility script to install the binaries for jq and gomplate to this repostiory
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
     ;;

   Linux)
     echo 'Installing binaries for Linux'
     mkdir -p $INSTALL_DIR/linux/x64
     curl -L https://releases.hashicorp.com/terraform/0.11.1/terraform_0.11.1_linux_amd64.zip?_ga=2.175573474.1964440187.1512375886-1169145750.1512375886 -o $INSTALL_DIR/linux/x64/terraform.zip
     unzip -o $INSTALL_DIR/mac/x64/terraform.zip  -d $INSTALL_DIR/mac/x64/ ;
     rm $INSTALL_DIR/mac/x64/terraform.zip;
     ;;

   *)
     echo 'this OS is not supported yet !' 
    ;;
esac
find $INSTALL_DIR -type f -exec chmod +x {} \;
