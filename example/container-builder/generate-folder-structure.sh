#!/usr/bin/env bash

if [ "$1" == "clean" ]
   then
      echo "Cleaning bin directories of services"
      rm -rf ./data-service/bin/
      rm -rf ./palisade-service/bin/
      rm -rf ./policy-service/bin/
      rm -rf ./resource-service/bin/
      rm -rf ./user-service/bin/
      rm -rf ./config-service/bin/
      rm -rf ./rest-redirector/bin/
fi

echo "Creating bin directories of services"

mkdir -p ./data-service/bin/rest-data-service
mkdir -p ./palisade-service/bin/rest-palisade-service
mkdir -p ./policy-service/bin/rest-policy-service
mkdir -p ./resource-service/bin/rest-resource-service
mkdir -p ./user-service/bin/rest-user-service
mkdir -p ./config-service/bin/rest-config-service
mkdir -p ./rest-redirector/bin/rest-redirector
