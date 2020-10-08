: # Copyright 2020 Crown Copyright
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
./mvnw --version

: # DOS does a GOTO, Unix does a skip
:<<"::CMDLITERAL"
@ECHO OFF
GOTO :CMDSCRIPT
::CMDLITERAL

: ###############
: # Unix Script #
: ###############

: # For each palisade repo
for dir in Palisade-common Palisade-readers Palisade-clients Palisade-services Palisade-examples
do
    # Clone it (0.4.0 release)
    git clone --depth 1 --branch palisade-0.4.0 https://github.com/gchq/$dir.git
    cd $dir
    # Install it (skip dockerfile, tests, javadoc, etc.)
    ../mvnw install -Pquick
    cd ..
done

: # Run the REST example
cd Palisade-services
java -Dspring.profiles.active=discovery -Dmanager.mode=run -jar services-manager/target/services-manager-*-exec.jar
java -Dspring.profiles.active=example-model -Dmanager.mode=run -jar services-manager/target/services-manager-*-exec.jar

: # DOS GOTO jumps to here, so exit just before then
exit $?
:CMDSCRIPT

: ##############
: # DOS Script #
: ##############

: # For each palisade repo
FOR %%DIR IN (Palisade-common, Palisade-readers, Palisade-clients, Palisade-services, Palisade-examples) DO (
    REM clone it
    git clone --depth 1 --branch palisade-0.4.0 https://github.com/gchq/%%DIR.git
    cd $dir
    REM install it
    ../mvnw.cmd install -Pquick
    cd ..
)

: # Run the REST example
cd Palisade-services
java -D"spring.profiles.active=discovery" -D"manager.mode=run" -jar services-manager/target/services-manager-*-exec.jar
java -D"spring.profiles.active=example-model" -D"manager.mode=run" -jar services-manager/target/services-manager-*-exec.jar
