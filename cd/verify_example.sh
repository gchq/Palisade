#!/usr/bin/env bash

key1="Nickolas Ryan"
key2="Reece Cummings"
key1flag=1
key2flag=1

validate_example_output() {
   echo "Validating example output"
   echo $1

   for line in $1
   do
     if [[ $line = *"$key1"* ]]; then
        key1flag=0
     fi
     if [[ $line = *"$key2"* ]]; then
        key2flag=0
     fi
   done
   if [[ $key1flag = 0 ]] && [[ $key2flag = 0 ]]; then
        return 0
   fi

   return 1
}
