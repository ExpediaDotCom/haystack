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
    }
  ]
}