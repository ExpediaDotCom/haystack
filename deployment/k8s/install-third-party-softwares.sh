#!/bin/bash

# This is a simple utitility script to install the binaries for jq and gomplate to this repostiory
set -e

DIR=`pwd`
INSTALL_DIR=$DIR/third_party_softwares

case "$(uname -s)" in
   Darwin)
     echo 'Installing binaries for Mac/OS'
     mkdir -p $INSTALL_DIR/mac/x64
     curl -L https://github.com/stedolan/jq/releases/download/jq-1.5/jq-osx-amd64 -o $INSTALL_DIR/mac/x64/jq
     curl -L https://github.com/hairyhenderson/gomplate/releases/download/v1.9.1/gomplate_darwin-amd64 -o $INSTALL_DIR/mac/x64/gomplate
     mkdir -p $INSTALL_DIR/mac/x64/kubectl/1.6.7
     curl -L https://storage.googleapis.com/kubernetes-release/release/v1.6.7/bin/darwin/amd64/kubectl -o $INSTALL_DIR/mac/x64/kubectl/1.6.7/kubectl
     ;;

   Linux)
     echo 'Installing binaries for Linux'
     mkdir -p $INSTALL_DIR/linux/x64
     curl -L https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64 -o $INSTALL_DIR/linux/x64/jq
     curl -L https://github.com/hairyhenderson/gomplate/releases/download/v1.9.1/gomplate_linux-amd64 -o $INSTALL_DIR/linux/x64/gomplate
     mkdir -p $INSTALL_DIR/linux/x64/kubectl/1.5.7
     curl -L https://storage.googleapis.com/kubernetes-release/release/v1.6.7/bin/linux/amd64/kubectl -o $INSTALL_DIR/linux/x64/kubectl/1.6.7/kubectl
     ;;

   *)
     echo 'this OS is not supported yet !' 
    ;;
esac
find $INSTALL_DIR -type f -exec chmod +x {} \;
