#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

case "$(uname -s)" in
  Darwin)
    THIRD_PARTY_SOFTWARE_PATH=$DIR/third_party_softwares/mac/x64
    ;;
  Linux)
    THIRD_PARTY_SOFTWARE_PATH=$DIR/third_party_softwares/linux/x64
    ;;
esac

# variable for accessing third party softwares
PACKER=$THIRD_PARTY_SOFTWARE_PATH/packer

if [ ! -f $PACKER ]; then
  $DIR/install-third-party-softwares.sh
fi

$PACKER build -var-file=variables.json cassandra-ami.json