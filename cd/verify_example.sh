#!/usr/bin/env bash

key1="Nickolas Ryan"
key1flag=1
IFS=$'\n'

validate_example_output() {
   echo "Validating example output"

   for line in $1
   do
     if [[ $line = *"$key1"* ]]; then
        key1flag=0
     fi
   done
   if [[ $key1flag -eq 0 ]]; then
        return 0
   fi

   return 1
}