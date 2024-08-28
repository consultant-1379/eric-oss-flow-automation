# Flow Automation API Guide

This section covers the following topics for Flow Automation:

[Flow](#psd-flow-automation-api-guide?chapter=flow)

- [Activate Flow Version](#psd-flow-automation-api-guide?chapter=activate-flow-version)
- [Delete Flow](#psd-flow-automation-api-guide?chapter=delete-flow)
- [Enable Flow](#psd-flow-automation-api-guide?chapter=enable-flow)
- [Execute Flow](#psd-flow-automation-api-guide?chapter=create-flow-execution)
- [Get Flow](#psd-flow-automation-api-guide?chapter=get-flow-definition)
- [Get Flow Input Schema](#psd-flow-automation-api-guide?chapter=get-flow-input-schema)
- [Get Flow Version Process Details](#psd-flow-automation-api-guide?chapter=get-process-details)
- [Get Flows](#psd-flow-automation-api-guide?chapter=get-flow-defintions)
- [Import Flow](#psd-flow-automation-api-guide?chapter=import-a-flow)

[Flow Execution](#psd-flow-automation-api-guide?chapter=flow-execution)

- [Back User Task](#psd-flow-automation-api-guide?chapter=back-user-task)
- [Complete Usertask](#psd-flow-automation-api-guide?chapter=complete-usertask)
- [Delete Execution](#psd-flow-automation-api-guide?chapter=delete-execution)
- [Get Report Variable](#psd-flow-automation-api-guide?chapter=get-report-variable)
- [Download Execution Resource](#psd-flow-automation-api-guide?chapter=download-execution-resource)
- [Get Flow Instance Events](#psd-flow-automation-api-guide?chapter=get-flow-instance-events)
- [Get Execution Report](#psd-flow-automation-api-guide?chapter=get-execution-report)
- [Get Execution Report Schema](#psd-flow-automation-api-guide?chapter=get-execution-report-schema)
- [Get Executions](#psd-flow-automation-api-guide?chapter=get-executions)
- [Get Flow Input Schema and Data](#psd-flow-automation-api-guide?chapter=get-flow-input-schema-and-data)
- [Get Schema of Usertask](#psd-flow-automation-api-guide?chapter=get-schema-of-usertask)
- [Get Usertasks of Execution](#psd-flow-automation-api-guide?chapter=get-usertasks-of-execution)
- [Stop Execution](#psd-flow-automation-api-guide?chapter=stop-execution)
- [Suspend all Executions](#psd-flow-automation-api-guide?chapter=suspend-all-executions)
- [Suspend active Execution](#psd-flow-automation-api-guide?chapter=suspend-active-execution)

# Flow

## Activate Flow Version

**Description**:
Activate or deactivate a version of a flow with given flow-id and flow-version.

**Path:**
`put /v1/flows/{flow-id}/activate`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 ``flow-id`` (required) | **String** | ID of a flow. | [default to null]

**Consumes**
This API call consumes the following media types via the Content-Type request header:
 - Content Type:``application/json``

**Query Parameters:**
| Name | Type | Description
| ------ | ------ | ------
| ``flow-version`` (required) | **String** | Version of a flow. default to null

**Request Body**:
| Name | Type | Description |
| ------ | ------ | ------
| ``FlowStatus `` | [FlowStatus](#psd-flow-automation-api-guide?chapter=flowstatus---up) | Value related to the status of a flow.

**Request Header**:
Content-Type: - ``application/json``

| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**204 No Content**| NoContent. The request has succeeded, but that the client doesn't need to navigate away from its current page. |  |
|**400 Bad Request**| If the request is malformed or syntactically incorrect. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up)
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Delete Flow

**Description**:
Removes the flow definition with the given flow-id.

**Path:**
`delete /v1/flows/{flow-id}`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 ``flow-id`` (required) | **String** | ID of a flow. | [default to null]

**Query Parameters**:
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``force`` (optional) | **Boolean** | Force the flow to be deleted, either true or false. | [default to null]

**Produces**
- Content-Type: ``application/json``

| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**204 No Content**| NoContent. The request has succeeded, but that the client doesn't need to navigate away from its current page. |  |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**412 Precondition Failed**| Flow with execution cannot be deleted when force is false. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up)
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Enable Flow

**Description**:
Enable or disable the flow with the given flow-id.

**Path:**
`put /v1/flows/{flow-id}/enable`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 ``flow-id`` (required)| **String**| ID of a flow. | [default to null]

**Consumes**
This API call consumes the following media types via the Content-Type request header:
- Content Type:``application/json``

**Request Body**:
| Name | Type | Description
| ------ | ------ | ------
| ``FlowStatus`` (optional) | **Boolean** | Status of a flow being enabled/disabled or active/deactive.

**Produces**:
Content-Type: ``application/json``

| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**204 No Content**| NoContent. The request has succeeded, but that the client doesn't need to navigate away from its current page. |  |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Create Flow Execution

**Description**:
Create a flow execution for the flow with the given flow-id.

**Path:**
`post /v1/flows/{flow-id}/execute`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 ``flow-id`` (required) | **String**| ID of a flow. | [default to null]

**Consumes**
This API call consumes the following media types via the Content-Type request header:
 - Content Type:``application/json``
 - Content Tpe:``multipart/form-data``

**Request Headers**:
| Name | Type | Description
| ------ | ------ | ------
| ``UserId`` (required) | **String**| User ID.

**Form Parameters**
 Name | Description | Notes
 ------------- | ------------- | ------------- |
 ``name`` (required) | Name to use for the created flow execution. | [default to null]
 ``file-name`` (optional) | Name of the file being used to execute a flow. | [default to null]
 ``flow-input`` (optional) | The file being used to execute the flow. | [default to null] format: byte

**Responses**:
- Content-Type: ``application/json``

| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. Flow execution created | See Below |
|**400 Bad Request**| Bad Request. If the request is malformed or syntactically incorrect | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

**200 OK Response**:
```
{
  "name":"SFE_123456"
}
```

## Get Flow Definition

**Description**:
Gets the definition of a flow with the given flow-id. 

**Path:**
`get /v1/flows/{flow-id}`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 ``flow-id`` (required) | **String**| ID of a flow. | [default to null]

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. Returns the definition of the flow with the given flow-id | [FlowDefinition](#psd-flow-automation-api-guide?chapter=flowdefinition---up) |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

**200 OK Response**:
```
{
  {
  "name" : "name",
  "flowVersions" : [ {
    "createdDate" : "createdDate",
    "createdBy" : "createdBy",
    "description" : "description",
    "active" : true,
    "version" : "version",
    "setupPhaseRequired" : true,
    "reportSupported" : true
  }, {
    "createdDate" : "createdDate",
    "createdBy" : "createdBy",
    "description" : "description",
    "active" : true,
    "version" : "version",
    "setupPhaseRequired" : true,
    "reportSupported" : true
  } ],
  "id" : "id",
  "source" : "source",
  "status" : "status"
  }
}
```

## Get Flow Input Schema

**Description**:
Returns flow input schema for the active version or for a specific version of a flow.

**Path:**
`get /v1/flows/{flow-id}/resource/flow-input-schema`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 ``flow-id`` (required) | **String** | ID of a flow. | [default to null]

**Query Parameters**:
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-version`` (optional) | **String** | Version of a flow | [default to null]

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. Returns flow input schema for the active version or for a specific version of a flow | **String** |
|**400 Bad Request**| Bad Request. If the request is malformed or syntactically incorrect | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Get Process Details

**Description**:
Returns the process details of the associated flow-id and flow-verison.

**Path:**
`get /v1/flows/{flow-id}/{flow-version}/process-details`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 ``flow-id`` (required) | **String**| ID of a flow. | [default to null]
 ``flow-version`` (required) | **String** | Version of a flow | [default to null]

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. Returns the flow version process details | [FlowVersionProcessDetails](#psd-flow-automation-api-guide?chapter=flowversionprocessdetails---upmodels) |
|**400 Bad Request**| Bad Request. If the request is malformed or syntactically incorrect | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

**200 OK Response**:
```
{
  "processId" : "processId",
  "setupProcessId" : "setupProcessId",
  "executeProcessId" : "executeProcessId"
}
```
## Get Flow Defintions

**Description**:
Gets the flow definitions for all imported flows.

**Path:**
`get /v1/flows`

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. Lists all imported flows | See Below |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

**200 OK Response**:
```
{
  "name" : "name",
  "flowVersions" : [ {
    "createdDate" : "createdDate",
    "createdBy" : "createdBy",
    "description" : "description",
    "active" : true,
    "version" : "version",
    "setupPhaseRequired" : true,
    "reportSupported" : true
  }, {
    "createdDate" : "createdDate",
    "createdBy" : "createdBy",
    "description" : "description",
    "active" : true,
    "version" : "version",
    "setupPhaseRequired" : true,
    "reportSupported" : true
  } ],
  "id" : "id",
  "source" : "source",
  "status" : "status"
}
```

## Import a Flow

**Description**:
Import a new flow.

**Path:**
`post /v1/flows`

**Consumes**
- Content-Type: ``multipart/form-data``

**Request Headers**:
| Name | Type | Description
| ------ | ------ | ------
| ``UserId`` (required) | **String** | User ID.

**Form Parameters**
| Name | Description
| ------ | ------
| ``flow-package`` (optional) | [default to null] format:byte

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. Flow Imported | [FlowDefinition](#psd-flow-automation-api-guide?chapter=flowdefinition---upmodels) |
|**400 Bad Request**| Bad Request. If the request is malformed or syntactically incorrect | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**409 Conflict**| Conflict. Conflict. The request could not be processed because of conflict in the request, such as the requested resource is not in the expected state, or the result of processing the request creates a conflict within the resource. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

**200 OK Response**:
```
{
  "name" : "name",
  "flowVersions" : [ {
    "createdDate" : "createdDate",
    "createdBy" : "createdBy",
    "description" : "description",
    "active" : true,
    "version" : "version",
    "setupPhaseRequired" : true,
    "reportSupported" : true
  }, {
    "createdDate" : "createdDate",
    "createdBy" : "createdBy",
    "description" : "description",
    "active" : true,
    "version" : "version",
    "setupPhaseRequired" : true,
    "reportSupported" : true
  } ],
  "id" : "id",
  "source" : "source",
  "status" : "status"
}
```

# Flow Execution

## Back User Task

**Description**:
Request that a flow execution in setup phase go back to previously active user task.

**Path:**
`post /v1/executions/usertasks/{task-id}/back`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 ``task-id`` (required) | **String** | Task ID of a usertask. | [default to null]

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**204 No Content**| No Content. The request has succeeded, but that the client doesn't need to navigate away from its current page. | |
|**403 Forbidden**| Forbidden. If the API consumer is not allowed to perform a particular request to a particular resource, the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Complete Usertask

**Description**:
Completes the usertask with the given task-id.

**Path:**
`post /v1/executions/usertasks/{task-id}/complete`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 ``task-id`` (required) | **String** | Task ID of a usertask. | [default to null]

**Consumes**
- Content-Type: ``multipart/form-data``

**Form Parameters**
| Name | Description |
| ------------- | ------------- |
| ``usertask-input`` (required) | Usertask input [default to null] |
| ``usertask-input-files`` (optional) | Usertask input, using a zip file. [default to null] format: binary |

**Produces**
Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**204 No Content**| No Content. The request has succeeded, but that the client doesn't need to navigate away from its current page. | |
|**400 Bad Request**| Bad Request. If the request is malformed or syntactically incorrect. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Delete Execution

**Description**:
Deletes the execution with the given flow-execution-name. It applies only if the flow execution is in following states - SETTING_UP.

**Path:**
`delete /v1/executions/{flow-execution-name}`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 ``flow-execution-name`` (required) | **String**| Flow execution name. | [default to null]

 **Query Parameters:**
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-id`` (required) | **String** |  ID of a flow. | [default to null]

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**204 No Content**| No Content. The request has succeeded, but that the client doesn't need to navigate away from its current page. | |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**409 Conflict**| Conflict. Conflict. The request could not be processed because of conflict in the request, such as the requested resource is not in the expected state, or the result of processing the request creates a conflict within the resource. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

# Get Report Variable

**Description**:
Get the requested report variable for a flow execution.

**Path:**
`get /v1/executions/{flow-execution-name}/download/report-variable/{name}`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 ``flow-execution-name`` (required) | **String** | Flow execution name. | [default to null]
 ``name`` (required) | **String** | Report variable name. | [default to null]

 **Query Parameters:**
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-id`` (required) | **String** |  ID of a flow. | [default to null]

**Produces**
- Content-Type:``application/json``
- Content-Type:``application/octet-stream``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. The requested report variable for the flow execution. It is present in the response as a file. | |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Download Execution Resource

**Description**:
Downloads a requested resource.

**Path:**
`get /v1/executions/{flow-execution-name}/download/{resource}`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
``flow-execution-name`` (required) | **String** | Flow execution name. | [default to null]
``resource`` (required) | **String** | Resource type. | [default to null]

 **Query Parameters:**
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-id`` (required) | **String** |  ID of a flow. | [default to null]

**Produces**
- Content-Type:``application/json``
- Content-Type:``application/octet-stream``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. The requested resource for the flow execution. It is present in the response as a file. | |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Get Flow Instance Events

**Description**:
Returns flow instance events using multiple query filters.

**Path:**
`get /v1/executions/{flow-execution-name}/events`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
``flow-execution-name`` (required) | **String** | Flow execution name. | [default to null]

 **Query Parameters:**
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-id`` (required) | **String** |  ID of a flow. | [default to null]
| ``startDate `` (optional) | **String** |  Date when the flow was started. | [default to null]
| ``endDate `` (optional) | **String** |  Date when the flow has ended. | [default to null]
| ``resource `` (optional) | **String** |  Resource type. | [default to null]
| ``message `` (optional) | **String** |  Message corresponding to the event. | [default to null]
| ``severity `` (optional) | **String** |  Severity level of the event. | [default to null]
| ``page-num`` (optional) | **String** |  Page number. | [default to null] format: int32
| ``page-size`` (optional) | **String** |  Number of messages on the page. | [default to null] format: int32
| ``sort-by`` (optional) | **String** |  Sort the events by a defined method. | [default to eventTime]
| ``sort-order`` (optional) | **String** |  Sort the events by a defined order. | [default to null]
| ``from`` (optional) | **String** |  ID of a flow. | [default to null] format: int32
| ``to`` (optional) | **String** |  ID of a flow. | [default to null] format: int32

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. Returns Flow related events that occurred during the life cycle of a Flow Instance. | [PaginatedDataFlowExecutionEvent](#psd-flow-automation-api-guide?chapter=paginateddataflowexecutionevent---upmodels) |
|**400 Bad Request**| Bad Request. If the request is malformed or syntactically incorrect. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

**200 OK Response**:
```
{
  "records" : [ {
    "severity" : "severity",
    "eventData" : "eventData",
    "eventTime" : "eventTime",
    "message" : "message",
    "target" : "target"
  }, {
    "severity" : "severity",
    "eventData" : "eventData",
    "eventTime" : "eventTime",
    "message" : "message",
    "target" : "target"
  } ],
  "numberOfRecords" : 0
}
```
## Get Execution Report

**Description**:
Retrieves report for a specified flow execution.

**Path:**
`get /v1/executions/{flow-execution-name}/report`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
``flow-execution-name`` (required) | **String** | Flow execution name. | [default to null]

 **Query Parameters:**
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-id`` (required) | **String** |  ID of a flow. | [default to null]

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. The requested resource for the flow execution. It is present in the response as a file. | |
|**400 Bad Request**| Bad Request. If the request is malformed or syntactically incorrect. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Get Execution Report Schema

**Description**:
Retrieves the report schema of the execution with the given flow-execution-name.

**Path:**
`get /v1/executions/{flow-execution-name}/report-schema`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
``flow-execution-name`` (required)| **String** | Flow execution name. | [default to null]

 **Query Parameters:**
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-id`` (required) | **String** |  ID of a flow. | [default to null]

**Produces**
Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. Retrieves the report schema of the execution with the given flow-execution-name. | |
|**400 Bad Request**| Bad Request. If the request is malformed or syntactically incorrect. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Get Executions

**Description**:
Lists executions of all or specific flows.

**Path:**
`get /v1/executions`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
``flow-execution-name`` (required)| **String**| Flow execution name. | [default to null]

**Request Headers**:
| Name | Type | Description
| ------ | ------ | ------
| ``UserId`` (optional) | **String** | User ID.

 **Query Parameters:**
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-id`` (optional) | **String** |  ID of a flow. | [default to null]
| ``flow-execution-name`` (optional) | **String** |  Flow execution name. | [default to null]
| ``flow-version`` (optional) | **String** |  Version of a flow. | [default to null]
| ``filter-by-user`` (optional) | **String** |  Filter results by user. | [default to null]

**Produces**
-Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. Lists executions of all or specific flows. | See Below |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

**200 OK Response**:
```
{
  "executedBy" : "executedBy",
  "numberActiveUserTasks" : 0,
  "processInstanceId" : "processInstanceId",
  "flowVersion" : "flowVersion",
  "flowName" : "flowName",
  "duration" : "duration",
  "summaryReport" : "summaryReport",
  "createdBy" : "createdBy",
  "flowSource" : "flowSource",
  "processInstanceBusinessKey" : "processInstanceBusinessKey",
  "name" : "name",
  "startTime" : "startTime",
  "endTime" : "endTime",
  "state" : "state",
  "flowId" : "flowId"
}
```

## Get Flow Input Schema and Data

**Description**:
Retrieves the flow input schema and data.

**Path:**
`get /v1/executions/{flow-execution-name}/flowinput-schema-data`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
``flow-execution-name`` (required)| **String** | Flow execution name. | [default to null]

 **Query Parameters:**
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-id`` (required) | **String** |  ID of a flow. | [default to null]

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. Return flow input schema and data. | |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Get Schema of Usertask

**Description**:
Retrieves the schema of the usertask with given task-id.

**Path:**
`get /v1/executions/usertasks/{task-id}/schema`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
``task-id`` (required)| **String**| Task ID of a usertask. | [default to null]

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. Return flow input schema and data. | See Below |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

**200 OK Response**:
```
{
  "schema" : "schema"
}
```

## Get Usertasks of Execution

**Description**:
Retrieves user tasks for a flow execution.

**Path:**
`get /v1/executions/{flow-execution-name}/usertasks`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
``flow-execution-name`` (required)| **String** | Flow execution name. | [default to null]

 **Query Parameters:**
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-id`` (optional) | **String** |  ID of a flow. | [default to null]

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**200 OK**| OK. List of user tasks. | See Below |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

**200 OK Response**:
```
{
  "processDefinitionId" : "processDefinitionId",
  "name" : "name",
  "nameExtra" : "nameExtra",
  "id" : "id",
  "taskDefinitionId" : "taskDefinitionId",
  "status" : "status"
}
```

## Stop Execution

**Description**:
Submits a request to stop an active flow execution.

**Path:**
`put /v1/executions/{flow-execution-name}/stop`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
``flow-execution-name`` (required)| **String** | Flow execution name. | [default to null]

 **Query Parameters:**
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-id`` (required) | **String** |  ID of a flow. | [default to null]

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**204 No Content**| No Content. The request has succeeded, but that the client doesn't need to navigate away from its current page. | |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**409 Conflict**| Conflict. The request could not be processed because of conflict in the request, such as the requested resource is not in the expected state, or the result of processing the request creates a conflict within the resource. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Suspend all Executions

**Description**:
Suspends all executions for a flow version with given flow-id and flow-version.

**Path:**
`put /v1/executions/suspend`

 **Query Parameters:**
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-id`` (required) | **String** |  ID of a flow. | [default to null]
| ``flow-version`` (required) | **String** | Version of a flow. | [default to null]

**Produces**
- Content-Type: ``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**204 No Content**| No Content. The request has succeeded, but that the client doesn't need to navigate away from its current page. | |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**409 Conflict**| Conflict. The request could not be processed because of conflict in the request, such as the requested resource is not in the expected state, or the result of processing the request creates a conflict within the resource. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

## Suspend active Execution

**Description**:
Suspends an active flow execution.

**Path:**
`put /v1/executions/{flow-execution-name}/suspend`

**Path Parameters:**
Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
``flow-execution-name`` (required)| **String**| Flow execution name. | [default to null]

 **Query Parameters:**
| Name | Type | Description | Example
| ------ | ------ | ------ | ------
| ``flow-id`` (required) | **String** |  ID of a flow. | [default to null]

**Produces**
- Content-Type:
``application/json``

**Responses**:
| Response Code | Description | Response Data Structure
| ------ | ------ | ------ |
|**204 No Content**| No Content. The request has succeeded, but that the client doesn't need to navigate away from its current page. | |
|**404 Not Found**| Not Found. The requested resource entity was not found. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**409 Conflict**| Conflict. The request could not be processed because of conflict in the request, such as the requested resource is not in the expected state, or the result of processing the request creates a conflict within the resource. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**500 Internal Server Error**| Internal Server Error. If there is an application error not related to the client's input that cannot be easily mapped to any other HTTP response code ("catch all error"), the API producer responds with this response code. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |
|**503 Service Unavailable**| A runtime exception indicating that the requested resource cannot be served. | [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up) |

# Models

Table of Contents
- [ErrorDetails](#psd-flow-automation-api-guide?chapter=errordetails---up)
- [ExecuteInput](#psd-flow-automation-api-guide?chapter=executeinput---up)
- [FlowDefinition](#psd-flow-automation-api-guide?chapter=flowdefinition---up)
- [FlowExecution](#psd-flow-automation-api-guide?chapter=flowexecution---up)
- [FlowExecutionEvent](#psd-flow-automation-api-guide?chapter=flowexecutionevent---up)
- [FlowExecutionName](#psd-flow-automation-api-guide?chapter=flowexecutionname---up)
- [FlowStatus](#psd-flow-automation-api-guide?chapter=flowstatus---up)
- [FlowVersion](#psd-flow-automation-api-guide?chapter=flowversion---up)
- [FlowVersionProcessDetails](#psd-flow-automation-api-guide?chapter=flowversionprocessdetails---up)
- [PaginatedDataFlowExecutionEvent](#psd-flow-automation-api-guide?chapter=paginateddatafloweexecutionevent---up)
- [ProblemDetails](#psd-flow-automation-api-guide?chapter=#problemdetails---up)
- [UserTask](#psd-flow-automation-api-guide?chapter=usertask---up)
- [UserTaskSchema](#psd-flow-automation-api-guide?chapter=usertaskschema---up)

## ErrorDetails - [Up](#psd-flow-automation-api-guide?chapter=models)
code (optional)
- **String** Flow Automation error code.

errorMessage (optional)
- **String** Message describing the error.

## ExecuteInput - [Up](#psd-flow-automation-api-guide?chapter=models)
name
- **String** Name to use for the created flow execution

fileMinusname (optional)
- **String** Name of the file being used to execute a flow

flowMinusinput (optional)
- **byte[]** The file being used to execute the flow. format: byte

## FlowDefinition - [Up](#psd-flow-automation-api-guide?chapter=models)
Definition of a flow object.
id (optional)
- **String** ID of a Flow

name (optional)
- **String** Name of a flow

status (optional)
- **String** Status of a flow

source (optional)
- **String** Source of the flow

flowVersions (optional)
- **array[FlowVersion]** Versions of the flow.

## FlowExecution - [Up](#psd-flow-automation-api-guide?chapter=models)
Description about a flow execution.
name (optional)
- **String** Name of the execution

flowId (optional)
- **String** ID of a Flow

flowName (optional)
- **String** Name of the flow

flowVersion (optional)
- **String** Version of the flow

createdBy (optional)
- **String** User who imported the flow version

executedBy (optional)
- **String** User who created the flow execution

startTime (optional)
- **String** Time when the flow was executed

endTime (optional)
- **String** Time when the flow had finished executing

duration (optional)
- **String** How long the flow was executing for

state (optional)
- **String** The current state of the flow

numberActiveUserTasks (optional)
- **Long** Number of active user tasks associated with the flow execution format: int64

flowSource (optional)
- **String** The source of the flow

summaryReport (optional)
- **String** The summary report of the flow execution

processInstanceId (optional)
- **String** The process instance ID of the flow execution

processInstanceBusinessKey (optional)
- **String** The process instance Business key associated with the flow execution

## FlowExecutionEvent - [Up](#psd-flow-automation-api-guide?chapter=models)
Information about an event for a flow execution
eventTime (optional)
- **String** The time of the event

severity (optional)
- **String** Severity level of the corresponding event

target (optional)
- **String** The target of the relevant event

message (optional)
- **String** Information about the event itself

eventData (optional)
- **String** Data about the associated event

## FlowExecutionName - [Up](#psd-flow-automation-api-guide?chapter=models)
The name of the flow execution
name
- **String** Name for the associated flow execution

## FlowStatus - [Up](#psd-flow-automation-api-guide?chapter=models)
Value related to the status of a flow.
value (optional)
- **Boolean** Status of a flow being enabled/disabled or active/deactive.

## FlowVersion - [Up](#psd-flow-automation-api-guide?chapter=models)
Description about versions of a flow.
version (optional)
- **String** Version of the flow.

description (optional)
- **String** Description of the flow.

active (optional)
- **Boolean** Is the flow active?

createdBy (optional)
- **String** User who imported the flow version.

createdDate (optional)
- **String** Date when flow was created.

setupPhaseRequired (optional)
- **Boolean** Does the flow require a setup phase?

reportSupported (optional)
- **Boolean** Does the flow support a report?

## FlowVersionProcessDetails - [Up](#psd-flow-automation-api-guide?chapter=models)
Process details relating to a flow.
processId (optional)
- **String** Process id of a flow

setupProcessId (optional)
- **String** Setup process id of a flow

executeProcessId (optional)
- **String** Execute process id of a flow

## PaginatedDataFlowExecutionEvent - [Up](#psd-flow-automation-api-guide?chapter=models)
Information about all the events for a flow execution
numberOfRecords (optional)
- **Long** Number of records associated with the execution event format: int64

records (optional)
- **array[FlowExecutionEvent]**

## ProblemDetails - [Up](#psd-flow-automation-api-guide?chapter=models)
status
- **Integer** Integer The HTTP status code for this occurrence of the problem.

reasonPhrase
- **String** A human-readable explanation specific to this occurrence of the problem.

errors
- **array[ErrorDetails]**

## UserTask - [Up](#psd-flow-automation-api-guide?chapter=models)
Information about a usertask for a flow execution
id (optional)
- **String** ID of a user task

name (optional)
- **String** Name of the usertask

nameExtra (optional)
- **String** Extra name of the usertask

processDefinitionId (optional)
- **String** Process definition of the usertask

taskDefinitionId (optional)
- **String** Task definition id of the usertask

status (optional)
- **String** Status of the usertask

## UserTaskSchema - [Up](#psd-flow-automation-api-guide?chapter=models)
Information about a usertask schema for a flow execution

Schema (optional)
- **String** Schema associated with the usertask

id (optional)
- **String** ID of a user task

name (optional)
- **String** Name of the usertask

nameExtra (optional)
- **String** Extra name of the usertask

processDefinitionId (optional)
- **String** Process definition of the usertask

taskDefinitionId (optional)
- **String** Task definition id of the usertask

status (optional)
- **String** Status of the usertask