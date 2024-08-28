# Flow Automation Developers Guide

[TOC]

## Introduction

This guide contains information on the API for Flow Automation (FA).

Flow Automation is a generic workflow-based framework and application that allows the Flows to be designed and executed. These flows are meant, among other things, to be the orchestrator of the different day to day tasks of the operator to automate them and reduce the time of doing them. Flow Automation allows the user to remove manual steps and instead automate the tasks by using flows.

Flow Automation provides a suite of reusable components that makes the automation design easy and efficient. There are multiple types of reusable components, such as Flow Java Libraries. The basic Flow Java Libraries are used to implement low-level logic with Business Process Model and Notation (BPMN) script tasks. Flow User Interaction allows flows to interact with users.

The Flow Automation application provides the runtime for flow execution. REST and GUI are the interfaces supported, and allow deployment, execution, and monitoring of flows.

The purpose of this document is to describe the Flow Automation REST API. The REST endpoints are supported for the deployment, execution, and the administration of flows. For more information in regard to the parameters of the endpoints, please refer to the [API Documentation](https://adp.ericsson.se/marketplace/flow-automation/documentation/development/dpi/api-documentation).

## Concepts
### Flow
A Flow is a sequence of steps and operations used to automate a use case. A Flow is defined by a collection of BPMN processes organized in a predefined folder structure and packaged as a zip file.

### Flow Execution
A flow execution is a running instance of a flow. There can be multiple flow executions of the same flow running at the same time.

## Flow Endpoints
### /v1/flows/ (POST)
This endpoint allows the user to import a flow. This interface can only import a flow in a flow package zip file. Other formats of input return an error and the import is rejected.
```
curl -i -X POST http://localhost:8080/flow-automation/v1/flows/ --form flow-package=@sdk-flow-example.zip --header "UserID: saga"

HTTP/2 200 OK
content-type: application/json

{
  "id":"com.ericsson.oss.fa.flows.sdk-flow-example",
  "name":"SDK Flow Example",
  "status":"enabled",
  "source":"EXTERNAL",
  "flowVersions":[
    {
      "version":"1.0.0",
      "description":"SDK Flow Example",
      "active":true,
      "createdBy":"saga",
      "createdDate":"2022-04-11 13:58:38.0",
      "setupPhaseRequired": true,
      "reportSupported": true
    }
  ]
}
```
The curl command uses the zip file that is stored in the current working directory. This is the same with other curl commands that works with files in this guide.

If a file is missing within the zip file, the server returns an error with a description of the missing file.

### /v1/flows/ (GET)
This endpoint returns information on all imported flows as shown in the curl command example below.
```
curl -i http://localhost:8080/flow-automation/v1/flows

HTTP/2 200 OK
content-type: application/json

[
  {
    "id": "com.ericsson.oss.fa.flows.sdk-flow-example",
    "name": "SDK Flow Example",
    "status": "enabled",
    "source": "EXTERNAL",
    "flowVersions": [
      {
        "version": "1.0.0",
        "description": "SDK Flow Example",
        "active": true,
        "createdBy": "saga",
        "createdDate": "2022-04-05 14:42:24.0",
        "setupPhaseRequired": true,
        "reportSupported": true
      }
    ]
  },
  {
    "id": "com.ericsson.oss.fa.flows.backEnabled",
    "name": "Flow with back enabled",
    "status": "enabled",
    "source": "EXTERNAL",
    "flowVersions": [
      {
        "version": "1.0.0",
        "description": "Basic flow that shows the back enabled support",
        "active": false,
        "createdBy": "saga",
        "createdDate": "2022-04-07 16:42:23.0",
        "setupPhaseRequired": true,
        "reportSupported": false
      },
      {
        "version": "1.0.1",
        "description": "Basic flow that shows the back enabled support",
        "active": false,
        "createdBy": "saga",
        "createdDate": "2022-04-11 09:26:25.0",
        "setupPhaseRequired": true,
        "reportSupported": false
      },
      {
        "version": "1.0.2",
        "description": "Basic flow that shows the back enabled support",
        "active": true,
        "createdBy": "saga",
        "createdDate": "2022-04-11 09:28:19.0",
        "setupPhaseRequired": true,
        "reportSupported": false
      }
    ]
  },
  {
    "id": "com.ericsson.oss.fa.flows.multiInstance.download-variables",
    "name": "Download Report variable flow",
    "status": "enabled",
    "source": "EXTERNAL",
    "flowVersions": [
      {
        "version": "1.0.5",
        "description": "Multi-Instance Flow with links to download variable",
        "active": true,
        "createdBy": "saga",
        "createdDate": "2022-04-08 12:17:20.0",
        "setupPhaseRequired": true,
        "reportSupported": true
      }
    ]
  }
]
```

### /v1/flows/{flow-id} (GET)
This endpoint returns information regarding the specific flow as shown in the example below.
```
curl -i http://localhost:8080/flow-automation/v1/flows/com.ericsson.oss.fa.flows.sdk-flow-example

HTTP/2 200 OK
content-type: application/json

{
  "id": "com.ericsson.oss.fa.flows.sdk-flow-example",
  "name": "SDK Flow Example",
  "status": "enabled",
  "source": "EXTERNAL",
  "flowVersions": [
    {
      "version": "1.0.0",
      "description": "SDK Flow Example",
      "active": true,
      "createdBy": "saga",
      "createdDate": "2022-04-05 14:42:24.0",
      "setupPhaseRequired": true,
      "reportSupported": true
    }
  ]
}
```

### /v1/flows/{flow-id} (DELETE)
This endpoint deletes the specific flow as shown in the curl command below.
```
curl -i -X DELETE http://localhost:8080/flow-automation/v1/flows/com.ericsson.oss.fa.flows.sdkBasicFlowExample?force=true

HTTP/2 204 NO CONTENT
```
The force parameter must be true for the delete to be performed. It is to avoid accidental deletion.

### /v1/flows/{flow-id}/activate (PUT)
This endpoint activates/deactivates a specific flow based on its flow version shown in the curl command example below.
```
curl -i -X PUT -d '{"value" : {boolean}}' -H "Content-Type: application/json" http://localhost:8080/flow-automation/v1/flows/com.ericsson.oss.fa.flows.sdk-flow-example/activate?flow-version=1.0.1

HTTP/2 204 NO CONTENT
```
Replace {boolean} with true to activate and false to deactivate the flow version.

Activating a version of the flow deactivates the current active flow automatically.

Only one flow version can be active at a given moment and a flow must have an active flow version, or else, it would cause the GUI to not be able to view any flow executions in the catalog.

### /v1/flows/{flow-id}/enable (PUT)
This endpoint enables/disables a specific flow based on the flow id provided as shown in the curl command example below.
```
curl -i -X PUT -d '{"value" : {boolean}}' -H "Content-Type: application/json" http://localhost:8080/flow-automation/v1/flows/com.ericsson.oss.fa.flows.sdk-flow-example/enable

HTTP/2 204 NO CONTENT
```
Replace {boolean}, true to enable and false to disable the flow.

### /v1/flows/{flow-id}/execute (POST)
This endpoint starts an execution of the flow as shown in the curl command example below. The name of the execution should be returned.
```
curl -i -X POST http://localhost:8080/flow-automation/v1/flows/com.ericsson.oss.fa.flows.sdk-flow-example/execute --form "name=SFE_123456" --header "UserID: saga"

HTTP/2 200 OK
content-type: application/json

{
  "name":"SFE_123456"
}
```


### /v1/flows/{flow-id}/resource/flow-input-schema (GET)
This endpoint returns the flow input schema for the specific flow as shown in the curl command example below.
```
curl -i http://localhost:8080/flow-automation/v1/flows/com.ericsson.oss.fa.flows.sdk-flow-example/resource/flow-input-schema?flow-version=1.0.0

HTTP/2 200 OK
content-type: application/json

{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "description": "SDK Flow Example",
  "type": "object",
  "properties": {
    "nodeSelection": {
      "name": "Nodes Selection",
      "type": "object",
      "properties": {
        "nodes": {
          "name": "Radio Nodes",
          "type": "string",
          "format": "select-list",
          "schemaGen": {
            "type": "enum",
            "binding": "variable:local:nodesList"
          }
        }
      },
      "required": [
        "nodes"
      ],
      "additionalProperties": false
    },
    "softwarePackageSelection": {
      "name": "Software Package Selection",
      "type": "object",
      "properties": {
        "softwarePackages": {
          "name": "Software Packages",
          "type": "string",
          "format": "select-list",
          "schemaGen": {
            "type": "enum",
            "binding": "variable:local:softwarePackagesList"
          }
        }
      },
      "required": [
        "softwarePackages"
      ],
      "additionalProperties": false
    }
  },
  "additionalProperties": false,
  "required": [
    "nodeSelection",
    "softwarePackageSelection"
  ]
}
```
The flow-version is an optional parameter, if the flow-version is not provided, the flow-input-schema of the active flow version is returned.

## Flow Execution Endpoints
### /v1/executions (GET)
This endpoint returns all flow executions as shown in the curl command example below.
```
curl -i 'http://localhost:8080/flow-automation/v1/executions?flow-id=com.ericsson.oss.fa.flows.sdk-flow-example&flow-execution-name=SFE_123456&flow-version=1.0.0&filter-by-user=true' --header "UserID: saga"

HTTP/2 200 OK
content-type: application/json

[
  {
    "name": "SFE_123456",
    "flowId": "com.ericsson.oss.fa.flows.sdk-flow-example",
    "flowName": "SDK Flow Example",
    "flowVersion": "1.0.0",
    "createdBy": null,
    "executedBy": "saga",
    "startTime": "2022-04-05 14:54:15",
    "endTime": null,
    "duration": null,
    "state": "SETTING_UP",
    "numberActiveUserTasks": 1,
    "flowSource": "EXTERNAL",
    "summaryReport": null,
    "processInstanceId": "43716c1f-b4f0-11ec-ab80-1aed4ab4da3b",
    "processInstanceBusinessKey": "com.ericsson.oss.fa.flows.sdk-flow-example.-.SFE_123456.-.322"
  }
]
```
Query parameters can be provided to filter the query. If no parameters are entered, all flow executions are returned.

For more information regarding the parameters please refer it in the [API Documentation](https://adp.ericsson.se/marketplace/flow-automation/documentation/development/dpi/api-documentation).

### /v1/executions/{flow-execution-name} (DELETE)
This endpoint deletes a flow execution that shares the name given as shown in the curl command example below
```
curl -i -X DELETE http://localhost:8080/flow-automation/v1/executions/SFE_123456?flow-id=com.ericsson.oss.fa.flows.sdk-flow-example

HTTP/2 204 NO CONTENT
```

### /v1/executions/{flow-execution-name}/usertasks (GET)
This endpoint retrieves the user tasks related to the specific flow execution as show in the curl command example below.
```
curl -i http://localhost:8080/flow-automation/v1/executions/SFE_123456/usertasks?flow-id=com.ericsson.oss.fa.flows.sdk-flow-example

HTTP/2 200 OK
content-type: application/json

[
  {
    "id": "3821b91a-b691-11ec-9e08-ba95e43d00dd",
    "name": "Choose Setup",
    "nameExtra": "",
    "processDefinitionId": "9bbd1926-b4ee-11ec-ab80-1aed4ab4da3b",
    "taskDefinitionId": "FAInternal-optionally-get-file-based-input",
    "status": "active"
  }
]
```

### /v1/executions/{flow-execution-name}/report (GET)
This endpoint gets the report of the flow execution as shown in the curl command example below.
```
curl -i http://localhost:8080/flow-automation/v1/executions/SFE_123456/report?flow-id=com.ericsson.oss.fa.flows.sdk-flow-example

HTTP/2 200 OK
content-type: application/json

{
  "header": {
    "reportTime": "2022-04-07T11:11:26+0000",
    "flowId": "com.ericsson.oss.fa.flows.sdk-flow-example",
    "flowVersion": "1.0.0",
    "flowName": "SDK Flow Example",
    "flowExecutionName": "SFE_123456",
    "startedBy": "saga",
    "startTime": "2022-04-05T14:54:15+0000",
    "status": "SETTING_UP"
  },
  "body": {
    "nodeName": "NA",
    "softwarePackage": "NA"
  }
}
```

### /v1/executions/{flow-execution-name}/events (GET)
This endpoint gets the events performed by the flow execution as shown in the curl command example below.
```
curl -i http://localhost:8080/flow-automation/v1/executions/SFE_123456/events?flow-id=com.ericsson.oss.fa.flows.sdk-flow-example

HTTP/2 200 OK
content-type: application/json

{
  "numberOfRecords": 11,
  "records": [
    {
      "eventTime": "2022-04-11 11:35:35.183",
      "severity": "INFO",
      "target": null,
      "message": "Getting Nodes List",
      "eventData": null
    },
    {
      "eventTime": "2022-04-11 11:35:38.092",
      "severity": "INFO",
      "target": null,
      "message": "Selected Nodes Validated",
      "eventData": null
    },
    {
      "eventTime": "2022-04-11 11:35:38.116",
      "severity": "INFO",
      "target": null,
      "message": "Getting Software Packages List",
      "eventData": null
    },
    {
      "eventTime": "2022-04-11 11:35:44.334",
      "severity": "INFO",
      "target": null,
      "message": "Software Package Validated",
      "eventData": null
    },
    {
      "eventTime": "2022-04-11 11:35:47.735",
      "severity": "INFO",
      "target": null,
      "message": "PreUpgrade Node Health Check Completed",
      "eventData": null
    },
    {
      "eventTime": "2022-04-11 11:35:47.816",
      "severity": "INFO",
      "target": null,
      "message": "PreUpgrade Licence Check Completed",
      "eventData": null
    },
    {
      "eventTime": "2022-04-11 11:35:47.87",
      "severity": "INFO",
      "target": null,
      "message": "PreUpgrade Backup Completed",
      "eventData": null
    },
    {
      "eventTime": "2022-04-11 11:35:47.927",
      "severity": "INFO",
      "target": null,
      "message": "Performing Node Upgrade",
      "eventData": null
    },
    {
      "eventTime": "2022-04-11 11:35:47.995",
      "severity": "INFO",
      "target": null,
      "message": "Validating Node Upgrade",
      "eventData": null
    },
    {
      "eventTime": "2022-04-11 11:35:48.057",
      "severity": "INFO",
      "target": null,
      "message": "PostUpgrade Node Health Check Completed",
      "eventData": null
    },
    {
      "eventTime": "2022-04-11 11:35:48.114",
      "severity": "INFO",
      "target": null,
      "message": "Node Upgraded Successfully",
      "eventData": null
    }
  ]
}
```
This endpoint has multiple optional parameters to better filter the events. For more information on the parameters, [API Document Link](https://adp.ericsson.se/marketplace/flow-automation/documentation/development/dpi/api-documentation)

### /v1/executions/{flow-execution-name}/report-schema (GET)
This endpoint retrieves the report schema as shown in the curl command example below.
```
curl -i http://localhost:8080/flow-automation/v1/executions/SFE_123456/report-schema?flow-id=com.ericsson.oss.fa.flows.sdk-flow-example

HTTP/2 200 OK
content-type: application/json

{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "title": "SDK Flow Example report schema body",
  "description": "SDK Flow Example report schema body",
  "type": "object",
  "required": [
    "header",
    "body"
  ],
  "properties": {
    "header": {
      "type": "object",
      "properties": {
        "reportTime": {
          "name": "Report Time",
          "description": "Time the report was produced",
          "type": "string",
          "format": "date-time"
        },
        "flowId": {
          "name": "Flow ID",
          "description": "Identifier for the flow",
          "type": "string"
        },
        "flowVersion": {
          "name": "Flow Version",
          "description": "Version of the flow",
          "type": "string"
        },
        "flowName": {
          "name": "Flow Name",
          "description": "Name of the flow",
          "type": "string"
        },
        "flowExecutionName": {
          "name": "Flow Execution Name",
          "description": "Name of the flow execution",
          "type": "string"
        },
        "startedBy": {
          "name": "Started By",
          "description": "Name of user who started the flow execution",
          "type": "string"
        },
        "startTime": {
          "name": "Start Time",
          "description": "Time the flow execution was started",
          "type": "string",
          "format": "date-time"
        },
        "endTime": {
          "name": "End Time",
          "description": "Time the flow execution ended",
          "type": "string",
          "format": "date-time"
        },
        "status": {
          "name": "Status",
          "description": "Status of the flow execution",
          "type": "string",
          "enum": [
            "STARTED",
            "SETTING_UP",
            "CONFIRM_EXECUTE",
            "EXECUTING",
            "COMPLETED",
            "CANCELLED",
            "INCIDENT",
            "SUSPENDED",
            "STOP",
            "STOPPING",
            "STOPPED",
            "FAILED",
            "FAILED_SETUP",
            "FAILED_EXECUTE"
          ],
          "enumNames": [
            "Started",
            "Setting Up",
            "Confirm Execute",
            "Executing",
            "Completed",
            "Cancelled",
            "Incident",
            "Suspended",
            "Stop",
            "Stopping",
            "Stopped",
            "Failed",
            "Setup Failed",
            "Execute Failed"
          ]
        }
      },
      "required": [
        "reportTime",
        "flowId",
        "flowVersion",
        "flowName",
        "flowExecutionName",
        "startedBy",
        "startTime",
        "status"
      ]
    },
    "body": {
      "type": "object",
      "properties": {
        "nodeName": {
          "name": "Node Name",
          "description": "Node Name",
          "type": "string",
          "default": "NA"
        },
        "softwarePackage": {
          "name": "Software Package",
          "description": "Software Package",
          "type": "string",
          "default": "NA"
        },
        "executionReport": {
          "name": "Node Upgrade Report",
          "description": "Node Upgrade Report",
          "type": "array",
          "items": {
            "name": "Node Upgrade Activities",
            "description": "Node Upgrade Activities",
            "type": "object",
            "properties": {
              "time": {
                "name": "Time",
                "description": "Time",
                "type": "string"
              },
              "activityName": {
                "name": "Activity",
                "description": "Activity",
                "type": "string"
              },
              "result": {
                "name": "Result",
                "description": "Result",
                "type": "string",
                "enum": [
                  "Not Available",
                  "Success",
                  "Failure",
                  "Error"
                ]
              },
              "summary": {
                "name": "Summary",
                "description": "Summary",
                "type": "string",
                "default": ""
              }
            },
            "required": [
              "time",
              "activityName",
              "result",
              "summary"
            ]
          }
        }
      },
      "required": [
        "nodeName",
        "softwarePackage"
      ]
    }
  }
}
```

### /v1/executions/{flow-execution-name}/download/{resource} (GET)
This endpoint downloads resources of the flow execution as shown in the curl command example below.
```
curl http://localhost:8080/flow-automation/v1/executions/SFE_123456/download/all?flow-id=com.ericsson.oss.fa.flows.sdk-flow-example --output sdk-flow-example-download.zip

  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  5681  100  5681    0     0   4498      0  0:00:01  0:00:01 --:--:--  4498
```
The curl command above downloads report and flow input of the flow execution into a zip file named sdk-flow-example-download.

### /v1/executions/{flow-execution-name}/download/report-variable/{name} (GET)
This endpoint downloads the report variable of a flow execution as shown in the curl command below.

For the example below, multi instance example flow was used and a txt file was uploaded to it.
```
curl http://localhost:8080/flow-automation/v1/executions/DRVF_123456/download/report-variable/uploadedFileContent?flow-id=com.ericsson.oss.fa.flows.multiInstance.download-variables --output filedownload.txt

  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100    12  100    12    0     0     29      0 --:--:-- --:--:-- --:--:--    30
```
The file is downloaded in the current working directory.

### /v1/executions/{flow-execution-name}/suspend (PUT)
This endpoint suspends a flow execution as shown in the curl command example below.
```
curl -i -X PUT http://localhost:8080/flow-automation/v1/executions/SFE_123456/suspend?flow-id=com.ericsson.oss.fa.flows.sdk-flow-example

HTTP/2 204 NO CONTENT
```

### /v1/executions/{flow-execution-name}/stop (PUT)
This endpoint stops a flow execution during execution phase as shown in the curl command example below.
```
curl -i -X PUT http://localhost:8080/flow-automation/v1/executions/SFE_123456/stop?flow-id=com.ericsson.oss.fa.flows.sdk-flow-example

HTTP/2 204 NO CONTENT
```

### /v1/executions/usertasks/{task-id}/back (POST)
This endpoint reverts the execution to the previous user task when the execution is still in the setup phase and the back feature is enabled as shown in the curl command example below.
```
curl -i -X POST http://localhost:8080/flow-automation/v1/executions/usertasks/com.ericsson.oss.fa.flows.backEnabled/back

HTTP/2 204 NO CONTENT
```

### /v1/executions/usertasks/{task-id}/schema (GET)
This endpoint gets the schema of the user task as shown in the curl command example below.
```
curl -i http://localhost:8080/flow-automation/v1/executions/usertasks/3821b91a-b691-11ec-9e08-ba95e43d00dd/schema

HTTP/2 200 OK
content-type: application/json

{
  "schema": "{\n  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n  \"title\": \"Choose Setup usertask schema\",\n  \"description\": \"Choose Setup usertask schema\",\n  \"$id\": \"choose-setup-usertask-schema\",\n  \"name\": \"Setup Type\",\n  \"action\": \"Continue\",\n  \"actions\": [\n    \"Continue\"\n  ],\n  \"type\": \"object\",\n  \"properties\": {\n    \"setupType\": {\n      \"name\": \"Choose the setup type that best suits your needs\",\n      \"type\": \"object\",\n      \"format\": \"radio\",\n      \"oneOf\": [\n        {\n          \"properties\": {\n            \"interactive\": {\n              \"type\": \"boolean\",\n              \"name\": \"Interactive\",\n              \"description\": \"Input parameters through user tasks\"\n            }\n          },\n          \"required\": [\n            \"interactive\"\n          ]\n        },\n        {\n          \"properties\": {\n            \"fileInput\": {\n              \"type\": \"object\",\n              \"name\": \"File Input\",\n              \"format\": \"file\",\n              \"description\": \"Input parameters using a file\",\n              \"properties\": {\n                \"fileName\": {\n                  \"type\": \"string\"\n                }\n              },\n              \"required\": [\n                \"fileName\"\n              ]\n            }\n          },\n          \"required\": [\n            \"fileInput\"\n          ]\n        }\n      ],\n      \"default\": \"interactive\"\n    }\n  },\n  \"additionalProperties\": false,\n  \"required\": [\n    \"setupType\"\n  ]\n}",
  "id": "3821b91a-b691-11ec-9e08-ba95e43d00dd",
  "name": "Choose Setup",
  "nameExtra": null,
  "processDefinitionId": "9bbd1926-b4ee-11ec-ab80-1aed4ab4da3b",
  "taskDefinitionId": "FAInternal-optionally-get-file-based-input",
  "status": "active"
}
```

### /v1/executions/usertasks/{task-id}/complete (POST)
This endpoint completes the user task together with the form information as shown in the curl command example below. The example below requires a JSON file with the information shown below.
```
cat input.json
{"setupType":{"interactive":true}}

curl -i -X POST http://localhost:8080/flow-automation/v1/executions/usertasks/3821b91a-b691-11ec-9e08-ba95e43d00dd/complete --form usertask-input=@input.json

HTTP/2 204 NO CONTENT
```
The curl command example below shows a user task being completed, supplying JSON input which refers to an additional JSON input file.

```
cat flow-input.json
{
  "nodeSelection" : {
    "nodes" : "RadioNode-1"
  },
  "softwarePackageSelection" : {
    "softwarePackages" : "RadioNode-ISO-v1"
  }
}

cat usertask-input.json
{"setupType":{"fileInput":{"fileName":"flow-input.json"}}}

curl -i -X POST http://localhost:8080/flow-automation/v1/executions/usertasks/5e0c7f54-c577-11ec-8d43-4e4dcec6f1d4/complete --form usertask-input-files=@flow-input.json --form usertask-input=@usertask-input.json

HTTP/2 204 NO CONTENT
```

### /v1/executions/suspend (PUT)
This endpoint suspends all flow executions related to the flow and flow version as shown in the curl command example below.
```
curl -i -X PUT 'http://localhost:8080/flow-automation/v1/executions/suspend?flow-id=com.ericsson.oss.fa.flows.sdk-flow-example&flow-version=1.0.0'

HTTP/2 204 NO CONTENT
```
This endpoint only works on flows that are inactive, for this endpoint to work on this example. The same flow with a different version has to be imported.


## Reference
* curl: <https://curl.se/docs/manpage.html>
* JSON Schema: <https://json-schema.org/>
* JSON Schema Online Validator: <https://www.jsonschemavalidator.net/>