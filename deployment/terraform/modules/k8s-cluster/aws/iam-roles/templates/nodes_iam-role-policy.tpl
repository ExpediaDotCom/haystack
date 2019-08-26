{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "kopsK8sEC2NodePerms",
      "Effect": "Allow",
      "Action": [
        "ec2:DescribeInstances"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Sid": "kopsK8sS3GetListBucket",
      "Effect": "Allow",
      "Action": [
        "s3:GetBucketLocation",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::${s3_bucket_name}"
      ]
    },
    {
      "Sid": "kopsK8sS3NodeBucketSelectiveGet",
      "Effect": "Allow",
      "Action": [
        "s3:Get*"
      ],
      "Resource": [
        "arn:aws:s3:::${s3_bucket_name}/*"
      ]
    },
    {
      "Sid": "kopsK8sECR",
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:GetRepositoryPolicy",
        "ecr:DescribeRepositories",
        "ecr:ListImages",
        "ecr:BatchGetImage"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Sid": "kinesisStream",
      "Effect": "Allow",
      "Action": [
        "kinesis:GetShardIterator",
        "kinesis:GetRecords",
        "kinesis:DescribeStream",
        "kinesis:PutRecord",
        "kinesis:PutRecords",
        "kinesis:DescribeStream"
      ],
      "Resource": [
        "arn:aws:kinesis:${kinesis-stream-region}:${account_id}:stream/haystack-*"
      ]
    },
    {
      "Sid": "kinesisFirehoseDeliveryStream",
      "Effect": "Allow",
      "Action": [
        "firehose:DeleteDeliveryStream",
        "firehose:PutRecord",
        "firehose:PutRecordBatch",
        "firehose:UpdateDestination"
      ],
      "Resource": [
        "arn:aws:firehose:${current_region}:${account_id}:deliverystream/haystack-*"
      ]
    },
    {
      "Sid": "dynamoDb",
      "Effect": "Allow",
      "Action": [
        "dynamodb:CreateTable",
        "dynamodb:BatchGetItem",
        "sts:AssumeRole",
        "dynamodb:BatchWriteItem",
        "cloudwatch:PutMetricData",
        "dynamodb:PutItem",
        "dynamodb:ListTables",
        "dynamodb:DescribeTable",
        "dynamodb:GetItem",
        "dynamodb:Scan",
        "dynamodb:UpdateItem",
        "dynamodb:GetRecords",
        "dynamodb:DeleteItem"
      ],
      "Resource": [
        "arn:aws:dynamodb:${kinesis-stream-region}:${account_id}:table/haystack-*"
      ]
    }
  ]
}
