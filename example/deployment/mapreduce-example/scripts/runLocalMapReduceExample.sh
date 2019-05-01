#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
#/home/gmiller/repos/Palisade/example/deployment/mapreduce-example/scripts

INPUTFILE=$2
OUT_PATH=$1

if [[ $# -lt 1 ]]
then
    OUT_PATH='/var/tmp/palisade-mapreduce-example/output'
fi

if [[ $# -lt 2 ]]
then
    INPUTFILE="$DIR/../../../resources/Employee_file0.avro"
fi

java -cp example/deployment/mapreduce-example/mapreduce-example-runner/target/mapreduce-example-runner-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.MapReduceExample "$INPUTFILE" "$OUT_PATH"

if [[ $? -eq 0 ]]
then
    echo -e "\nYou can find the results in directory \"$OUT_PATH\""
    echo -e "\nThe results are:\n"
    cat ${OUT_PATH}/part-r-00000
else
    echo -e "\nSomething went wrong\n"
fi
