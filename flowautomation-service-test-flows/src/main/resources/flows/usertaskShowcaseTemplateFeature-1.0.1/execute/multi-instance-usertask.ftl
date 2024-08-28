{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "title": "Multi-instance usertask schema",
  "description": "Multi-instance usertask schema",
  "$id": "multi-instance-usertask-schema",
  "name": "Multi-instance Usertask",
  "type": "object",
  "properties": {
    "multiInstanceUsertask": {
      "name": "Multi-instance Usertask",
      "type": "object",
      "format": "informational",
      "nameExtra": "${nameExtra}",
      "properties": {
        "instanceName": {
          "name": "Instance name",
          "type": "string",
          "default": "${instanceName}"
        }
      }
    }
  }
}
