#!/usr/bin/env bash

key="ExampleObj"

validate_example_output() {
   echo "Validating example output"
   echo $1

   for line in $1
   do
     if [[ $line = *"$key"* ]]; then
        return 0
     fi
   done

   return 1
}
