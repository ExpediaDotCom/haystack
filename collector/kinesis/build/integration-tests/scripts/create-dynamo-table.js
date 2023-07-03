var AWS = require('aws-sdk');

var config = {
  "accessKeyId": "FAKE",
  "secretAccessKey": "FAKE",
  "region": "us-east-1",
  "dynamoEndpoint": "http://localstack:4569",
  "tableName": "haystack-kinesis-proto-span-collector",
  "ShardCount": 1
};

var dynamodb = new AWS.DynamoDB({ endpoint: new AWS.Endpoint(config.dynamoEndpoint),
    accessKeyId: config.accessKeyId,
    secretAccessKey: config.secretAccessKey,
    region: config.region});

var params = {
    TableName : config.tableName,
    KeySchema: [
        { AttributeName: "leaseKey", KeyType: "HASH"}  //Partition key
    ],
    AttributeDefinitions: [
        { AttributeName: "leaseKey", AttributeType: "S" }
    ],
    ProvisionedThroughput: {
        ReadCapacityUnits: 10,
        WriteCapacityUnits: 10
    }
};

dynamodb.createTable(params, function(err, data) {
    if (err) {
        console.error("Unable to create table. Error JSON:", JSON.stringify(err, null, 2));
    } else {
        console.log("Created table. Table description JSON:", JSON.stringify(data, null, 2));
    }
});