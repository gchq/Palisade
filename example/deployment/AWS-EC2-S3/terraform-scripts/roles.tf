# Palisade DefaultRole

#resource "aws_iam_role" "EC2_DefaultRole_Palisade" {
#  name               = "EC2_DefaultRole_Palisade"
#  description        = "Default EC2 Role for Palisade"
#  assume_role_policy = "${data.aws_iam_policy_document.emr-assume-role-policy.json}"
#}
