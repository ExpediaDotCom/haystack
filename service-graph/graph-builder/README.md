#Haystack : node-finder

Information on what this component is all about is documented in the [README](../README.md) of the repository

## Building

```
mvn clean verify
```

or

```
make docker-image
```

## Testing Locally 

* Download Kafka 0.11.0.x
* Start Zookeeper locally (from kafka home)
```
bin/zookeeper-server-start.sh config/zookeeper.properties
```
* Start Kafka locally (from kafka home)
```
bin/kafka-server-start.sh config/server.properties
```
* Create proto-spans topic (from kafka home)
```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic proto-spans
```
* Create a local.conf override file
```
cat local.conf

health.status.path = "logs/isHealthy"

kafka {
  streams {
    bootstrap.servers = "localhost:9092"
  }
}
```
* Build graph-builder application locally (graph-builder app root)
```
mvn clean package
```
* Start the node-finder (node-finder app root)
```
export HAYSTACK_OVERRIDES_CONFIG_PATH=<path>/local.conf
java -jar target/haystack-service-graph-node-finder.jar
```

* Start application (graph-builder app root)
```
java -jar target/haystack-service-graph-graph-builder.jar
```
* Send data to Kafka (refer to fakespans tool README)
```
$GOBIN/fakespans --from-file fakespans.json --kafka-broker localhost:9092
```
* Check the output topics (from kafka home)
```
curl http://localhost:8080/servicegraph
```
