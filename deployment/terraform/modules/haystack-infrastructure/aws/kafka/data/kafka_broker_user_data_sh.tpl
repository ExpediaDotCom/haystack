#!/bin/bash

# get ip from metadata service
local_ip=`curl -s http://169.254.169.254/latest/meta-data/local-ipv4`

# setup local ip in hosts
echo "127.0.0.1 $(hostname)" | sudo tee -a /etc/hosts

# replace ips from configs
sudo sed -i -e "s/_HAYSTACK_GRAPHITE_HOST/${haystack_graphite_host}/g" /var/jmxtrans/jmxtrans-agent.xml
sudo sed -i -e "s/_HAYSTACK_GRAPHITE_PORT/${haystack_graphite_port}/g" /var/jmxtrans/jmxtrans-agent.xml

# start service
sudo cd /opt
sudo bin/zookeeper-server-start.sh config/zookeeper.properties
sudo bin/kafka-server-start.sh config/server.properties