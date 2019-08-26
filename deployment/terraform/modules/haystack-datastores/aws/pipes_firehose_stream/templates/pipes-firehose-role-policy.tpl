{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "S3DestinationAccess",
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:ListBucketMultipartUploads",
                "s3:AbortMultipartUpload",
                "s3:ListBucket",
                "s3:GetBucketLocation"
            ],
            "Resource": [
                "arn:aws:s3:::${s3_bucket_name}/*",
                "arn:aws:s3:::${s3_bucket_name}"
            ]
        }
    ]
}