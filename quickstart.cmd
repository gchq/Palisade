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

: # This script should be valid for both UNIX and DOS environments,
: # thus the lack of any 'fancy' bash or powershell syntax, weird comments
: # and use of string escapes for dotted command properties.

git --version
java --version
mvn --version

: # Download each of the repos
git clone --depth 1 --branch palisade-0.4.0 https://github.com/gchq/Palisade-common.git
git clone --depth 1 --branch palisade-0.4.0 https://github.com/gchq/Palisade-readers.git
git clone --depth 1 --branch palisade-0.4.0 https://github.com/gchq/Palisade-clients.git
git clone --depth 1 --branch palisade-0.4.0 https://github.com/gchq/Palisade-services.git
git clone --depth 1 --branch palisade-0.4.0 https://github.com/gchq/Palisade-examples.git

: # Install each project in order of any dependencies
cd Palisade-common
mvn install -Pquick
cd ../Palisade-readers
mvn install -Pquick
cd ../Palisade-clients
mvn install -Pquick
cd ../Palisade-services
mvn install -Pquick
cd ..

: # Run the JVM example using the cross-platform services-manager
cd Palisade-services
java -D"spring.profiles.active=discovery" -D"manager.mode=run" -jar services-manager/target/services-manager-*-exec.jar
java -D"spring.profiles.active=example-model" -D"manager.mode=run" -jar services-manager/target/services-manager-*-exec.jar

