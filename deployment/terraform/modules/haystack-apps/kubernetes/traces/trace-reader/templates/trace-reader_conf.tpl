service {
    port = 8080
    ssl {
      enabled = false
      cert.path = ""
      private.key.path = ""
    }
}

cassandra {
  # multiple endpoints can be provided as comma separated
   endpoints: "${cassandra_hostname}"

  # enable the auto.discovery mode, if true then we ignore the endpoints(above) and use auto discovery
  # mechanism to find cassandra nodes. For today we only support aws node discovery provider
  # auto.discovery {
  #  enabled: false
  #    aws: {
  #       region: "us-west-2"
  #       tags: {
  #         Role: haystack-cassandra
  #         Environment: ewetest
  #       }
  #     }
  #}

  credentials {
    username = "cassandra",
    password = "cassandra"
  }

  connections {
    max.per.host = 10
    read.timeout.ms = 30000
    conn.timeout.ms = 10000
    keep.alive = true
  }

  keyspace {
    name = "haystack"
    table.name = "traces"
  }
}

elasticsearch {
  endpoint = "http://${elasticsearch_endpoint}"
  conn.timeout.ms = 10000
  read.timeout.ms = 30000

  index {
    name.prefix = "haystack-traces"
    type = "spans"
  }
  
  use.root.doc.starttime = true
}

trace {
  validators {
    sequence = [
      "com.expedia.www.haystack.trace.reader.readers.validators.TraceIdValidator"
    ]
  }

  transformers {
    pre {
      sequence = [
        "com.expedia.www.haystack.trace.reader.readers.transformers.DeDuplicateSpanTransformer"
        "com.expedia.www.haystack.trace.reader.readers.transformers.ClientServerEventLogTransformer"
      ]
    }
    post {
      sequence = [
        "com.expedia.www.haystack.trace.reader.readers.transformers.PartialSpanTransformer"
        "com.expedia.www.haystack.trace.reader.readers.transformers.ServerClientSpanMergeTransformer"
        "com.expedia.www.haystack.trace.reader.readers.transformers.InvalidRootTransformer"
        "com.expedia.www.haystack.trace.reader.readers.transformers.InvalidParentTransformer"
        "com.expedia.www.haystack.trace.reader.readers.transformers.ClockSkewTransformer"
        "com.expedia.www.haystack.trace.reader.readers.transformers.SortSpanTransformer"
      ]
    }
  }
}

service.metadata {
  enabled = true

  cassandra {
    keyspace {
      name = "haystack_metadata"
      table.name = "services"
    }
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
