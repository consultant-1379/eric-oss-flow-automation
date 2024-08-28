
/*******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.services.flowautomation.test.fwk.rest;

import java.io.IOException;

import com.ericsson.oss.services.flowautomation.model.UserTaskSchema;
import com.ericsson.oss.services.flowautomation.model.UserTaskStatus;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class UserTaskSchemaDeserializer extends StdDeserializer<UserTaskSchema> {

    private static final long serialVersionUID = 1L;

    public UserTaskSchemaDeserializer() {
        this(null);
    }

    public UserTaskSchemaDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public UserTaskSchema deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        final JsonNode userTaskSchemaNode = jp.getCodec().readTree(jp);
        final String id = userTaskSchemaNode.get("id").asText();
        final String name = userTaskSchemaNode.get("name").asText();
        final String processDefinitionId = userTaskSchemaNode.get("processDefinitionId").asText();
        final String taskDefinitionId = userTaskSchemaNode.get("taskDefinitionId").asText();
        final String statusString = userTaskSchemaNode.get("status").asText();
        final String schema = userTaskSchemaNode.get("schema").asText();
        UserTaskStatus status = null;
        if (statusString.equals("active")) {
            status = UserTaskStatus.ACTIVE;
        } else if (statusString.equals("completed")) {
            status = UserTaskStatus.COMPLETED;
        }

        return new UserTaskSchema(id, name, processDefinitionId, taskDefinitionId, status, schema);
    }

}
