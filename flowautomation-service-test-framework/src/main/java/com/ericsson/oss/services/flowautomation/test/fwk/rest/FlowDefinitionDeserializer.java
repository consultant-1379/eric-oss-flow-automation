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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowVersion;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class FlowDefinitionDeserializer extends StdDeserializer<FlowDefinition> {

    private static final long serialVersionUID = 1L;

    public FlowDefinitionDeserializer() {
        this(null);
    }

    public FlowDefinitionDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public FlowDefinition deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        final JsonNode flowDefinitionNode = jp.getCodec().readTree(jp);
        final String id = flowDefinitionNode.get("id").textValue();
        final String name = flowDefinitionNode.get("name").textValue();
        final String status = flowDefinitionNode.get("status").textValue();
        final String source = flowDefinitionNode.get("source").textValue();
        final List<FlowVersion> flowVersions = deserializeFlowVersions(flowDefinitionNode.get("flowVersions"));

        return new FlowDefinition(id, name, status, source, flowVersions);
    }

    private List<FlowVersion> deserializeFlowVersions(final JsonNode flowVersionsNode) {
        final List<FlowVersion> flowVersions = new ArrayList<>();

        for (final JsonNode flowVersionNode : flowVersionsNode) {
            final String version = flowVersionNode.get("version").asText();
            final String description = flowVersionNode.get("description").asText();
            final boolean active = flowVersionNode.get("active").booleanValue();
            final String createdBy = flowVersionNode.get("createdBy").asText();
            final String createdDate = flowVersionNode.get("createdDate").asText();
            final boolean setupPhaseRequired = flowVersionNode.get("setupPhaseRequired").booleanValue();
            final boolean reportSupported = flowVersionNode.get("reportSupported").booleanValue();
            flowVersions.add(new FlowVersion(version, description, active, createdBy, createdDate, setupPhaseRequired, reportSupported));
        }

        return flowVersions.stream().sorted(Comparator.comparing(FlowVersion::getVersion)).collect(Collectors.toList());
    }

}
