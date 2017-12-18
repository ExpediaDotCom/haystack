# Packer Cassandra AMI Builder
 
 Builder script for creating Cassandra golden image using Packer. There is still instance specific configuration that must be applied before Cassandra nodes can be started.
 
 Update required configurations in `variable.json` and use the below command to create the AMI. It will output AMI id once done successfully, please save that for later use. 
 ```bash
./build-image.sh
```
 
 