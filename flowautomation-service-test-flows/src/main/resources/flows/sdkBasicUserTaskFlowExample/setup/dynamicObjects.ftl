{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "title": "Dynamic Objects",
    "description": "Dynamic Objects",
    "$id": "dynamic-objects-schema",
    "name": "Dynamic Objects",
    "type": "object",
    "properties": {
        "dynamicObjects": {
            "name": "Dynamic Objects",
            "type": "object",
            "properties": {
                <#list dynamicObjectsInput as dynamicObject >
                "${dynamicObject.id}": {
                    "name": "${dynamicObject.name}",
                    "type": "object",
                    "properties": {
                        <#list dynamicObject.properties as property >

                        <#if property.textbox??>
                        "${property.propertyName}": {
                            "name": "${property.name}",
                            "type": "string"
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
