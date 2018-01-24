#!/bin/bash

# log output from user_data run in /var/log/user-data.log
exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1

# get ip from metadata service
local_ip=`curl -s http://169.254.169.254/latest/meta-data/local-ipv4`

# setup local ip in hosts
echo "127.0.0.1 $(hostname)" | sudo tee -a /etc/hosts


### Seed logic
getNodes()
{
SERVERCOUNT=`echo $SERVERS |wc -w`
while true
do
  if [ $SERVERCOUNT -lt ${seed_node_count} ]
  then
   echo "found $SERVERCOUNT ... retrying"
   SERVERS=$(aws ec2 describe-instances --output text  --filters "Name=tag:ClusterRole,Values=${clusterRole}" "Name=instance-state-name,Values=running" --query 'Reservations[*].Instances[*].[PrivateIpAddress,Tags[?Key==`Type`].Value[]]' --region us-west-2)
   SERVERCOUNT=`echo $SERVERS |wc -w`
  else
   echo "found servers: $SERVERS"
   break
  fi
  sleep 30
done
}
getNodes

seeds=""
for ID in $SERVERS
do
  seeds=$ID,$seeds
done

# replace ips from configs
sudo sed -i -e "s/_LOCAL_IP/$local_ip/g" /etc/cassandra/default.conf/cassandra.yaml
sudo sed -i -e "s/_SEED_IP/$seeds/g" /etc/cassandra/default.conf/cassandra.yaml
sudo sed -i -e "s/_HAYSTACK_GRAPHITE_HOST/${haystack_graphite_host}/g" /var/jmxtrans/jmxtrans-agent.xml
sudo sed -i -e "s/_HAYSTACK_GRAPHITE_PORT/${haystack_graphite_port}/g" /var/jmxtrans/jmxtrans-agent.xml

# start service
sudo chkconfig cassandra on
sudo service cassandra start