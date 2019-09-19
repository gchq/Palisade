#!/usr/bin/env sh
#
#
# Copyright 2019 Crown Copyright
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


if [ $# -eq 1 ]; then

	# if `docker run` only has one arguments, we assume user is running alternate command like `bash` to inspect the image
    aws ecr create-repository --region eu-west-1 --repository-name $1

else
    echo "Usage: create-repository <<repository-name>>"
fi