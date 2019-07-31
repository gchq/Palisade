# Palisade DefaultRole

resource "aws_iam_role" "EMR_DefaultRole_Palisade" {
  name               = "EMR_DefaultRole_Palisade"
  description        = "Default EMR Role for Palisade"
  assume_role_policy = "${data.aws_iam_policy_document.emr-assume-role-policy.json}"
}

data "aws_iam_policy" "AmazonElasticMapReduceRole" {
  arn = "arn:aws:iam::aws:policy/service-role/AmazonElasticMapReduceRole"
}

resource "aws_iam_role_policy_attachment" "EMR_DefaultRole_Palisade-attach" {
  role       = "${aws_iam_role.EMR_DefaultRole_Palisade.name}"
  policy_arn = "${data.aws_iam_policy.AmazonElasticMapReduceRole.arn}"
}

data "aws_iam_policy_document" "emr-assume-role-policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["elasticmapreduce.amazonaws.com"]
    }
  }
}

# Palisade EC2_DefaultRole

data "aws_iam_policy" "AmazonElasticMapReduceforEC2Role" {
  arn = "arn:aws:iam::aws:policy/service-role/AmazonElasticMapReduceforEC2Role"
}

resource "aws_iam_role_policy_attachment" "EMR_EC2_DefaultRole_Palisade-attach" {
  role       = "${aws_iam_role.EMR_EC2_DefaultRole_Palisade.name}"
  policy_arn = "${data.aws_iam_policy.AmazonElasticMapReduceforEC2Role.arn}"
}

data "aws_iam_policy_document" "emr-ec2-assume-role-policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "EMR_EC2_DefaultRole_Palisade" {
  name               = "EMR_EC2_DefaultRole_Palisade"
  description        = "Default EMR EC2 Role for Palisade"
  assume_role_policy = "${data.aws_iam_policy_document.emr-ec2-assume-role-policy.json}"
}
