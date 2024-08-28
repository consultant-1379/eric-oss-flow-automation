{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "title": "Select Dynamic Objects",
    "description": "Select Dynamic Objects schema",
    "$id": "dynamic-objects-selection-schema",
    "name": "Select Dynamic Objects User Task",
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
                        "default": ${dynamicObject.default?c}
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
