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

package com.ericsson.oss.services.flowautomation.test.fwk;

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The type Flow input check builder.
 */
public class FlowInputCheckBuilder extends AbstractFlowInputCheckBuilder {

    public FlowInputCheckBuilder check(final String name, final Object value) {
        inputs.add(new UsertaskInput(name, value, null));
        return this;
    }

    @Override
    public void performCheck(final String flowInputSchemaAndData) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        final Map<String, Object> schemaMap = objectMapper.readValue(flowInputSchemaAndData, Map.class);

        assertNull(schemaMap.get(JsonSchemaKeywords.ACTIONS));

        @SuppressWarnings("unchecked")
        final Map<String, Object> schemaPropertiesMap = (Map<String, Object>) schemaMap.get("properties");

        final List<String> inputLocation = new ArrayList<>();

        checkProperties(schemaPropertiesMap, inputLocation);
    }
}
