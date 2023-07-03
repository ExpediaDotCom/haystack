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
  accumulator {
      interval = 1000
  }
}
```
* Build node-finder application locally (node-finder app root)
```
mvn clean package
```
* Start the application (node-finder app root)
```
export HAYSTACK_OVERRIDES_CONFIG_PATH=<path>/local.conf
java -jar target/haystack-service-graph-node-finder.jar
```
* Send data to Kafka (refer to fakespans tool README)
```
$GOBIN/fakespans --from-file fakespans.json --kafka-broker localhost:9092
```
* Check the output topics (from kafka home)
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic graph-nodes --from-beginning

bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic metricpoints --from-beginning
```
