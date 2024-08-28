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

import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class FlowExecutionDeserializer extends StdDeserializer<FlowExecution> {

    private static final long serialVersionUID = 1L;

    public FlowExecutionDeserializer() {
        this(null);
    }

    public FlowExecutionDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public FlowExecution deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        final JsonNode flowExecutionNode = jp.getCodec().readTree(jp);
        final String name = flowExecutionNode.get("name").asText();
        final String flowId = flowExecutionNode.get("flowId").asText();
        final String flowName = flowExecutionNode.get("flowName").asText();
        final String flowVersion = flowExecutionNode.get("flowVersion").asText();
        final String createdBy = flowExecutionNode.get("createdBy").asText();
        final String executedBy = flowExecutionNode.get("executedBy").asText();
        final String startTime = flowExecutionNode.get("startTime").asText();
        final String endTime = flowExecutionNode.get("endTime").asText().equals("null") ? null : flowExecutionNode.get("endTime").asText();
        final String duration = flowExecutionNode.get("duration").asText().equals("null") ? null : flowExecutionNode.get("duration").asText();
        final String state = flowExecutionNode.get("state").asText();
        final String summaryReport = flowExecutionNode.get("summaryReport").asText();
        final Long numberActiveUserTasks = flowExecutionNode.get("numberActiveUserTasks").asLong();
        final String source = flowExecutionNode.get("flowSource").asText();
        final String processInstanceId = flowExecutionNode.get("processInstanceId").asText();
        final String processInstanceBusinessKey = flowExecutionNode.get("processInstanceBusinessKey").asText();

        final FlowExecution flowExecution = new FlowExecution(name, flowId, flowName, flowVersion, createdBy, executedBy, startTime, endTime,
                duration, summaryReport, state, numberActiveUserTasks, source, processInstanceId, processInstanceBusinessKey);
        flowExecution.setSummaryReport(flowExecutionNode.get("summaryReport").asText());

        return flowExecution;
    }
}
