#!/bin/bash

# get ip from metadata service
local_ip=`curl -s http://169.254.169.254/latest/meta-data/local-ipv4`

# setup local ip in hosts
echo "127.0.0.1 $(hostname)" | sudo tee -a /etc/hosts

# replace ips from configs
sudo sed -i -e "s/_HAYSTACK_GRAPHITE_HOST/${haystack_graphite_host}/g" /var/jmxtrans/jmxtrans-agent.xml
sudo sed -i -e "s/_HAYSTACK_GRAPHITE_PORT/${haystack_graphite_port}/g" /var/jmxtrans/jmxtrans-agent.xml

# start service
sudo mkdir /var/log
sudo chmod a+w /var/log
KAFKA_CONSOLE_LOG=/var/log/kafka.log
ZOOKEEPER_CONSOLE_LOG=/var/log/zookeeper.log

sudo nohup sh /opt/kafka/bin/zookeeper-server-start.sh /opt/kafka/config/zookeeper.properties 2>&1 >> $ZOOKEEPER_CONSOLE_LOG 2>&1 &
sudo nohup sh /opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/server.properties 2>&1 >> $KAFKA_CONSOLE_LOG 2>&1 &