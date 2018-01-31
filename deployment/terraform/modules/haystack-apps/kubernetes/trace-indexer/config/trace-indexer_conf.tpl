health.status.path = "/app/isHealthy"

span.accumulate {
  store {
    min.traces.per.cache = 1000 # this defines the minimum traces in each cache before eviction check is applied. This is also useful for testing the code
    all.max.entries = 100000 # this is the maximum number of spans that can live across all the stores
  }
  window.ms = 10000
  poll.ms = 2000
}

kafka {
  close.stream.timeout.ms = 15000

  topic.consume = "proto-spans"
  topic.produce = "${span_produce_topic}"

  num.stream.threads = 2
  poll.timeout.ms = 100

  # if consumer poll hangs, then wakeup it after after a timeout
  # also set the maximum wakeups allowed, if max threshold is reached, then task will raise the shutdown request
  max.wakeups = 10
  wakeup.timeout.ms = 3000

  commit.offset {
    retries = 3
    backoff.ms = 200
  }

  # consumer specific configurations
  consumer {
    group.id = "haystack-proto-trace-indexer"
    bootstrap.servers = "${kafka_endpoint}"
    auto.offset.reset = "latest"

    # disable auto commit as the app manages offset itself
    enable.auto.commit = "false"
  }

  # producer specific configurations
  producer {
    bootstrap.servers = "${kafka_endpoint}"
  }
}


cassandra {
  # multiple endpoints can be provided as comma separated
  endpoints: "${cassandra_hostname}"

  # defines the max inflight writes for cassandra
  max.inflight.requests = 100

  # enable the auto.discovery mode, if true then we ignore the endpoints(above) and use auto discovery
  # mechanism to find cassandra nodes. For today we only support aws node discovery provider
  auto.discovery {
    enabled: false
//    aws: {
//      region: "us-west-2"
//      tags: {
//        Role: haystack-cassandra
//        Environment: ewetest
//      }
//    }
  }

  connections {
    max.per.host = 10
    read.timeout.ms = 5000
    conn.timeout.ms = 10000
    keep.alive = true
  }

  consistency.level = "one"
  ttl.sec = 259200

  keyspace: {
    # auto creates the keyspace and table name in cassandra(if absent)
    # if schema field is empty or not present, then no operation is performed
    auto.create.schema = "CREATE KEYSPACE IF NOT EXISTS haystack WITH REPLICATION = { 'class': 'NetworkTopologyStrategy', 'us-west-2' : 2 } AND durable_writes = false; CREATE TABLE IF NOT EXISTS haystack.traces (id varchar, ts timestamp, spans blob, PRIMARY KEY ((id), ts)) WITH CLUSTERING ORDER BY (ts ASC) AND compaction = { 'class' :  'DateTieredCompactionStrategy', 'max_sstable_age_days': '3' } AND gc_grace_seconds = 86400;"

    name: "haystack"
    table.name: "traces"
  }
}

elasticsearch {
  endpoint = "http://${elasticsearch_endpoint}"

  # defines settings for bulk operation like max inflight bulks, number of documents and the total size in a single bulk
  bulk.max {
    docs {
      count = 100
      size.kb = 1000
    }
    inflight = 10
  }

  conn.timeout.ms = 10000
  read.timeout.ms = 5000
  consistency.level = "one"

  index {
    # apply the template before starting the client, if json is empty, no operation is performed
    template.json = "{\"template\":\"haystack-traces*\",\"settings\":{\"number_of_shards\":1,\"index.mapping.ignore_malformed\":true,\"analysis\":{\"normalizer\":{\"lowercase_normalizer\":{\"type\":\"custom\",\"filter\":[\"lowercase\"]}}}},\"aliases\":{\"haystack-traces\":{}},\"mappings\":{\"spans\":{\"_all\":{\"enabled\":false},\"_source\":{\"includes\":[\"traceid\"]},\"properties\":{\"traceid\":{\"enabled\":false},\"spans\":{\"type\":\"nested\",\"properties\":{\"servicename\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":true,\"norms\":false},\"operationname\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":true,\"norms\":false}}}},\"dynamic_templates\":[{\"strings_as_keywords_1\":{\"match_mapping_type\":\"string\",\"mapping\":{\"type\":\"keyword\",\"normalizer\":\"lowercase_normalizer\",\"doc_values\":false,\"norms\":false}}},{\"longs_disable_doc_norms\":{\"match_mapping_type\":\"long\",\"mapping\":{\"type\":\"long\",\"doc_values\":false,\"norms\":false}}}]}}}"

    name.prefix = "haystack-traces"
    type = "spans"
  }
}

reload {
  tables {
    index.fields.config = "indexing-fields"
  }
  config {
    endpoint = "http://${elasticsearch_endpoint}"
    database.name = "reload-configs"
  }
  startup.load = true
  interval.ms = 600000 # -1 will imply 'no reload'
}
