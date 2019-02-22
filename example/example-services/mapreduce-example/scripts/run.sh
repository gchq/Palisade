#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

FILE=$2
OUT_PATH=$1

if [[ $# -lt 1 ]]
then
    OUT_PATH='/tmp/palisade-mapreduce-example/output'
fi

if [[ $# -lt 2 ]]
then
    FILE="$DIR/../../resources/exampleObj_file1.txt"
fi

java -cp example/mapreduce-example/mapreduce-example-runner/target/mapreduce-example-runner-*-shaded.jar uk.gov.gchq.palisade.example.MapReduceExample "$FILE" "$OUT_PATH"

if [[ $? -eq 0 ]]
then
    echo -e "\nYou can find the results in directory \"$OUT_PATH\""
    echo -e "\nThe results are:\n"
    cat ${OUT_PATH}/part-r-00000
else
    echo -e "\nSomething went wrong\n"
fi
