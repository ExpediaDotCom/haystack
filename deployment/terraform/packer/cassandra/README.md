# Packer Cassandra AMI Builder
 
 Builder script for creating Cassandra golden image using Packer. There is still instance specific configuration that must be applied before Cassandra nodes can be started.
 
 Update required configurations in `variable.json` and use the below command to create the AMI. Script installs Packer if its not available already. It will output AMI id once done successfully, please save that for later use. 
 ```bash
./build-image.sh
```
 
You don't need to build AMI everytime you create haystack cluster. Once you have an AMI ready for a region, Terraform scripts will quary based on tag `type:haystack-cassandra-base`