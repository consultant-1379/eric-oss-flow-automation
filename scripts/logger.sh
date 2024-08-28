#!/bin/bash

VERSION="1.0.0"

function generate_log_text()
{
  if [ "${3}" == "indirect" ]
  then
    echo "{\"timestamp\":\"$(date -u +"%Y-%m-%dT%H:%M:%S.%3NZ")\",\"version\":\"${VERSION}\",\"message\":\"${2}\",\"service_id\":\"${SERVICE_ID}\",\"severity\":\"${1}\"}"
  elif [ "${3}" == "direct" ]
  then
    echo "{\"timestamp\":\"$(date -u +"%Y-%m-%dT%H:%M:%S.%3NZ")\",\"version\":\"${VERSION}\",\"message\":\"${2}\",\"metadata\":{\"pod_name\":\"${POD_NAME}\",\"pod_uid\":\"${POD_UID}\",\"container_name\":\"${CONTAINER_NAME}\",\"node_name\":\"${NODE_NAME}\",\"namespace\":\"${NAMESPACE}\"},\"service_id\":\"${SERVICE_ID}\",\"severity\":\"${1}\"}"
  else
    echo "Unsupported streaming method"
  fi
}

function stream_log()
{
  curl -X POST -H "Content-Type: application/json" -d "${1}" http://${LOGSTASH_DESTINATION}:${LOGSTASH_PORT}
}

function log()
{
  if [ "${STREAMING_METHOD}" == "indirect" ]
  then
    log_text=$(generate_log_text "${1}" "${2}" "${STREAMING_METHOD}")
    echo $log_text
  elif [ "${STREAMING_METHOD}" == "direct" ]
  then
    log_text=$(generate_log_text "${1}" "${2}" "${STREAMING_METHOD}")
    stream_log "${log_text}"
  elif [ "${STREAMING_METHOD}" == "dual" ]
  then
    log_text=$(generate_log_text "${1}" "${2}" "indirect")
    echo $log_text
    log_text=$(generate_log_text "${1}" "${2}" "direct")
    stream_log "${log_text}"
  else
    echo "Unsupported streaming method"
  fi
}

function log_debug()
{
  log "debug" "${1}"
}

function log_warn()
{
  log "warn" "${1}"
}

function log_info()
{
  log "info" "${1}"
}

function log_error()
{
  log "error" "${1}"
}

function log_critical()
{
  log "critical" "${1}"
}