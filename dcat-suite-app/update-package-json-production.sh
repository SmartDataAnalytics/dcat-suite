#!/bin/bash

# Delete existing files (will be recreated)
rm -f package.json package-lock.json

mvn -Pproduction vaadin:prepare-frontend

# The following command should produce the package-lock.json file and eventually fail
mvn -Pproduction vaadin:build-frontend || true

# Run npm ourselves ...
npm i --legacy-peer-deps

# ... and apply the fix
sed -i 's|"leaflet": "~1.3.1"|"leaflet": "1.6.0"|g' package-lock.json

# Now the build should work
mvn -Pproduction vaadin:build-frontend

