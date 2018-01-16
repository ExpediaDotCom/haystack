#!/bin/bash

# get ip from metadata service
local_ip=`curl -s http://169.254.169.254/latest/meta-data/local-ipv4`

# setup local ip in hosts
echo "127.0.0.1 $(hostname)" | sudo tee -a /etc/hosts

# setup config for JMX forwarding
JMX_TRANS_AGENT_FILE=/opt/jmxtrans/jmxtrans-agent.xml
sudo sed -i -e "s/_HAYSTACK_GRAPHITE_HOST/${haystack_graphite_host}/g" $JMX_TRANS_AGENT_FILE
sudo sed -i -e "s/_HAYSTACK_GRAPHITE_PORT/${haystack_graphite_port}/g" $JMX_TRANS_AGENT_FILE

# start service
sudo chmod a+w /var/log

sudo chmod a+x /etc/init.d/zookeeper
sudo chkconfig zookeeper on
sudo service zookeeper start