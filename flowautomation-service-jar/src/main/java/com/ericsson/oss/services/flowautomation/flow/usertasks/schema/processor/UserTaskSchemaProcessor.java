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


import static java.lang.String.format;

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.JSON_PROCESSING_INVALID_FIELD;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.JSON_PROCESSING_ERROR;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ARRAY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.BINDING;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.COLON;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DEFAULT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ENUM;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.EDIT_TABLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FORMAT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.INFORMATIONAL_TABLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SELECTABLE_ITEMS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SELECT_TABLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TABLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FA_READ_ONLY_USER_TASK_MAP;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DATE_TIME;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ITEMS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.LOCAL;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.OBJECT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ONEOF;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROPERTIES;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.READONLY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SCHEMA_GEN;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TYPE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.VARIABLE;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder.SchemaPropertyWrapper;
import com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flow.exception.InvalidPayloadException;
import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;

/**
 * The Class UserTaskSchemaProcessor.
 */
@SuppressWarnings("unchecked")
public class UserTaskSchemaProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserTaskSchemaProcessor.class);

    /**
     * The process engine.
     */
    @Inject
    @DefaultProcessEngine
    private ProcessEngine processEngine;
    private SchemaProcessorReport schemaProcessorReport;

    public UserTaskSchemaProcessor() {
        //
    }

    /**
     * Process schema.
     *
     * @param schemaMap the schema map
     * @param task      the task
     */
    public SchemaProcessorReport processSchema(final Map<String, Object> schemaMap, final Task task) {
        this.schemaProcessorReport = new SchemaProcessorReport(task.getName());

        Map<String, Object> userTaskVariables = new HashMap<>();
        final Map<String, Object> schemaProperties = (Map<String, Object>) schemaMap.get(PROPERTIES);
        final JsonPath jsonPath = new JsonPath("#");
        schemaProperties.forEach((key, value) -> processProperty(jsonPath.resolve(key), (Map<String, Object>) value, task, userTaskVariables));

        if (!userTaskVariables.isEmpty() && (processEngine.getTaskService().getVariableLocal(task.getId(), FA_READ_ONLY_USER_TASK_MAP) == null)) {
            processEngine.getTaskService().setVariableLocal(task.getId(), FA_READ_ONLY_USER_TASK_MAP, userTaskVariables);
        }

        return this.schemaProcessorReport;
    }

    /**
     * Process property.
     *
     * @param propertySchema the property schema
     * @param task           the task
     */
    @SuppressWarnings({"squid:S3776", "squid:S1125"})
    private void processProperty(final JsonPath jsonPath, final Map<String, Object> propertySchema, final Task task, Map<String, Object> userTaskVariables) {
        String propertyKey = jsonPath.getCurrent();
        final String type = (String) propertySchema.get(TYPE);
        final boolean readOnly = propertySchema.get(READONLY) != null ? (Boolean) propertySchema.get(READONLY) : false;

        if ((propertySchema.get(DEFAULT) != null) && readOnly) {
            userTaskVariables.put(propertyKey, propertySchema.get(DEFAULT));
        }

        if (propertySchema.get(SCHEMA_GEN) != null) {
            processSchemaGen(jsonPath, propertySchema, task, userTaskVariables, readOnly);
        }
        if (propertySchema.get(ONEOF) != null) {
            final List<Map<String, Object>> oneOfEntries = (List<Map<String, Object>>) propertySchema.get(ONEOF);
            oneOfEntries.forEach(entry -> {
                final Map<String, Object> entryProperties = (Map<String, Object>) entry.get(PROPERTIES);
                entryProperties.forEach((key, value) -> processProperty(jsonPath.resolve(key), (Map<String, Object>) value, task, userTaskVariables));
            });
        } else if (type.equals(OBJECT)) {
            final Map<String, Object> schemaProperties = (Map<String, Object>) propertySchema.get(PROPERTIES);
            Map<String, Object> tmpUserTaskVariables = new HashMap<>();
            userTaskVariables.put(propertyKey, tmpUserTaskVariables);
            schemaProperties.forEach((key, value) ->
                    processProperty(jsonPath.resolve(key), (Map<String, Object>) value, task, tmpUserTaskVariables)
            );
            if (tmpUserTaskVariables.isEmpty()) {
                userTaskVariables.remove(propertyKey);
            }
        } else if (type.equals(ARRAY)) {
            final Map<String, Object> itemsMap = (Map<String, Object>) propertySchema.get(ITEMS);
            if (itemsMap.get(SCHEMA_GEN) != null) {
                processSchemaGen(jsonPath, itemsMap, task, userTaskVariables, readOnly);
            }

            final Map<String, Object> properties = (Map<String, Object>) itemsMap.get(PROPERTIES);
            if (properties != null) {
                properties.forEach((key, value) -> processProperty(jsonPath.resolve(key), (Map<String, Object>) value, task, userTaskVariables));
            }
        } else {
            if (propertySchema.get(SCHEMA_GEN) != null) {
                processSchemaGen(jsonPath, propertySchema, task, userTaskVariables, readOnly);
            }
        }
    }

    /**
     * Process schema gen.
     *
     * @param schemaMap the schema map
     * @param task      the task
     */
    private void processSchemaGen(final JsonPath jsonPath, final Map<String, Object> schemaMap, final Task task, Map<String, Object> userTaskVariables, boolean readOnly) {
        String key = jsonPath.getCurrent();
        final Object schemaGen = schemaMap.get(SCHEMA_GEN);
        if (schemaGen instanceof List) {
            List<Map<String, Object>> schemaGenList = (List<Map<String, Object>>) schemaMap.get(SCHEMA_GEN);
            /*The schemaGenList for selectable table should contain 2 objects.
            * 1. "type" : "default"
            * 2. "type" : "selectableItems"
            * Refer UserTaskShowCase flow for more details.
            * */
            if (schemaGenList.size() <= 2) {
                Map<String, Object> processedSchemaGenMap = new LinkedHashMap<>();
                for (Map<String, Object> schemaGenMap : schemaGenList) {
                    processSchemaGenMap(key, schemaGenMap, task, userTaskVariables, readOnly);
                    processedSchemaGenMap.putAll(schemaGenMap);
                }
                schemaMap.remove(SCHEMA_GEN);
                schemaMap.putAll(processedSchemaGenMap);
            }
        } else {
             Map<String, Object> schemaGenMap = (Map<String, Object>) schemaMap.get(SCHEMA_GEN);
            processSchemaGenMap(key, schemaGenMap, task, userTaskVariables, readOnly);
            schemaMap.remove(SCHEMA_GEN);
            schemaMap.putAll(schemaGenMap);
        }

        checkDateTimeFormat(jsonPath, schemaMap);
    }

    private void checkDateTimeFormat(final JsonPath path, final Map<String, Object> schemaMap) {
        final String format = (String) schemaMap.get(FORMAT);
        if (DATE_TIME.equals(format)) {
            validateDateTime(path, schemaMap);
        } else if (Arrays.asList(TABLE, INFORMATIONAL_TABLE, SELECT_TABLE, EDIT_TABLE).contains(format)) {
            validateTables(path, schemaMap);
        }
    }

    private void validateDateTime(final JsonPath path, final Map<String, Object> schemaMap) {
        final String dateTimeValue = (String) schemaMap.get(DEFAULT);
        try {
            if (dateTimeValue != null) {
                LocalDateTime.parse(dateTimeValue, DateTimeUtil.FA_DATE_TIME_FORMATTER);
            }
        } catch(DateTimeParseException e) {
            logDateTimeValidation(path, dateTimeValue, schemaMap);
        }
    }

    private void validateTables(final JsonPath path, final Map<String, Object> schemaMap) {
        final SchemaPropertyWrapper schemaWrapper = new SchemaPropertyWrapper(path.getCurrent(), schemaMap);
        final SchemaPropertyWrapper itemsProperties = schemaWrapper.getPropertyWrapper(ITEMS).getPropertyWrapper(PROPERTIES);

        final String format = (String) schemaMap.get(FORMAT);
        final List<Map<String, Object>> tableData;
        if (SELECT_TABLE.equals(format)) {
            tableData = schemaWrapper.getProperty(SELECTABLE_ITEMS);
        } else {
            tableData = schemaWrapper.getProperty(DEFAULT);
        }

        for (int i = 0; i < tableData.size(); ++i) {
            Map<String, Object> row = tableData.get(i);
            JsonPath rowPath = path.resolve(String.valueOf(i));
            row.forEach((key, value) -> {
                JsonPath cellPath = rowPath.resolve(key);
                SchemaPropertyWrapper columnSchema = itemsProperties.getPropertyWrapper(key);
                String columnFormat = columnSchema.getProperty(FORMAT);
                String dateTimeValue = String.valueOf(value);
                try {
                    if (DATE_TIME.equals(columnFormat)) {
                        LocalDateTime.parse(dateTimeValue, DateTimeUtil.FA_DATE_TIME_FORMATTER);
                    }
                } catch(DateTimeParseException e) {
                    logDateTimeValidation(cellPath, dateTimeValue, schemaMap);
                }
            });
        }
    }

    private void logDateTimeValidation(final JsonPath jsonPath, final String dateTimeValue, final Map<String, Object> schemaMap) {
        final String synopsis = String.format("String %s is invalid against accepted date format(s) [yyyy-MM-dd'T'HH:mm:ss.SSS'Z']",
                dateTimeValue);

        final String details = String.format("SCHEMA PATH: %s%nSCHEMA PROPERTY: %s", jsonPath.toString(), ObjectUtils.toString(schemaMap));
        schemaProcessorReport.addError(jsonPath, synopsis, details);
    }

    private void processSchemaGenMap(String key, Map<String, Object> schemaGenMap, final Task task, Map<String, Object> userTaskVariables, boolean readOnly) {
        final String schemaGenType = (String) schemaGenMap.get(TYPE);
        final String binding = (String) schemaGenMap.get(BINDING);
        String variableName = null;
        if (binding != null) {
            final String[] split = binding.split(COLON);
            if (split != null && split.length == 3 && split[0].equals(VARIABLE) && split[1].equals(LOCAL)) {
                variableName = split[2];
            } else {
                LOGGER.error("Invalid binding syntax: {}", binding);
                return;
            }
        }

        final Object variableValue = getLocalVariable(variableName, task);

        if (readOnly) {
            userTaskVariables.put(key, variableValue);
        }
        if (schemaGenType.equals(ENUM)) {
            if (variableValue instanceof List) {
                schemaGenMap.clear();
                schemaGenMap.put(schemaGenType, variableValue);
            } else {
                final String error = format(JSON_PROCESSING_INVALID_FIELD, variableName);
                LOGGER.error(error);
                throw new InvalidPayloadException(JSON_PROCESSING_ERROR, error);
            }
        } else {
            schemaGenMap.clear();
            schemaGenMap.put(schemaGenType, variableValue);
        }
    }

    /**
     * Gets the local variable.
     *
     * @param variableName the variable name
     * @param task         the task
     * @return the local variable
     */
    private Object getLocalVariable(final String variableName, final Task task) {
        if (variableName != null) {
            return processEngine.getRuntimeService().getVariableLocal(task.getExecutionId(), variableName);
        }

        return null;
    }
}
