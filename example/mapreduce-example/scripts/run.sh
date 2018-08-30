#!/usr/bin/env bash

OUT_PATH=$1

if [[ $# -lt 1 ]]
then
    OUT_PATH='/tmp/palisade-mapreduce-example/output'
fi

java -cp example/mapreduce-example/mapreduce-example-runner/target/mapreduce-example-runner-*-shaded.jar uk.gov.gchq.palisade.example.MapReduceExample "$OUT_PATH"

if [[ $? -eq 0 ]]
then
    echo -e "\nYou can find the results in directory \"$OUT_PATH\""
    echo -e "\nThe results are:\n"
    cat ${OUT_PATH}/part-r-00000
else
    echo -e "\nSomething went wrong\n"
fi
