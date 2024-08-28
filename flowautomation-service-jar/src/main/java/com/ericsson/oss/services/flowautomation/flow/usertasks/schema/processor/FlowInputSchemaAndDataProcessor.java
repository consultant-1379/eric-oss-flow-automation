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

package com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor;

import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ARRAY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.BOOLEAN;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.CHECKBOX;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DEFAULT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FILE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FORMAT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.INFORMATIONAL;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.INTEGER;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ITEMS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.NAME;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.NAME_EXTRA;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.NUMBER;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.OBJECT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ONEOF;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROPERTIES;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.READONLY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.STRING;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TYPE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.variable.value.FileValue;

/**
 * The type Flow input schema and data processor.
 */
public class FlowInputSchemaAndDataProcessor {


    /**
     * Process flow input variables.
     *
     * @param flowInputschemaMap the flow inputschema map
     * @param flowInputVariables the flow input variables
     */
    public void processFlowInputVariables(final Map<String, Object> flowInputschemaMap, final Map<String, Object> flowInputVariables) {
        final Map<String, Object> schemaProperties = (Map<String, Object>) flowInputschemaMap.get(PROPERTIES);
        for (final Iterator<Entry<String, Object>> it = schemaProperties.entrySet().iterator(); it.hasNext(); ) {
            final Entry<String, Object> entry = it.next();
            final Object variableObject = flowInputVariables.get(entry.getKey());
            if (variableObject == null) {
                it.remove();
            } else {
                final Map<String, Object> propertySchema = (Map<String, Object>) entry.getValue();
                final String format = (String) propertySchema.get(FORMAT);
                final boolean informational = format != null && format.equals(INFORMATIONAL);
                if (!informational) {
                    final String name = (String) propertySchema.get(NAME);
                    final String nameExtra = (String) propertySchema.get(NAME_EXTRA);
                    if (name != null && nameExtra != null) {
                        propertySchema.put(NAME, name + " - " + nameExtra);
                    }
                    updateSchemaProperty(propertySchema, variableObject);
                } else {
                    it.remove();
                }
            }
        }
    }

    /**
     * Processes both the nameExtra map and the flowInputSchema,
     * trying to find matching key pairs and, in positive case,
     * updating the schema entry with the nameExtra value found
     *
     * @param flowInputSchemaMap    the flowInputSchema map
     * @param nameExtraMap          the map containing nameExtra items
     */
    public void processFlowInputNameExtraList(final Map<String, Object> flowInputSchemaMap, final Map<String, String> nameExtraMap) {
        final Map<String, Object> schemaProperties = (Map<String, Object>) flowInputSchemaMap.get(PROPERTIES);
        for (final Iterator<Entry<String, Object>> it = schemaProperties.entrySet().iterator(); it.hasNext(); ) {
            final Entry<String, Object> entry = it.next();
            final Map<String, Object> propertySchema = (Map<String, Object>) entry.getValue();
            final String jsonTaskId = entry.getKey();
            final String nameExtra = (String) propertySchema.get(NAME_EXTRA);
            if (nameExtra == null && nameExtraMap.containsKey(jsonTaskId)) {
                propertySchema.put(NAME_EXTRA, nameExtraMap.get(jsonTaskId));
            }
        }
    }

    /**
     * Update schema property.
     *
     * @param propertySchema the property schema
     * @param variableObject the variable object
     */
    private void updateSchemaProperty(final Map<String, Object> propertySchema, final Object variableObject) {
        final String type = (String) propertySchema.get(TYPE);
        if (type.equals(OBJECT)) {
            if (propertySchema.get(ONEOF) != null) {
                updateOneOfSchemaProperty(propertySchema, variableObject);
            } else {
                updateObjectSchemaProperty(propertySchema, variableObject);
            }
        } else if (type.equals(ARRAY)) {
            updateArrayTypeSchemaProperty(propertySchema, variableObject);
        } else {
            propertySchema.put(READONLY, true);
            if (variableObject instanceof FileValue) {
                propertySchema.put(DEFAULT, ((FileValue) variableObject).getFilename());
            } else {
                propertySchema.put(DEFAULT, variableObject);
            }
        }
    }

    private void updateObjectSchemaProperty(final Map<String, Object> propertySchema, final Object variableObject) {
        final String format = (String) propertySchema.get(FORMAT);
        if (format != null && format.equals(FILE)) {
            propertySchema.put(READONLY, true);
            propertySchema.put(DEFAULT, ((FileValue) variableObject).getFilename());
        } else if (format != null && format.equals(CHECKBOX)) {
            if (variableObject == null) {
                // remove subproperties to indicate the checkbox is unchecked for this property
                propertySchema.remove(PROPERTIES);
            } else {
                updateSchemaSubproperties(propertySchema, variableObject);
            }
        } else {
            updateSchemaSubproperties(propertySchema, variableObject);
        }
    }

    private void updateOneOfSchemaProperty(final Map<String, Object> propertySchema, final Object variableObject) {
        final Entry<?, ?> variableEntry = ((Map<String, Object>) variableObject).entrySet().iterator().next();
        final String optionName = (String) variableEntry.getKey();
        final List<Object> options = (List<Object>) propertySchema.get(ONEOF);
        final Map<String, Object> optionSchemaMap = findOptionSchemaProperties(options, optionName);
        if (optionSchemaMap != null) {
            final Map<String, Object> optionPropertiesSchemaMap = (Map<String, Object>) optionSchemaMap.get(optionName);
            updateSchemaProperty(optionPropertiesSchemaMap, variableEntry.getValue());
        }
        propertySchema.put(PROPERTIES, optionSchemaMap);
        propertySchema.remove(ONEOF);
    }

    private void updateSchemaSubproperties(final Map<String, Object> propertySchema, final Object variableObject) {
        final Map<String, Object> schemaProperties = (Map<String, Object>) propertySchema.get(PROPERTIES);

        for (final Iterator<Entry<String, Object>> it = schemaProperties.entrySet().iterator(); it.hasNext(); ) {
            final Entry<String, Object> entry = it.next();
            final Object subVariableObject = ((Map<String, Object>) variableObject).get(entry.getKey());
            if (subVariableObject == null) {
                final Map<String, Object> subVariableSchema = (Map<String, Object>) entry.getValue();
                final String subVariableType = (String) subVariableSchema.get(TYPE);
                final String subVariableFormat = (String) subVariableSchema.get(FORMAT);
                if (OBJECT.equals(subVariableType) && CHECKBOX.equals(subVariableFormat)) {
                    subVariableSchema.put(TYPE, BOOLEAN);
                    subVariableSchema.put(READONLY, true);
                    subVariableSchema.put(DEFAULT, false);
                    subVariableSchema.remove(PROPERTIES);
                } else {
                    it.remove();
                }
            } else {
                final Map<String, Object> subPropertySchema = (Map<String, Object>) entry.getValue();
                updateSchemaProperty(subPropertySchema, subVariableObject);
            }
        }
    }

    /**
     * Update array schema property.
     *
     * @param propertySchema the property schema
     * @param variableObject the variable object
     */
    private void updateArrayTypeSchemaProperty(final Map<String, Object> propertySchema, final Object variableObject) {
        final Map<String, Object> items = (Map<String, Object>) propertySchema.get(ITEMS);
        final String itemsType = (String) items.get(TYPE);
        final String formatType = (String) propertySchema.get(FORMAT);
        propertySchema.put(READONLY, true);
        if (formatType != null && formatType.equals(FILE)) {
            final List<FileValue> filesAttached = (List<FileValue>) variableObject;
            final List<String> fileNames = new ArrayList<>();
            for (final FileValue file : filesAttached) {
                fileNames.add(file.getFilename());
            }
            propertySchema.put(DEFAULT, fileNames);
        } else if (itemsType.equals(INTEGER) || itemsType.equals(NUMBER) || itemsType.equals(STRING)) {
            propertySchema.put(DEFAULT, variableObject);
        } else if (itemsType.equals(OBJECT)) {
            if (variableObject instanceof ArrayList) {
                final List<Map<String, Object>> result = (ArrayList<Map<String, Object>>) variableObject;
                propertySchema.put(DEFAULT, result);
            } else {
                propertySchema.put(DEFAULT, variableObject);
            }
        }
    }

    /**
     * Find option schema properties.
     *
     * @param options    the options
     * @param optionName the option name
     * @return the map
     */
    private Map<String, Object> findOptionSchemaProperties(final List<Object> options, final String optionName) {
        Map<String, Object> optionSchemaProperties = null;
        for (final Object optionObject : options) {
            final Map<String, Object> optionProperties = (Map<String, Object>) ((Map<String, Object>) optionObject).get(PROPERTIES);
            if (optionProperties.get(optionName) != null) {
                optionSchemaProperties = optionProperties;
            }
        }
        return optionSchemaProperties;
    }
}