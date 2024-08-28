{
"$schema": "http://json-schema.org/draft-04/schema#",
"title": "Usertask Selection",
"description": "Usertask Selection schema",
"$id": "usertask-selection-schema",
"name": "UsertaskSelection Usertask",
"type": "object",
"properties": {
"usertaskSelection": {
"name": "Usertask Selection",
"type": "object",
"properties": {
    <#list tasks as task >
        "${task.propertyName}": {
        "name": "${task.name}",
        "type": "boolean",
        "format": "checkbox",
        "info": "${task.info}",
        "default": ${task.default?c}
        }
        <#sep>,
    </#list>
        },
        "required": [
            <#list tasks as task >
            "${task.propertyName}"
                <#sep>,
            </#list>
        ],
        "additionalProperties": false

}
}
}
