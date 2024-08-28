{
"$schema": "http://json-schema.org/draft-04/schema#",
"title": "Select Dynamic Objects",
"description": "Select Dynamic Objects schema",
"$id": "dynamic-objects-selection-schema",
"name": "Select Dynamic Objects Usertask",
"type": "object",
"properties": {
"selectDynamicObjects": {
"name": "Select Dynamic Objects",
"type": "object",
"properties": {
<#list dynamicObjectsOptions as dynamicObject >
    "${dynamicObject.propertyName}": {
    "name": "${dynamicObject.name}",
    "type": "boolean",
    "format": "checkbox",
    "info": "${dynamicObject.info}",
    "default": ${dynamicObject.default?c},
    "nameExtra": "${dynamicObject.nameExtra}"
    }
    <#sep>,
</#list>
},
"required": [
<#list dynamicObjectsOptions as dynamicObject >
    "${dynamicObject.propertyName}"
    <#sep>,
</#list>
],
"additionalProperties": false

}
}
}
