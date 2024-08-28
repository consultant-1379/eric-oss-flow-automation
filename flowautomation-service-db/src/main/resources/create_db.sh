#!/bin/sh
source $(dirname "$0")/logger.sh

status=false
SPENT_TIME=0
SLEEP_TIMEOUT=1
message=`psql -h $POSTGRES_HOST -U $POSTGRES_USER -w -p $POSTGRES_PORT -d postgres -c "CREATE DATABASE $POSTGRES_DB" 2>&1`
ALREADY_EXISTS_ERROR_MESSAGE="ERROR:  database \"$POSTGRES_DB\" already exists"
log_info $message
while true; do
  if [[ "${#message}" = "${#ALREADY_EXISTS_ERROR_MESSAGE}" ]]
     then
        log_info "$POSTGRES_DB already present. Skipping creation of $POSTGRES_DB ..."
        exit 0;
  elif [[ "${message}" = "CREATE DATABASE" ]]
    then
        log_info "Successfully added $POSTGRES_DB database."
        exit 0;
  else
        log_error "Failed to create database \"$POSTGRES_DB\". Retrying...."
        sleep ${SLEEP_TIMEOUT}
        SPENT_TIME=$(($SPENT_TIME + $SLEEP_TIMEOUT))
        message=`psql -h $POSTGRES_HOST -U $POSTGRES_USER -w -p $POSTGRES_PORT -d postgres -c "CREATE DATABASE $POSTGRES_DB" 2>&1`
  fi
  if [[ "$SPENT_TIME" -ge "$STARTUP_WAIT" ]];
        then
            log_error "ERROR: Timeout limit reached"
            exit 1
  fi
done