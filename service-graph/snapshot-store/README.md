#Haystack : snapshot-store

The "snapshot" feature of the service graph starts with a call (made at regular intervals) to the service-graph service,
asking for a current copy of the service graph; the service graph that it receives is then persisted. Currently two 
types of persistent storage are supported:
1. The local file system, for development work and small installations
2. Amazon [AWS S3](https://aws.amazon.com/s3/)

The persistent copies can then be queried to observe the service graph at a point of time in the past.

This snapshot-store package contains code to manage both types of durable locations.

The persistent copies of the service-graph will be purged after a suitable amount of time. Since S3 has an 
[object expiration feature](https://aws.amazon.com/blogs/aws/amazon-s3-object-expiration/), there is no need 
for snapshot-store code to purge data from S3. The code for the local file system does purge expired data.

Snapshots are stored in two CSV files: one for edges and one for nodes. These files can be consumed by 
[Spark DataFrames](https://spark.apache.org/docs/2.3.0/sql-programming-guide.html#datasets-and-dataframes)
and were chosen as the storage format to facilitate using Spark or similar tools to analyze historical service graphs.

## Building

```
mvn clean package
```