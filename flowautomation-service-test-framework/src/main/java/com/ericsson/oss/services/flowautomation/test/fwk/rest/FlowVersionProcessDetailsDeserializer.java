/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

import com.ericsson.oss.services.flowautomation.model.FlowVersionProcessDetails;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class FlowVersionProcessDetailsDeserializer extends StdDeserializer<FlowVersionProcessDetails> {

    private static final long serialVersionUID = 1L;

    public FlowVersionProcessDetailsDeserializer() {
        this(null);
    }

    public FlowVersionProcessDetailsDeserializer(final Class<?> vc) {
        super(vc);
    }

    public FlowVersionProcessDetails deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        final JsonNode flowExecutionNode = jp.getCodec().readTree(jp);
        final String processId = flowExecutionNode.get("processId").asText();
        final String setupProcessId = flowExecutionNode.get("setupProcessId").asText();
        final String executeProcessId = flowExecutionNode.get("executeProcessId").asText();

        return new FlowVersionProcessDetails(processId, setupProcessId, executeProcessId);
    }
}