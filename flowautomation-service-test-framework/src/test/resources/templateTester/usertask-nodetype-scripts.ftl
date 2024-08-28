{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "title": "Node type scripts selection usertask schema",
    "description": "Node type scripts selection usertask schema",
    "$id": "usertask-nodetype-scripts",
    "name": "Node type script selection usertask",
    "type": "object",
    "properties": {
        "nodeTypeScriptSelection": {
            "name": "Node Type Script Selection",
            "type": "object",
            "properties": {
                <#list supportedNeTypes as neType>
                    "${neType}": {
                        "name": "Node type",
                        "nameExtra": "${neType}",
                        "type": "object",
                          "properties": {
                            <#if preparationPhase??>
                            "preInstallScript": {
                              "name": "Pre-Install Script",
                              "type": "object",
                              "properties": {
                                "performPreInstallScript": {
                                  "name": "Perform Pre-Install Script",
                                  "description": "Perform Scripts before installing software package on the node.",
                                  "type": "object",
                                  "format": "checkbox",
                                  "properties": {
                                    "scriptDetails": {
                                      "name": "Script File",
                                      "type": "object",
                                      "format": "radio",
                                      "oneOf": [
                                        {
                                          "properties": {
                                            "scriptFilePath": {
                                              "name": "Provide Script File Path",
                                              "description": "Provide absolute path of the script file.",
                                              "type": "object",
                                              "properties": {
                                                "path": {
                                                  "name": "Script File Path",
                                                  "type": "string"
                                                }
                                              },
                                              "required": [
                                                "path"
                                              ],
                                              "additionalProperties": false
                                            }
                                          },
                                          "required": [
                                            "scriptFilePath"
                                          ],
                                          "additionalProperties": false
                                        },
                                        {
                                          "properties": {
                                            "configureScriptFilePath": {
                                              "name": "Select Script File",
                                              "description": "Select required script from the list displayed. Script should be available in /home/shared/ASU/.",
                                              "type": "object",
                                              "properties": {
                                                "path": {
                                                  "name": "Script File Path",
                                                  "type": "string",
                                                  "format": "select-list",
                                                    "enum": ${scriptFiles}
                                                }
                                              },
                                              "required": [
                                                "path"
                                              ],
                                              "additionalProperties": false
                                            }
                                          },
                                          "required": [
                                            "configureScriptFilePath"
                                          ],
                                          "additionalProperties": false
                                        }
                                      ],
                                      "default": "scriptFilePath"
                                    },
                                    "scriptTimeout": {
                                      "name": "Timeout for execution in seconds",
                                      "description": "Maximum time that the script can take to execute.",
                                      "type": "integer",
                                      "minimum": 60,
                                      "maximum": 3600,
                                      "default": 600
                                    },
                                    "flowBehaviorOnFailure": {
                                      "name": "In the event of script failure",
                                      "type": "string",
                                      "format": "select",
                                      "uniqueItems": true,
                                      "maxSelect": 1,
                                      "enum": [
                                        "EXCLUDE_NODE_FROM_UPGRADE",
                                        "DO_NOT_EXCLUDE_NODE_FROM_UPGRADE",
                                        "PROMPT_FOR_CONFIRMATION"
                                      ],
                                      "enumNames": [
                                        "Exclude node from Upgrade",
                                        "Do not exclude node from Upgrade",
                                        "Prompt for Confirmation"
                                      ],
                                      "default": "PROMPT_FOR_CONFIRMATION"
                                    }
                                  },
                                  "additionalProperties": false,
                                  "required": [
                                    "scriptDetails",
                                    "scriptTimeout",
                                    "flowBehaviorOnFailure"
                                  ]
                                }
                              },
                              "additionalProperties": false
                            }
                            <#if activationPhase??>,</#if>
                          </#if>
                            <#if activationPhase??>
                            "preUpgradeScript": {
                              "name": "Pre-Upgrade Script",
                              "type": "object",
                              "properties": {
                                "performPreUpgradeScript": {
                                  "name": "Perform Pre-Upgrade Script",
                                  "description": "Perform Scripts before upgrading the node.",
                                  "type": "object",
                                  "format": "checkbox",
                                  "properties": {
                                    "scriptDetails": {
                                      "name": "Script File",
                                      "type": "object",
                                      "format": "radio",
                                      "oneOf": [
                                        {
                                          "properties": {
                                            "scriptFilePath": {
                                              "name": "Provide Script File Path",
                                              "description": "Provide absolute path of the script file.",
                                              "type": "object",
                                              "properties": {
                                                "path": {
                                                  "name": "Script File Path",
                                                  "type": "string"
                                                }
                                              },
                                              "required": [
                                                "path"
                                              ],
                                              "additionalProperties": false
                                            }
                                          },
                                          "required": [
                                            "scriptFilePath"
                                          ],
                                          "additionalProperties": false
                                        },
                                        {
                                          "properties": {
                                            "configureScriptFilePath": {
                                              "name": "Select Script File",
                                              "description": "Select required script from the list displayed. Script should be available in /home/shared/ASU/.",
                                              "type": "object",
                                              "properties": {
                                                "path": {
                                                  "name": "Script File Path",
                                                  "type": "string",
                                                  "format": "select-list",
                                                    "enum": ${scriptFiles}
                                                }
                                              },
                                              "required": [
                                                "path"
                                              ],
                                              "additionalProperties": false
                                            }
                                          },
                                          "required": [
                                            "configureScriptFilePath"
                                          ],
                                          "additionalProperties": false
                                        }
                                      ],
                                      "default": "scriptFilePath"
                                    },
                                    "scriptTimeout": {
                                      "name": "Timeout for execution in seconds",
                                      "description": "Maximum time that the script can take to execute.",
                                      "type": "integer",
                                      "minimum": 60,
                                      "maximum": 3600,
                                      "default": 600
                                    },
                                    "flowBehaviorOnFailure": {
                                      "name": "In the event of script failure",
                                      "type": "string",
                                      "format": "select",
                                      "uniqueItems": true,
                                      "maxSelect": 1,
                                      "enum": [
                                        "EXCLUDE_NODE_FROM_UPGRADE",
                                        "DO_NOT_EXCLUDE_NODE_FROM_UPGRADE",
                                        "PROMPT_FOR_CONFIRMATION"
                                      ],
                                      "enumNames": [
                                        "Exclude node from Upgrade",
                                        "Do not exclude node from Upgrade",
                                        "Prompt for Confirmation"
                                      ],
                                      "default": "PROMPT_FOR_CONFIRMATION"
                                    }
                                  },
                                  "additionalProperties": false,
                                  "required": [
                                    "scriptDetails",
                                    "scriptTimeout",
                                    "flowBehaviorOnFailure"
                                  ]
                                }
                              },
                              "additionalProperties": false
                            },
                            "postUpgradeScript": {
                              "name": "Post-Upgrade Script",
                              "type": "object",
                              "properties": {
                                "performPostUpgradeScript": {
                                  "name": "Perform Post-Upgrade Script",
                                  "description": "Perform Scripts after upgrading the node.",
                                  "type": "object",
                                  "format": "checkbox",
                                  "properties": {
                                    "scriptDetails": {
                                      "name": "Script File",
                                      "type": "object",
                                      "format": "radio",
                                      "oneOf": [
                                        {
                                          "properties": {
                                            "scriptFilePath": {
                                              "name": "Provide Script File Path",
                                              "description": "Provide absolute path of the script file.",
                                              "type": "object",
                                              "properties": {
                                                "path": {
                                                  "name": "Script File Path",
                                                  "type": "string"
                                                }
                                              },
                                              "required": [
                                                "path"
                                              ],
                                              "additionalProperties": false
                                            }
                                          },
                                          "required": [
                                            "scriptFilePath"
                                          ],
                                          "additionalProperties": false
                                        },
                                        {
                                          "properties": {
                                            "configureScriptFilePath": {
                                              "name": "Select Script File",
                                              "description": "Select required script from the list displayed. Script should be available in /home/shared/ASU/.",
                                              "type": "object",
                                              "properties": {
                                                "path": {
                                                  "name": "Script File Path",
                                                  "type": "string",
                                                  "format": "select-list",
                                                    "enum": ${scriptFiles}
                                                }
                                              },
                                              "required": [
                                                "path"
                                              ],
                                              "additionalProperties": false
                                            }
                                          },
                                          "required": [
                                            "configureScriptFilePath"
                                          ],
                                          "additionalProperties": false
                                        }
                                      ],
                                      "default": "scriptFilePath"
                                    },
                                    "scriptTimeout": {
                                      "name": "Timeout for execution in seconds",
                                      "description": "Maximum time that the script can take to execute.",
                                      "type": "integer",
                                      "minimum": 60,
                                      "maximum": 3600,
                                      "default": 600
                                    },
                                    "flowBehaviorOnFailure": {
                                      "name": "In the event of script failure",
                                      "type": "string",
                                      "format": "select",
                                      "uniqueItems": true,
                                      "maxSelect": 1,
                                      "enum": [
                                        "EXCLUDE_NODE_FROM_UPGRADE",
                                        "DO_NOT_EXCLUDE_NODE_FROM_UPGRADE",
                                        "PROMPT_FOR_CONFIRMATION"
                                      ],
                                      "enumNames": [
                                        "Exclude node from Upgrade",
                                        "Do not exclude node from Upgrade",
                                        "Prompt for Confirmation"
                                      ],
                                      "default": "PROMPT_FOR_CONFIRMATION"
                                    }
                                  },
                                  "additionalProperties": false,
                                  "required": [
                                    "scriptDetails",
                                    "scriptTimeout",
                                    "flowBehaviorOnFailure"
                                  ]
                                }
                              },
                              "additionalProperties": false
                            },
                            "cleanUpScript": {
                              "name": "Clean-up Script",
                              "type": "object",
                              "properties": {
                                "performCleanUpScript": {
                                  "name": "Perform Clean-up Script",
                                  "description": "Perform Cleanup Script at the end.",
                                  "type": "object",
                                  "format": "checkbox",
                                  "properties": {
                                    "scriptDetails": {
                                      "name": "Script File",
                                      "type": "object",
                                      "format": "radio",
                                      "oneOf": [
                                        {
                                          "properties": {
                                            "scriptFilePath": {
                                              "name": "Provide Script File Path",
                                              "description": "Provide absolute path of the script file.",
                                              "type": "object",
                                              "properties": {
                                                "path": {
                                                  "name": "Script File Path",
                                                  "type": "string"
                                                }
                                              },
                                              "required": [
                                                "path"
                                              ],
                                              "additionalProperties": false
                                            }
                                          },
                                          "required": [
                                            "scriptFilePath"
                                          ],
                                          "additionalProperties": false
                                        },
                                        {
                                          "properties": {
                                            "configureScriptFilePath": {
                                              "name": "Select Script File",
                                              "description": "Select required script from the list displayed. Script should be available in /home/shared/ASU/.",
                                              "type": "object",
                                              "properties": {
                                                "path": {
                                                  "name": "Script File Path",
                                                  "type": "string",
                                                  "format": "select-list",
                                                    "enum": ${scriptFiles}
                                                }
                                              },
                                              "required": [
                                                "path"
                                              ],
                                              "additionalProperties": false
                                            }
                                          },
                                          "required": [
                                            "configureScriptFilePath"
                                          ],
                                          "additionalProperties": false
                                        }
                                      ],
                                      "default": "scriptFilePath"
                                    },
                                    "scriptTimeout": {
                                      "name": "Timeout for execution in seconds",
                                      "description": "Maximum time that the script can take to execute.",
                                      "type": "integer",
                                      "minimum": 60,
                                      "maximum": 3600,
                                      "default": 600
                                    }
                                  },
                                  "additionalProperties": false,
                                  "required": [
                                    "scriptDetails",
                                    "scriptTimeout"
                                  ]
                                }
                              },
                              "additionalProperties": false
                            }
                            </#if>
                           }
                    }<#sep>,
                </#list>
            }
        }
    },
    "required": [ "nodeTypeScriptSelection" ], "additionalProperties": false
}

