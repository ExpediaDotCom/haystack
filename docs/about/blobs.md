---
title: Blobs
sidebar_label: Blobs
---

[Blobs](https://github.com/ExpediaDotCom/blobs) are a way to store any form of data for future need, typically with the request or response data of a service. When used with Haystack, the metadata of a blob is stored in the tag of a span, which is used for reading the blob in the future. 

Normally, sending too many tags with a span can strain a tracing infrastructure and greatly increase costs. With blobs, clients can include a much larger volume of information with spans, with retention rates kept separate from Haystack spans. 

## Using blobs with the Haystack agent
[Haystack agent](./clients.html#haystack-agent) has a blob provider that listens as a gRPC server and accepts protobuf blobs from clients. The blob agent supports the S3 dispatcher to send the blobs to a S3 bucket. You can set up the agent with the following configuration: 

```
agents {
     spans {
       enabled = true
       port = 35000
   
       dispatchers {
         // configure dispatchers
       }
     }
     ossblobs {
       enabled = true
       port = 35001
       max.blob.size.in.kb = 512
       dispatchers {
         s3 {
           keep.alive = true
           max.outstanding.requests = 150
           should.wait.for.upload = true
           max.connections = 50
           retry.count = 1
           bucket.name = "haystack-blobs"
           region = "us-east-1"
           aws.access.key = "accessKey"
           aws.secret.key = "secretKey"
         }
       }
     }
   }
```

An example of blobs and Haystack integrated together is viewable [here](https://github.com/ExpediaDotCom/haystack-blob-example). Another blob integration example is included with [haystack-docker](https://github.com/ExpediaDotCom/haystack-docker#to-start-haystacks-traces-blobs-trends-service-graph-and-adaptive-alerting), with more information available in the [getting started instructions](./getting_started.html#starting-haystack-components). 

## Blobs with Zipkin-style spans

Blobs are compatible with Zipkin spans as well when used in conjunction with [Pitchfork](./clients.html#pitchfork-agent). Example is coming soon. 
