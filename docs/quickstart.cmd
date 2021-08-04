: # Copyright 2018-2021 Crown Copyright
: #
: # Licensed under the Apache License, Version 2.0 (the "License");
: # you may not use this file except in compliance with the License.
: # You may obtain a copy of the License at
: #
: #     http://www.apache.org/licenses/LICENSE-2.0
: #
: # Unless required by applicable law or agreed to in writing, software
: # distributed under the License is distributed on an "AS IS" BASIS,
: # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
: # See the License for the specific language governing permissions and
: # limitations under the License.

: # This script should be valid for both UNIX and DOS environments

: # Print out the versions for the required tools
git --version
java --version

: # DOS does a GOTO, Unix does a skip
:<<"::CMDLITERAL"
@ECHO OFF
GOTO :CMDSCRIPT
::CMDLITERAL

: ###############
: # Unix Script #
: ###############

: # For each palisade repo
for dir in Palisade-services Palisade-examples
do
    # Clone it (0.5.1 release)
    git clone --depth 1 --branch palisade-0.5.1 https://github.com/gchq/$dir.git
done

: # Run the K8s example
cd Palisade-examples
bash deployment-k8s/local-k8s/example-runner/deployServicesToK8s.sh
bash deployment-k8s/local-k8s/example-runner/runFormattedK8sExample.sh
bash deployment-k8s/local-k8s/example-runner/verify.sh
cd ..

: # DOS GOTO jumps to here, so exit just before then
exit $?
:CMDSCRIPT

: ##############
: # DOS Script #
: ##############

setlocal EnableDelayedExpansion
set "dir_list=Palisade-services Palisade-examples"

: # For each palisade repo
FOR %%s IN (%dir_list%) DO (
    set "url=https://github.com/gchq/%%s.git"
    REM clone it
    git clone --depth 1 --branch palisade-0.5.1 "!url!"
)

: # Run the K8s example
cd Palisade-examples
bash deployment-k8s/local-k8s/example-runner/deployServicesToK8s.sh
bash deployment-k8s/local-k8s/example-runner/runFormattedK8sExample.sh
bash deployment-k8s/local-k8s/example-runner/verify.sh
cd ..
