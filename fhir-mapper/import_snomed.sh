#!/bin/bash

# Define the database name
DB_NAME="fhir_mapper"

# Import JSON files into their respective collections
mongoimport --host mongodb --db $DB_NAME --collection "snomed-condition-procedures" --file /docker-entrypoint-initdb.d/snomed/snomed-condition-procedures.json --jsonArray
mongoimport --host mongodb --db $DB_NAME --collection "snomed-diagnostics" --file /docker-entrypoint-initdb.d/snomed/snomed-diagnostics.json --jsonArray
mongoimport --host mongodb --db $DB_NAME --collection "snomed-encounters" --file /docker-entrypoint-initdb.d/snomed/snomed-encounters.json --jsonArray
mongoimport --host mongodb --db $DB_NAME --collection "snomed-medicines" --file /docker-entrypoint-initdb.d/snomed/snomed-medicines.json --jsonArray
mongoimport --host mongodb --db $DB_NAME --collection "snomed-medicine-routes" --file /docker-entrypoint-initdb.d/snomed/snomed-medicine-routes.json --jsonArray
mongoimport --host mongodb --db $DB_NAME --collection "snomed-observations" --file /docker-entrypoint-initdb.d/snomed/snomed-observations.json --jsonArray
mongoimport --host mongodb --db $DB_NAME --collection "snomed-specimens" --file /docker-entrypoint-initdb.d/snomed/snomed-specimens.json --jsonArray
mongoimport --host mongodb --db $DB_NAME --collection "snomed-vaccines" --file /docker-entrypoint-initdb.d/snomed/snomed-vaccines.json --jsonArray
# Add more lines as needed for additional files and collections

echo "Data import completed."