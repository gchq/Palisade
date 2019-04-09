#!/usr/bin/env bash

sed $'s/Alice \[/\\\n\\\nAlice \[/g'| \
sed $'s/Bob \[/\\\n\\\nBob \[/g' | \
sed $'s/Eve \[/\\\n\\\nEve \[/g' | \
sed $'s/,name=/\\\n\\\nname=/g'| \
sed $'s/,dateOfBirth=/\\\ndateOfBirth=/g'| \
sed $'s/,contactNumbers=/\\\ncontactNumbers=/g'| \
sed $'s/,emergencyContacts=/\\\nemergencyContacts=/g'| \
sed $'s/,address=/\\\naddress=/g'| \
sed $'s/,bankDetails=/\\\nbankDetails=/g'| \
sed $'s/,taxCode=/\\\ntaxCode=/g'| \
sed $'s/,nationality=/\\\nnationality=/g'
