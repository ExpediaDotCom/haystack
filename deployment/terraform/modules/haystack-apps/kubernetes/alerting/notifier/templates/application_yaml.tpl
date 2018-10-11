kafka:
  topic: alerts
  consumer:
    bootstrap.servers: "${kafka_endpoint}"
    group.id: "notifier"
    key.deserializer: "org.apache.kafka.common.serialization.StringDeserializer"
    value.deserializer: "com.expedia.adaptivealerting.kafka.serde.JsonPojoDeserializer"
    jsonPojoClass: "com.expedia.adaptivealerting.core.data.MappedMetricData"
    auto.offset.reset: "earliest"
    session.timeout.ms: 30000
    heartbeat.interval.ms: 10000
    request.timeout.ms: 40000

webhook.url: "${webhook_url}"
