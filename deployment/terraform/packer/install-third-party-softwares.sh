#!/bin/bash

set -e

DIR=`pwd`
INSTALL_DIR=$DIR/third_party_softwares

case "$(uname -s)" in
   Darwin)
     echo 'Installing binaries for Mac/OS'
     mkdir -p $INSTALL_DIR/mac/x64
     curl -L https://releases.hashicorp.com/packer/1.1.3/packer_1.1.3_darwin_amd64.zip -o $INSTALL_DIR/mac/x64/packer.zip;
     unzip -o $INSTALL_DIR/mac/x64/packer.zip  -d $INSTALL_DIR/mac/x64/ ;
     rm $INSTALL_DIR/mac/x64/packer.zip ;
     ;;

   Linux)
     echo 'Installing binaries for Linux'
     mkdir -p $INSTALL_DIR/linux/x64
     curl -L https://releases.hashicorp.com/packer/1.1.3/packer_1.1.3_linux_amd64.zip -o $INSTALL_DIR/linux/x64/packer.zip;
     unzip -o $INSTALL_DIR/linux/x64/packer.zip  -d $INSTALL_DIR/linux/x64/ ;
     rm $INSTALL_DIR/linux/x64/packer.zip;
     ;;

   *)
     echo 'this OS is not supported yet !' 
    ;;
esac
find $INSTALL_DIR -type f -exec chmod +x {} \;
