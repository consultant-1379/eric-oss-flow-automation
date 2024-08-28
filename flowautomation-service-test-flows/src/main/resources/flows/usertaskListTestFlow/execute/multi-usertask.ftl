{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "name": "${usertask.name}",
  "type": "object",
  "properties": {
    "nonGroupedUsertask": {
      "name": "${usertask.name}",
      "type": "object",
      "format": "informational",
      "properties": {
        "data": {
          "name": "Data",
          "type": "string",
          "readOnly": true,
          "default": "${usertask.data}"
        }
      }
    }
  }
}
