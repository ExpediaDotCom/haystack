# Http Span Collector

The http collector is a web service built on akka-http. It accepts [proto](https://github.com/ExpediaDotCom/haystack-idl/tree/master/proto) serialized and json serialized spans on port 8080(configurable).

Collector has two endpoints
 1.  `/span`: It is for span ingestion. The 'Content-Type' header is used to understand the data format. Therefore one needs to set it correctly as:
 * `application/json`:  Json formatted spans 
 * `application/octet-stream`: Proto-serialized binary format.

 2. `/isActive`: It can be used as a health check by your load balancer
 
### How to publish spans in json format

Span's json schema should match with the object model described [here](./src/main/scala/com/expedia/www/haystack/http/span/collector/json/Span.scala)

```
curl -XPOST -H "Content-Type: application/json" -d ' \
{
    "traceId": "466848c0-a105-4867-8685-e3d00e3eb254",
    "spanId": "8f79f97b-a317-4c8f-bbfd-5fd228550416",
    "serviceName": "baz",
    "operationName": "foo",
    "startTime": 1521482680950000,
    "duration": 2000,
    "tags": [{
        "key": "span.kind",
        "value": "server"
    }, {
        "key": "error",
        "value": false
    }]
}' \
"http://localhost:8080/span"
```

### How to publish spans in proto format

```
curl -XPOST -H "Content-Type: application/octet-stream" -d '<proto-serialized-span>'  "http://localhost:8080/span"
```