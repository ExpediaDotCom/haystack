#Haystack : snapshotter

The "snapshot" feature of the service graph is a Scala "main" application that runs the specified
[snapshot-store](https://github.com/ExpediaDotCom/haystack-service-graph). The 
[Scala Main class](https://github.com/ExpediaDotCom/haystack-service-graph/blob/master/snapshotter/src/main/scala/com/expedia/www/haystack/service/graph/snapshotter/Main.scala)
expects as its first argument the fully qualified class name of the snapshot store to use. More precisely:

1. The first parameter is the fully qualified class name of the implementation of the snapshot store to run. There are
are currently two implementations:
  * [com.expedia.www.haystack.service.graph.snapshot.store.FileSnapshotStore](https://github.com/ExpediaDotCom/haystack-service-graph/blob/master/snapshot-store/src/main/scala/com.expedia.www.haystack.service.graph.snapshot.store.FileSnapshotStore)
  * [com.expedia.www.haystack.service.graph.snapshot.store.S3SnapshotStore](https://github.com/ExpediaDotCom/haystack-service-graph/blob/master/snapshot-store/src/main/scala/com.expedia.www.haystack.service.graph.snapshot.store.S3SnapshotStore)
2. The rest of the arguments are passed to the constructor of the class specified by args(0).
  * For FileSnapshotStore, the only additional argument required is the directory name where the snapshots will be stored,
    e.g. /var/snapshots
  * For S3SnapshotStore, there are three additional arguments, which are in order:
    * the bucket name
    * the folder name inside the bucket
    * the number of items to fetch at one time when calling the S3 listObjectsV2 API; the best value to choose,
      assuming sufficient memory on the JVM running the snapshotter, is the maximum number of snapshots that will exist 
      in S3 before being purged. For example, with a one hour snapshot interval and a snapshot TTL of 1 year, 
      366 * 24 = 8784 would be a good value (perhaps rounded to 10,000).
      
## Building

```
mvn clean package
```