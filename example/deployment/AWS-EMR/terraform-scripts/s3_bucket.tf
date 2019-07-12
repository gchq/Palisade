resource "aws_s3_bucket" "s3_bucket" {
  bucket = "${var.bucket_name}"
  force_destroy = true
  lifecycle {
    prevent_destroy = false
  }
}
