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

import com.ericsson.oss.services.flowautomation.model.UserTask;
import com.ericsson.oss.services.flowautomation.model.UserTaskStatus;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class UserTaskDeserializer extends StdDeserializer<UserTask> {

    private static final long serialVersionUID = 1L;

    public UserTaskDeserializer() {
        this(null);
    }

    public UserTaskDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public UserTask deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        final JsonNode flowExecutionNode = jp.getCodec().readTree(jp);
        final String id = flowExecutionNode.get("id").asText();
        final String name = flowExecutionNode.get("name").asText();
        final String processDefinitionId = flowExecutionNode.get("processDefinitionId").asText();
        final String taskDefinitionId = flowExecutionNode.get("taskDefinitionId").asText();
        final String nameExtra = flowExecutionNode.get("nameExtra").asText();
        final String statusString = flowExecutionNode.get("status").asText();
        UserTaskStatus status = null;
        if (statusString.equals("active")) {
            status = UserTaskStatus.ACTIVE;
        } else if (statusString.equals("completed")) {
            status = UserTaskStatus.COMPLETED;
        }

        return new UserTask(id, name, nameExtra, processDefinitionId, taskDefinitionId, status);
    }

}
