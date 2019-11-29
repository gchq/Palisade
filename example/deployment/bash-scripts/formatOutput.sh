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
sed $'s/,nationality=/\\\nnationality=/g'| \
sed $'s/,manager=/\\\nmanager=/'| \
sed $'s/,hireDate=/\\\nhireDate=/g'| \
sed $'s/,grade=/\\\ngrade=/g'| \
sed $'s/,department=/\\\ndepartment=/g'| \
sed $'s/,salaryAmount=/\\\nsalaryAmount=/g'| \
sed $'s/,salaryBonus=/\\\nsalaryBonus=/g'| \
sed $'s/,workLocation=/\\\nworkLocation=/g'| \
sed $'s/,sex=/\\\nsex=/g'