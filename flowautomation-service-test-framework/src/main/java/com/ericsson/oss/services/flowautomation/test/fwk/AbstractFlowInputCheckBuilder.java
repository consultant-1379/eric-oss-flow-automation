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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static com.ericsson.oss.services.flowautomation.test.fwk.BasicUtils.copyList;
import static com.ericsson.oss.services.flowautomation.test.fwk.UsertaskUtils.findInput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * The type Abstract flow input check builder.
 */
public abstract class AbstractFlowInputCheckBuilder {

    protected final List<UsertaskInput> inputs = new ArrayList<>();

    public abstract void performCheck(final String schema) throws IOException;

    protected void checkProperties(final Map<String, Object> schemaPropertiesMap, final List<String> inputLocation) {
        for (final Map.Entry<?, ?> entry : schemaPropertiesMap.entrySet()) {
            @SuppressWarnings("unchecked") final Map<String, Object> subPropertyValueMap = (Map<String, Object>) entry.getValue();

            final String name = (String) subPropertyValueMap.get("name");
            final String type = (String) subPropertyValueMap.get("type");
            final String format = (String) subPropertyValueMap.get("format");
            if (type.equals("object")) {
                checkObjectProperties(inputLocation, subPropertyValueMap, name, format);
            } else {
                final UsertaskInput input = findInput(inputs, inputLocation, name);
                if (input != null) {
                    checkSimpleProperty(name, inputLocation, subPropertyValueMap, input, format);
                }
            }
        }
    }

    protected void checkObjectProperties(final List<String> inputLocation,
                                         final Map<String, Object> propertyMap, final String name, final String format) {

        final List<String> inputLocationX = copyList(inputLocation);
        inputLocationX.add(name);

        if (propertyMap.get("oneOf") != null) {
            checkOneOfProperty(inputLocationX, (List<Map<String, Object>>) propertyMap.get("oneOf"));
        } else if (format != null && format.equals("file")) {
            checkFileInputProperty(name, inputLocation, propertyMap);
        } else {
            @SuppressWarnings("unchecked") final Map<String, Object> schemaPropertiesMap = (Map<String, Object>) propertyMap.get("properties");

            checkProperties(schemaPropertiesMap, inputLocationX);
        }
    }

    private void checkOneOfProperty(final List<String> inputLocation, final List<Map<String, Object>> oneOfProperty) {
        for (Map<String, Object> stringObjectMap : oneOfProperty) {
            Map<String, Object> properties = (Map<String, Object>) stringObjectMap.get("properties");
            Map<String, Object> option = (Map<String, Object>) properties.entrySet().iterator().next().getValue();

            Object defaultProperty = option.get("default");
            if (defaultProperty != null) {
                String name = (String) option.get("name");

                final UsertaskInput input = findInput(inputs, inputLocation, name);
                assertNotNull("[" + name + "] is unexpected at location " + inputLocation, input);
                String format = (String) option.get("format");
                checkSimpleProperty(name, inputLocation, option, input, format);
                break;
            }
        }
    }

    /**
     * Verifies the file input name.
     * There is no need to check for the bytes value. If it is missing, the user task won't complete successfully.
     * @param name
     * @param inputLocation
     * @param propertyMap
     */
    private void checkFileInputProperty(final String name, final List<String> inputLocation, final Map<String, Object> propertyMap) {
        final UsertaskInput input = findInput(inputs, inputLocation, name);
        List<String> fileLocation = copyList(inputLocation);
        fileLocation.add(name);
        fileLocation.add("File Name");
        if (input != null) {
            Object defaultValue = propertyMap.get("default");
            if (defaultValue == null) {
                Map<String, Object> fileProperties = (Map<String, Object>) propertyMap.get("properties");
                if (fileProperties != null) {
                    Map<String, Object> fileNameProperties = (Map<String, Object>) fileProperties.get("fileName");
                    if (fileNameProperties != null) {
                        defaultValue = fileNameProperties.get("default");
                    }
                }
            }
            assertEquals("File name mismatch at location "+ fileLocation, input.getValue(), defaultValue);
        }
    }

    protected void checkSimpleProperty(final String name, final List<String> inputLocation, final Map<String, Object> propertyMap,
                                       final UsertaskInput input, final String format) {
        final String type = (String) propertyMap.get("type");
        final Object defaultValue = propertyMap.get("default");
        final String errorMsg = "[" + name + "] should match at location " + inputLocation;

        if (defaultValue != null) {
            if (type.equals("integer")) {
                assertEquals(errorMsg, input.getValue(), defaultValue);
            } else if (type.equals("boolean")) {
                assertEquals(errorMsg, input.getValue(), defaultValue);
            } else if (type.equals("string")) {
                if (format != null && format.equals("select")) {
                    final String value = getEnumNameForEnum(propertyMap, (String) defaultValue);
                    assertEquals(errorMsg, input.getValue(), value);
                } else {
                    assertEquals(errorMsg, input.getValue(), defaultValue);
                }
            } else if (type.equals("array") && ("email".equals(format) || "list".equals(format))) {
                List<String> result;
                if (input.getValue() instanceof List) {
                    result = (List<String>) input.getValue();
                } else {
                    result = Arrays.asList((String[]) input.getValue());
                }
                assertEquals(errorMsg, result, defaultValue);
            } else if (type.equals("array")) {
                final List<Map<String, Object>> result = ((ArrayList<Map<String, Object>>)input.getValue()) ;
                assertEquals(errorMsg, result, defaultValue);
            } else {
                fail("Unexpected type (" + type + ") for " + name + " at location " + inputLocation);
            }
        }
    }

    protected String getEnumNameForEnum(final Map<String, Object> propertySchema, final String value) {
        String name = value;

        @SuppressWarnings("unchecked") final List<String> enums = (List<String>) propertySchema.get("enum");

        @SuppressWarnings("unchecked") final List<String> enumNames = (List<String>) propertySchema.get("enumNames");

        if (enums != null && enumNames != null) {
            for (int i = 0; i < enums.size(); i++) {
                if (enums.get(i).equals(value)) {
                    name = enumNames.get(i);
                    break;
                }
            }
        }

        return name;
    }
}
