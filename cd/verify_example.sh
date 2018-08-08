#!/usr/bin/env bash

key="ExampleObj"
example_expected_results=(
     'ExampleObj[property=item1c,visibility=public,timestamp=20]'
     'ExampleObj[property=item1d,visibility=private,timestamp=20]'
     'ExampleObj[property=item2c,visibility=public,timestamp=20]'
     'ExampleObj[property=item2d,visibility=private,timestamp=20]'
     'ExampleObj[property=item2d,visibility=private,timestamp=20]'
     'ExampleObj[property=item2c,visibility=public,timestamp=20]'
   )

validate_example_output() {
   echo "Validating example output"
   echo $1

   counter=0;
   for line in $1
   do
     if [[ $line = *"$key"* ]]; then
       if [[ ${example_expected_results[$line]} ]]; then ((counter++)); fi
     fi
   done

   if [ "$counter" -eq "${#example_expected_results[@]}" ]; then
      return 0
   fi

   echo "Counter $counter"
   return 1
}
