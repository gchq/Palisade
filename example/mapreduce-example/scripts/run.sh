#!/usr/bin/env bash

java -cp example/mapreduce-example/mapreduce-example-runner/target/mapreduce-example-runner-*-shaded.jar uk.gov.gchq.palisade.example.MapReduceExample $@

echo -e "\nLook for results in directory \"$1\""
