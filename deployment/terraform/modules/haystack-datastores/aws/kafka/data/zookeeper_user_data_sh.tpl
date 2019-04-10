#!/bin/bash

# log output from user_data run in /var/log/user-data.log
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

# get ip from metadata service
local_ip=`curl -s http://169.254.169.254/latest/meta-data/local-ipv4`

# setup local ip in hosts
echo "127.0.0.1 $(hostname)" | sudo tee -a /etc/hosts

# setup config for JMX forwarding
JMX_TRANS_AGENT_FILE=/opt/jmxtrans/jmxtrans-agent-zookeeper.xml
sudo sed -i -e "s/_HAYSTACK_GRAPHITE_HOST/${haystack_graphite_host}/g" $JMX_TRANS_AGENT_FILE
sudo sed -i -e "s/_HAYSTACK_GRAPHITE_PORT/${haystack_graphite_port}/g" $JMX_TRANS_AGENT_FILE

### Seed logic
getservers()
{
SERVERCOUNT=`echo $SERVERS |wc -w`
while true
do
  if [ $SERVERCOUNT -lt ${zk_node_count} ]
  then
   echo "found $SERVERCOUNT ... retrying"
   SERVERS=$(aws ec2 describe-instances --output text  --filters "Name=tag:Role,Values=${role}" "Name=tag:ClusterName,Values=${cluster_name}" "Name=instance-state-name,Values=running" --query 'Reservations[*].Instances[*].[PrivateIpAddress,Tags[?Key==`Type`].Value[]]' --region us-west-2)
   SERVERCOUNT=`echo $SERVERS |wc -w`
  else
   echo "found servers: $SERVERS"
   break
  fi
  sleep 30
done
}

getservers

# build zookeeper configuration
sed -i -e "s/dataDir.*/dataDir=\/var\/zookeeper/g" /opt/kafka/config/zookeeper.properties
echo "tickTime=4000" >> /opt/kafka/config/zookeeper.properties
echo "initLimit=30" >> /opt/kafka/config/zookeeper.properties
echo "syncLimit=15" >> /opt/kafka/config/zookeeper.properties
mkdir /var/zookeeper
myid=$[${index} +1]
echo $myid > /var/zookeeper/myid

COUNT=0
while [ $COUNT -lt $SERVERCOUNT ]
do
  ZKNAME=${zk_a_name}$COUNT
  COUNT=$[$COUNT +1]
  echo "server.$COUNT=$ZKNAME:2888:3888" >> /opt/kafka/config/zookeeper.properties
done

# start service
sudo chmod a+w /var/log

sudo chmod a+x /etc/init.d/zookeeper
sudo chkconfig zookeeper on
sudo service zookeeper start
