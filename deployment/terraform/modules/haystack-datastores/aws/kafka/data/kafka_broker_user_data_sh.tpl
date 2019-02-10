#!/bin/bash

#format and mount ephemeral disk if the instance type from i3 family
if [[ `curl -s http://169.254.169.254/latest/meta-data/instance-type ` =~ ^(i3.*)$ ]]
then
    echo 'sudo mkfs.ext4 -E nodiscard /dev/nvme0n1' | sudo tee -a /etc/rc.local
    echo 'sudo mount -o discard /dev/nvme0n1 /var/kafka' | sudo tee -a /etc/rc.local
    echo 'sudo service kafka restart' | sudo tee -a /etc/rc.local
fi

# get ip from metadata service
local_ip=`curl -s http://169.254.169.254/latest/meta-data/local-ipv4`

# setup local ip in hosts
echo "127.0.0.1 $(hostname)" | sudo tee -a /etc/hosts

# setup config for JMX forwarding
JMX_TRANS_AGENT_FILE=/opt/jmxtrans/jmxtrans-agent-kafkaServer.xml
sudo sed -i -e "s/_HAYSTACK_GRAPHITE_HOST/${haystack_graphite_host}/g" $JMX_TRANS_AGENT_FILE
sudo sed -i -e "s/_HAYSTACK_GRAPHITE_PORT/${haystack_graphite_port}/g" $JMX_TRANS_AGENT_FILE

# update kafka config
KAFKA_SERVER_PROPERTIES_FILE=/opt/kafka/config/server.properties
BROKER_ID=$((RANDOM % 1000))
sudo sed -i -e "s/_BROKER_ID/$BROKER_ID/g" $KAFKA_SERVER_PROPERTIES_FILE
sudo sed -i -e "s/_ZOOKEEPER_HOSTS/${zookeeper_hosts}/g" $KAFKA_SERVER_PROPERTIES_FILE
sudo sed -i -e "s/_LOCAL_IP/$local_ip/g" $KAFKA_SERVER_PROPERTIES_FILE
sudo sed -i -e "s/_NUM_PARTITIONS/${num_partitions}/g" $KAFKA_SERVER_PROPERTIES_FILE
sudo sed -i -e "s/_RETENTION_HOURS/${retention_hours}/g" $KAFKA_SERVER_PROPERTIES_FILE
sudo sed -i -e "s/_RETENTION_BYTES/${retention_bytes}/g" $KAFKA_SERVER_PROPERTIES_FILE
sudo sed -i -e "/broker.id/ a \broker.rack=${broker_rack}" $KAFKA_SERVER_PROPERTIES_FILE

# start service
sudo chmod a+w /var/log
sudo chmod a+x /etc/init.d/kafka
sudo chkconfig kafka on
sudo service kafka start
