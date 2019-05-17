#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

INPUTFILE=$1
OUT_PATH=$2

if [[ $# -lt 1 ]]
then
    OUT_PATH='/var/tmp/palisade-awsemrmapreduce-example/output'
fi

if [[ $# -lt 2 ]]
then
    INPUTFILE="$DIR/../../../resources/employee_file0.avro"
fi

java -cp example/deployment/AWS-EMR/example-aws-emr-runner/target/example-aws-emr-runner-*-shaded.jar -Dpalisade.rest.config.path=configRest.json uk.gov.gchq.palisade.example.AwsEmrMapReduceExample "$INPUTFILE" "$OUT_PATH"

if [[ $? -eq 0 ]]
then
    echo -e "\nYou can find the results in directory \"$OUT_PATH\""
    echo -e "\nThe results are:\n"
    cat ${OUT_PATH}/part-r-00000
else
    echo -e "\nSomething went wrong\n"
fi