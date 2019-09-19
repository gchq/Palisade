resource "aws_emr_cluster" "palisade_cluster" {
  name          = "${var.cluster_name}"
  release_label = "${var.release_label}"
  applications  = "${var.apps_to_install}"
  service_role  = "${aws_iam_role.EMR_DefaultRole_Palisade.id}"
  log_uri       = "s3://${aws_s3_bucket.s3_bucket.bucket}/logs/"

  tags {
    name = "Palisade Example EMR cluster"
    Owner = "${var.owner}"
    Project = "${var.project}"
  }

  ec2_attributes {
    instance_profile                  = "${aws_iam_instance_profile.emr_profile.id}"
    key_name                          = "${var.key_name}"
    subnet_id                         = "${var.subnet_id}"
    emr_managed_master_security_group = "${aws_security_group.palisade_allow_inbound.id}"
    emr_managed_slave_security_group  = "${aws_security_group.palisade_allow_inbound.id}"
  }

  instance_group {
    instance_role  = "MASTER"
    instance_type  = "${var.master_instance_type}"
    instance_count = "${var.master_instance_count}"
    ebs_config {
      size = 50
      type = "gp2"
    }
  }

  instance_group {
    instance_role  = "CORE"
    instance_type  = "${var.core_instance_type}"
    instance_count = "${var.core_instance_count}"
  }

  bootstrap_action {
    path = "s3://elasticmapreduce/bootstrap-actions/run-if"
    name = "runif"
    args = ["instance.isMaster=true", "echo running on master node"]
  }

}

resource "aws_iam_instance_profile" "emr_profile" {
  name = "emr_profile2"
  role = "${aws_iam_role.EMR_EC2_DefaultRole_Palisade.id}"
}
