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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Usertask check builder
 */
public class UsertaskCheckBuilder extends AbstractFlowInputCheckBuilder {

    /**
     * Adds a new user task input with given name and value for checking
     * @param name
     * @param value
     * @return
     */
    public UsertaskCheckBuilder check(final String name, final Object value) {
        inputs.add(new UsertaskInput(name, value, null));
        return this;
    }

    /**
     * Adds a new user task input with given name for checking
     * @param name
     * @return
     */
    public UsertaskCheckBuilder check(final String name) {
        return check(name, null);
    }

    /**
     * Checks if user tasks input has valid values based on schema
     * @param usertaskSchema
     * @throws IOException
     */
    @Override
    public void performCheck(final String usertaskSchema) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        final Map<String, Object> schemaMap = objectMapper.readValue(usertaskSchema, Map.class);

        @SuppressWarnings("unchecked")
        final Map<String, Object> schemaPropertiesMap = (Map<String, Object>) schemaMap.get("properties");

        final List<String> inputLocation = new ArrayList<>();

        checkProperties(schemaPropertiesMap, inputLocation);
    }


}
