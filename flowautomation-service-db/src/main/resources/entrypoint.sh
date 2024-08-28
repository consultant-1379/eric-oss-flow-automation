#!/bin/bash
#
# COPYRIGHT Ericsson 2022
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

# Create Database in postgres DB, DB creation will be skipped for docker-compose env
if [[ -z "${SKIP_DB_CREATION}" ]]; then
  export POSTGRES_USER=$DB_USERNAME
  export PGPASSWORD=$DB_PASSWORD
  /home/create_db.sh
  retVal=$?
  if [ $retVal -ne 0 ]; then
    exit 1
  fi
fi

# Create/Update Schema in DB
java -jar /home/flowautomation-service-db-jar.jar
