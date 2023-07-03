# haystack-kinesis-span-collector
This haystack component reads the batch of spans from kinesis stream and publish them to kafka topic.
It expects the [Batch](https://github.com/ExpediaDotCom/haystack-idl/blob/master/proto/span.proto) protobuf object in the stream.
It deserializes this proto object and use each span's TraceId as the partition key for writing to the kafka topic. 
This component uses the [KCL](http://docs.aws.amazon.com/streams/latest/dev/developing-consumers-with-kcl.html#kinesis-record-processor-overview-kcl) 
library to build the pipeline for reading from kinesis stream and writing to kafka using high level kafka consumer api.

##Required Reading
 
In order to understand the haystack, we recommend to read the details of [haystack](https://github.com/ExpediaDotCom/haystack) project. 

##Technical Details
Fill this as we go along..
