{
"$schema": "http://json-schema.org/draft-04/schema#",
"title": "Informational User Task",
"description": "Informational User Task schema",
"$id": "informational-user-task-schema",
"name": "Informational Usertask",
"type": "object",
"properties": {
"information": {
"name": "Information",
"type": "object",
"format": "informational",
"nameExtra" : "${information.nameExtra}",
"properties": {
<#list information.properties as property >
    "${property.propertyName}": {
    "name": "${property.name}",
    "type": "${property.type}",
    "format": "${property.format}",
    "info": "${property.info}",
    "default": "${property.default}"
    }
    <#sep>,
</#list>
},
"additionalProperties": false

}
}
}
