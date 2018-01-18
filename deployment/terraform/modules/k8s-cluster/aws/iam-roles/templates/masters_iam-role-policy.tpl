{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "kopsK8sEC2MasterPermsDescribeResources",
      "Effect": "Allow",
      "Action": [
        "ec2:DescribeInstances",
        "ec2:DescribeRouteTables",
        "ec2:DescribeSecurityGroups",
        "ec2:DescribeSubnets",
        "ec2:DescribeVolumes"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Sid": "kopsK8sEC2MasterPermsAllResources",
      "Effect": "Allow",
      "Action": [
        "ec2:CreateRoute",
        "ec2:CreateSecurityGroup",
        "ec2:CreateTags",
        "ec2:CreateVolume",
        "ec2:ModifyInstanceAttribute"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Sid": "kopsK8sEC2MasterPermsTaggedResources",
      "Effect": "Allow",
      "Action": [
        "ec2:AttachVolume",
        "ec2:AuthorizeSecurityGroupIngress",
        "ec2:DeleteRoute",
        "ec2:DeleteSecurityGroup",
        "ec2:DeleteVolume",
        "ec2:DetachVolume",
        "ec2:RevokeSecurityGroupIngress"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Sid": "kopsK8sASMasterPermsAllResources",
      "Effect": "Allow",
      "Action": [
        "autoscaling:DescribeAutoScalingGroups",
        "autoscaling:DescribeLaunchConfigurations",
        "autoscaling:GetAsgForInstance"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Sid": "kopsK8sASMasterPermsTaggedResources",
      "Effect": "Allow",
      "Action": [
        "autoscaling:SetDesiredCapacity",
        "autoscaling:TerminateInstanceInAutoScalingGroup",
        "autoscaling:UpdateAutoScalingGroup"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Sid": "kopsK8sELBMasterPermsRestrictive",
      "Effect": "Allow",
      "Action": [
        "elasticloadbalancing:AttachLoadBalancerToSubnets",
        "elasticloadbalancing:ApplySecurityGroupsToLoadBalancer",
        "elasticloadbalancing:CreateLoadBalancer",
        "elasticloadbalancing:CreateLoadBalancerPolicy",
        "elasticloadbalancing:CreateLoadBalancerListeners",
        "elasticloadbalancing:ConfigureHealthCheck",
        "elasticloadbalancing:DeleteLoadBalancer",
        "elasticloadbalancing:DeleteLoadBalancerListeners",
        "elasticloadbalancing:DescribeLoadBalancers",
        "elasticloadbalancing:DescribeLoadBalancerAttributes",
        "elasticloadbalancing:DetachLoadBalancerFromSubnets",
        "elasticloadbalancing:DeregisterInstancesFromLoadBalancer",
        "elasticloadbalancing:ModifyLoadBalancerAttributes",
        "elasticloadbalancing:RegisterInstancesWithLoadBalancer",
        "elasticloadbalancing:SetLoadBalancerPoliciesForBackendServer"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Sid": "kopsMasterCertIAMPerms",
      "Effect": "Allow",
      "Action": [
        "iam:ListServerCertificates",
        "iam:GetServerCertificate"
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
      "Sid": "kopsK8sS3MasterBucketFullGet",
      "Effect": "Allow",
      "Action": [
        "s3:Get*"
      ],
      "Resource": "arn:aws:s3:::${s3_bucket_name}/*"
    },
    {
      "Sid": "kopsK8sRoute53Change",
      "Effect": "Allow",
      "Action": [
        "route53:ChangeResourceRecordSets",
        "route53:ListResourceRecordSets",
        "route53:GetHostedZone"
      ],
      "Resource": [
        "arn:aws:route53:::hostedzone/${aws_hosted_zone_id}"
      ]
    },
    {
      "Sid": "kopsK8sRoute53GetChanges",
      "Effect": "Allow",
      "Action": [
        "route53:GetChange"
      ],
      "Resource": [
        "arn:aws:route53:::change/*"
      ]
    },
    {
      "Sid": "kopsK8sRoute53ListZones",
      "Effect": "Allow",
      "Action": [
        "route53:ListHostedZones"
      ],
      "Resource": [
        "*"
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