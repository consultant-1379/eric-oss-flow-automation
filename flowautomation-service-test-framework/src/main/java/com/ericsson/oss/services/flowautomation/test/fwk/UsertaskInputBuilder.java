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

import static java.util.Arrays.asList;

import static org.junit.Assert.fail;

import static com.ericsson.oss.services.flowautomation.test.fwk.BasicUtils.copyList;
import static com.ericsson.oss.services.flowautomation.test.fwk.UsertaskUtils.findInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Builds usertask input
 */

public class UsertaskInputBuilder {

    private List<UsertaskInput> inputs = new ArrayList<>();
    private Map<String, byte[]> fileInput = new HashMap<>();

    /**
     * Adds a new user task input
     * @param name
     * @param value
     * @return
     */
    public UsertaskInputBuilder input(final String name, final Object value) {
        inputs.add(new UsertaskInput(name, value, null));
        return this;
    }

    /**
     * Adds a new user task input
     * @param name
     * @param value
     * @param extraValue
     * @return
     */
    public UsertaskInputBuilder input(final String name, final Object value, final Object extraValue) {
        inputs.add(new UsertaskInput(name, value, extraValue));
        return this;
    }

    /**
     * Builds user task input json string
     * @param usertaskSchema
     * @return
     * @throws Exception
     */
    public String buildInputString(final String usertaskSchema) throws Exception {
        String inputString = null;

        // build json by traversing schema and inputs
        final ObjectMapper objectMapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        final Map<String,Object> schemaMap = objectMapper.readValue(usertaskSchema, Map.class);

        @SuppressWarnings("unchecked")
        final Map<String, Object> schemaPropertiesMap = (Map<String, Object>)schemaMap.get("properties");

        final Map<String, Object> inputMap = new LinkedHashMap<>();
        final List<String> inputLocation = new ArrayList<>();

        createProperties(schemaPropertiesMap, inputMap, inputLocation);

        final ObjectMapper jsonWriter = new ObjectMapper();
        jsonWriter.configure(SerializationFeature.INDENT_OUTPUT, true);
        inputString = jsonWriter.writeValueAsString(inputMap);

        return inputString;
    }

    /**
     * Gets file input
     * @return
     */
    public Map<String, byte[]> getFileInput() {
        return fileInput;
    }

    private void createProperties(final Map<String, Object> schemaPropertiesMap, final Map<String, Object> inputMap, final List<String> inputLocation) throws Exception {
        for (final Map.Entry<?, ?> entry : schemaPropertiesMap.entrySet()) {
            final String subPropertyName = (String) entry.getKey();

            @SuppressWarnings("unchecked") final Map<String, Object> subPropertyValueMap = (Map<String, Object>) entry.getValue();

            String name = (String) subPropertyValueMap.get("name");
            final String nameExtra = (String) subPropertyValueMap.get("nameExtra");
            if (nameExtra != null && !nameExtra.isEmpty() && !name.endsWith(" - " + nameExtra)) {
                name += " - " + nameExtra;
            }

            final String type = (String) subPropertyValueMap.get("type");
            final String format = (String) subPropertyValueMap.get("format");
            if (type.equals("object")) {
                createObjectProperties(inputMap, inputLocation, subPropertyName, subPropertyValueMap, name, format);
            } else if ("list".equals(format)) {
                createListProperties(inputMap, inputLocation, subPropertyName, name);
            } else if (format != null && (format.equals("select") || format.equals("select-list"))) {
                createSelectProperties(inputMap, inputLocation, subPropertyName, subPropertyValueMap, name);
            } else {
                final UsertaskInput input = UsertaskUtils.findInput(inputs, inputLocation, name);
                if (input != null) {
                    inputMap.put(subPropertyName, input.getValue());
                } else {
                    createSimplePropertyWithValue(inputMap, inputLocation, null, subPropertyName, subPropertyValueMap);
                }
            }
        }
    }

    private void createListProperties(final Map<String, Object> inputMap, final List<String> inputLocation,
                                      final String propertyName, String name) throws Exception {
        final UsertaskInput input = findInput(inputs, inputLocation, name);
        if (input != null) {
            List<String> items = null;
            if (input.getValue() instanceof List) {
                items = (List<String>) input.getValue();
            } else if (input.getValue() instanceof String[]) {
                items = Arrays.asList((String[]) input.getValue());
            } else if (input.getValue() instanceof String) {
                items = Arrays.asList(((String) (input.getValue())).split(","));
            } else {
                throw new Exception("Invalid items at location " + inputLocation.toString());
            }
            inputMap.put(propertyName, items);
        }
    }

    private void createSelectProperties(final Map<String, Object> inputMap, final List<String> inputLocation,
            final String propertyName, final Map<String, Object> propertySchemaMap, final String name) {

        final UsertaskInput input = UsertaskUtils.findInput(inputs, inputLocation, name);
        if (input != null) {
            final String enumNameValue = (String)input.getValue();
            final String enumValue = getEnumForEnumName(propertySchemaMap, enumNameValue);
            inputMap.put(propertyName, enumValue);
        }
    }

    private String getEnumForEnumName(Map<String, Object> propertySchemaMap, String enumName) {
        String name = enumName;

        final List<String> enums = (List<String>)propertySchemaMap.get("enum");
        final List<String> enumNames = (List<String>)propertySchemaMap.get("enumNames");
        if (enumNames != null) {
            for (int i = 0; i < enums.size(); i++) {
                if (enumNames.get(i).equals(enumName)) {
                    name = enums.get(i);
                    break;
                }
            }
        }
        else {
            for (int i = 0; i < enums.size(); i++) {
                if (enums.get(i).equals(enumName)) {
                    name = enums.get(i);
                    break;
                }
            }
        }

        return name;
    }

    @SuppressWarnings({ "squid:S00112" })
    private void createObjectProperties(final Map<String, Object> inputMap, final List<String> inputLocation,
            final String propertyName, final Map<String, Object> propertyValueMap, final String name,
            final String format) throws Exception {
        final List<String> inputLocationX = copyList(inputLocation);
        inputLocationX.add(name);

        final Map<String, Object> inputPropertyMap = new LinkedHashMap<>();

        if (propertyValueMap.get("oneOf") != null) {
            inputMap.put(propertyName, inputPropertyMap);
            createOneOfObjectProperties(propertyValueMap, inputPropertyMap, inputLocationX);
        }
        else if (format != null && format.equals("file")) {
            final UsertaskInput input = findInput(inputs, inputLocation, name);
            if (input == null) {
                throw new Exception("Missing input " + name + " at location " + inputLocation.toString());
            }

            final Map<String, Object> filePropertyMap = new LinkedHashMap<>();
            filePropertyMap.put("fileName", input.getValue());
            final Object extraValue = input.getExtraValue();
            if (extraValue != null) {
                fileInput.put((String) input.getValue(), (byte[]) extraValue);
            }

            inputMap.put(propertyName, filePropertyMap);
        }
        else if (format != null && format.equals("checkbox")) {
            final UsertaskInput input = findInput(inputs, inputLocation, name);
            if (input != null) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> schemaPropertiesMap = (Map<String, Object>)propertyValueMap.get("properties");

                createProperties(schemaPropertiesMap, inputPropertyMap, inputLocationX);
                inputMap.put(propertyName, inputPropertyMap);
            }
        }
        else {
            inputMap.put(propertyName, inputPropertyMap);

            @SuppressWarnings("unchecked")
            final Map<String, Object> schemaPropertiesMap = (Map<String, Object>)propertyValueMap.get("properties");

            createProperties(schemaPropertiesMap, inputPropertyMap, inputLocationX);
        }
    }

    @SuppressWarnings({ "squid:S00112" })
    private void createOneOfObjectProperties(final Map<String, Object> propertyValueMap,
                                             final Map<String, Object> inputPropertyMap, final List<String> inputLocationX) throws Exception {

        @SuppressWarnings("unchecked")
        final List<String> optionNames = getOneOfOptionNames((List<Object>)propertyValueMap.get("oneOf"));

        boolean optionChoosen = false;
        for (final String optionName : optionNames) {
            final UsertaskInput input = findInput(inputs, inputLocationX, optionName);
            if (input != null) {
                optionChoosen = true;

                @SuppressWarnings("unchecked")
                final Map<String, Object> option = getOneOfOptionByName((List<Object>)propertyValueMap.get("oneOf"), optionName);
                if (option == null) {
                    throw new Exception("Missing oneOf option");
                }

                createOneOfObjectProperty(inputPropertyMap, inputLocationX, option, input.getValue());

                break;
            }
        }

        if (!optionChoosen) {
            final String optionPropertyName = (String)propertyValueMap.get("default");
            if (optionPropertyName != null) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> option = getOneOfOptionByPropertyName((List<Object>)propertyValueMap.get("oneOf"), optionPropertyName);
                if (option == null) {
                    throw new Exception("Missing oneOf option");
                }

                createOneOfObjectProperty(inputPropertyMap, inputLocationX, option, null);
            }
        }
    }

    private void createOneOfObjectProperty(final Map<String, Object> inputPropertyMap,
            final List<String> inputLocationX, final Map<String, Object> option, final Object inputValue) throws Exception {

        @SuppressWarnings("unchecked")
        final Map<String, Object> optionProperties = (Map<String, Object>)option.get("properties");

        final Map.Entry<String, Object> optionEntry = optionProperties.entrySet().iterator().next();
        final String optionPropertyName = optionEntry.getKey();

        @SuppressWarnings("unchecked")
        final Map<String, Object> actualOption = (Map<String, Object>)optionEntry.getValue();

        final String optionType = (String)actualOption.get("type");
        if (optionType.equals("object")) {
            String name = (String) actualOption.get("name");
            String format = (String) actualOption.get("format");

            if (format != null && format.equals("file")) {
                createObjectProperties(inputPropertyMap, inputLocationX, optionPropertyName, actualOption, name, format);
            } else {
                final Map<String, Object> optionMap = new LinkedHashMap<>();
                inputPropertyMap.put(optionPropertyName, optionMap);

                inputLocationX.add((String) actualOption.get("name"));

                @SuppressWarnings("unchecked") final Map<String, Object> schemaSubPropertiesMap = (Map<String, Object>) actualOption.get("properties");

                createProperties(schemaSubPropertiesMap, optionMap, inputLocationX);
            }
        }
        else {
            createSimplePropertyWithValue(inputPropertyMap, inputLocationX, inputValue, optionPropertyName, actualOption);
        }
    }

    private void createSimplePropertyWithValue(final Map<String, Object> inputPropertyMap, final List<String> inputLocationX,
                                               final Object inputValue, final String optionPropertyName, final Map<String, Object> option) {

        final String format = (String) option.get("format");
        if (format != null && format.startsWith("message-")) {
            return;
        }
        if (asList("informational", "informational-list").contains(format)) {
            return;
        }

        final String type = (String) option.get("type");

        if (inputValue != null) {
            inputPropertyMap.put(optionPropertyName, inputValue);
        } else {
            createSimplePropertyWithDefaultValue(inputPropertyMap, inputLocationX, optionPropertyName, option, type);
        }
    }

    private void createSimplePropertyWithDefaultValue(final Map<String, Object> inputPropertyMap, final List<String> inputLocationX,
            final String optionPropertyName, final Map<String, Object> option, final String type) {
        Object value = null;
        final Object defaultValue = option.get("default");
        if (type.equals("boolean")) {
            value = false;
            if (defaultValue != null && (Boolean)defaultValue) {
                value = true;
            }
        }
        else if (type.equals("integer")) {
            if (defaultValue != null) {
                value = defaultValue;
            }
        }
        else {
            if (defaultValue != null) {
                value = defaultValue;
            }
        }

        if (value == null) {
            fail("Value not supplied for " + inputLocationX);
        }

        inputPropertyMap.put(optionPropertyName, value);
    }

    private List<String> getOneOfOptionNames(final List<Object> schemaOptions) {
        final List<String> options = new ArrayList<>();

        for (final Object option : schemaOptions) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> schemaOption = (Map<String, Object>)option;

            @SuppressWarnings("unchecked")
            final Map<String, Object> optionProperties = (Map<String, Object>)((Map<String, Object>)schemaOption.get("properties")).entrySet().iterator().next().getValue();

            final String optionName = (String)optionProperties.get("name");
            options.add(optionName);
        }

        return options;
    }

    private Map<String, Object> getOneOfOptionByName(final List<Object> schemaOptions, final String optionName) {
        Map<String, Object> schemaOption = null;

        for (final Object option : schemaOptions) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> schemaOptionX = (Map<String, Object>)option;

            @SuppressWarnings("unchecked")
            final Map<String, Object> optionProperties = (Map<String, Object>)((Map<String, Object>)schemaOptionX.get("properties")).entrySet().iterator().next().getValue();

            final String optionXName = (String)optionProperties.get("name");
            if (optionXName.equals(optionName)) {
                schemaOption = schemaOptionX;
                break;
            }
        }

        return schemaOption;
    }

    private Map<String, Object> getOneOfOptionByPropertyName(final List<Object> schemaOptions, final String optionPropertyName) {
        Map<String, Object> schemaOption = null;

        for (final Object option : schemaOptions) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> schemaOptionX = (Map<String, Object>)option;

            @SuppressWarnings("unchecked")
            final Map<String, Object> optionProperties = (Map<String, Object>)((Map<String, Object>)schemaOptionX.get("properties")).get(optionPropertyName);
            if (optionProperties != null) {
                schemaOption = schemaOptionX;
                break;
            }
        }

        return schemaOption;
    }

}
