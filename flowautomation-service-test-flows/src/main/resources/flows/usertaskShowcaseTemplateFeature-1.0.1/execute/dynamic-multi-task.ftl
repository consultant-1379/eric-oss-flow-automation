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
                <#list nodeTypesActivities as nodeTypeActivities >
                "${nodeTypeActivities.nodeType}": {
                    "name": "${nodeTypeActivities.name}",
                    "type": "object",
                    "properties": {
                        <#list nodeTypeActivities.activities as nodeActivity >
                        "${nodeActivity.propertyName}": {
                            "name": "${nodeActivity.name}",
                            "type": "object",
                            "properties": {
                                "checkbox": {
                                "name": "${nodeActivity.checkbox}",
                                "type": "boolean",
                                "format": "checkbox",
                                "info": "${nodeActivity.info}",
                                "default": ${nodeActivity.default?c}
                                }
                            },
                            "required": [
                            "checkbox"
                            ],
                            "additionalProperties": false
                        }
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
