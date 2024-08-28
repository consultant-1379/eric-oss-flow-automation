{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "title": "Dynamic Multi-Task usertask schema",
    "description": "Dynamic Multi-Task usertask schema",
    "$id": "dynamic-multi-task-schema",
    "name": "Dynamic Multi-Task Usertask",
    "type": "object",
    "properties": {
        "dynamicMultiTaskUsertask": {
            "name": "Dynamic Multi-Task Usertask",
            "type": "object",
            "properties": {
                <#list dynamicObjects as dynamicObject >
                "${dynamicObject.id}": {
                    "name": "${dynamicObject.name}",
                    "type": "object",
                    "properties": {
                        <#list dynamicObject.tasks as task >

                        <#if task.checkbox??>
                        "${task.propertyName}": {
                            "name": "${task.name}",
                            "type": "object",
                            "properties": {
                                "checkbox": {
                                    "name": "${task.checkbox}",
                                    "nameExtra" : "${task.nameExtra}",
                                    "type": "boolean",
                                    "format": "checkbox",
                                    "info": "${task.info}",
                                    "default": ${task.default?c}
                                }
                            },
                            "required": [
                                "checkbox"
                            ],
                            "additionalProperties": false
                        }
                        </#if>

                        <#if task.listbox??>
                        "${task.propertyName}": {
                            "name": "${task.name}",
                            "nameExtra" : "${task.nameExtra}",
                            "type": "string",
                            "format": "select-list",
                            "info": "${task.info}",
                            "enum": ${task.enum},
                            "enumNames":  ${task.enumNames},
                            "default": "${task.default}"
                            }
                        </#if>
                        <#sep>,
                        </#list>
                    }
                }
                <#sep>,
                </#list>
            }
        }
    }
}
