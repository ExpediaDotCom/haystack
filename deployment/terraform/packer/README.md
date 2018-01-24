# Packer AMI Builder
 
 Builder script for creating Cassandra and Kafka golden image using Packer. There is still instance specific configuration that must be applied before Cassandra and Kafka nodes can be started.
 
 Update required configurations in `variable.json` and use the below command to create the AMI. Script installs Packer if its not available already. It will output AMI id once done successfully, please save that for later use. 
 ```bash
./build-image.sh
```
 
You don't need to build AMI everytime you create haystack cluster. Once you have an AMI ready for a region, Terraform scripts will quary based on tag `type:haystack-cassandra-base` or `type:haystack-kafka-base`


## Cassandra
TODO

## Kafka
This ami is configured to be used as common ami for both kafka and zookeeper. It has binary and service scripts for both zookeeper and kafka svc.

Here are some useful details regarding the AMI -
- Use `service` command for maintaining kafka/zk services, eg.
```bash
$ sudo service kafka status
```
```bash
$ sudo service zookeeper status
```
- Important paths/files
    - `/opt/kafka` : root for kafka and zookeeper binary & configurations
    - `/opt/kafka/config/server.properties` : kafka configurations
    - `/var/log/kafka.log` : kafka service logs
    - `/opt/kafka/config/zookeeper.properties` : kafka configurations
    - `/var/log/zookeeper.log` : zookeeper service logs
    - `/tmp/zookeeper` : zookeeper data directory
- Use `admin` as default ssh user name on the generated AMIs

Please note that, these AMIs are not complete by themselves. There are some placeholders(eg. listen IPs, ZK IPs) that are being populated by Terrafor scripts. 