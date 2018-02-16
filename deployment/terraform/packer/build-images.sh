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

echo =============== building cassandra ami =========================
cd cassandra
$PACKER build -var-file=$DIR/variables.json cassandra-ami.json
cd -

echo =============== building kafka ami =============================
cd kafka
#$PACKER build -var-file=$DIR/variables.json kafka-ami.json
cd -

echo ================================================================