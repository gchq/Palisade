# Create an IAM role for the EC2 Server
resource "aws_iam_role" "palisade_iam_role" {
    name = "palisade_iam_role"
    assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_iam_instance_profile" "palisade_instance_profile" {
    name = "palisade_instance_profile"
    role = "palisade_iam_role"
    }

resource "aws_iam_role_policy" "palisade_iam_role_policy" {
  name = "palisade_iam_role_policy"
  role = "${aws_iam_role.palisade_iam_role.id}"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:ListBucket"],
      "Resource": ["arn:aws:s3:::developer6959palisadeec2test"]
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject"
      ],
      "Resource": ["arn:aws:s3:::developer6959palisadeec2test/*"]
    }
  ]
}
EOF
}

