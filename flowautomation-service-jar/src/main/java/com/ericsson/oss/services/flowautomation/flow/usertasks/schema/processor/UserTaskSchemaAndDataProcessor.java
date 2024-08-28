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

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.JSON_PROCESSING_INVALID_FIELD;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.JSON_PROCESSING_ERROR;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ARRAY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.BINDING;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.CHECKBOX;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.COLON;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DEFAULT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ENUM;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FA_READ_ONLY_USER_TASK_MAP;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FORMAT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ITEMS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.LOCAL;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.OBJECT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ONEOF;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROPERTIES;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.READONLY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SCHEMA_GEN;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TYPE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.VARIABLE;
import static java.lang.String.format;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flow.exception.InvalidPayloadException;
import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;

/**
 * The Class UserTaskSchemaAndDataProcessor that resolves the schemaGens and binds the previously submitted data in the usertask schema.
 */
@SuppressWarnings("unchecked")
public class UserTaskSchemaAndDataProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserTaskSchemaAndDataProcessor.class);

    @Inject
    @DefaultProcessEngine
    private ProcessEngine processEngine;

    public UserTaskSchemaAndDataProcessor() {
        //
    }

    /**
     * Resolves the schemaGens and binds the previously submitted data in the usertask schema.
     *
     * @param schemaMap                        the schema map
     * @param task                             the task
     * @param previousSubmittedDataForUserTask the previously submitted data for the usertask
     */
    public void processSchema(final Map<String, Object> schemaMap, final Task task, final Map<String, Object> previousSubmittedDataForUserTask) {
        final Map<String, Object> userTaskVariables = new HashMap<>();

        final Map<String, Object> schemaProperties = (Map<String, Object>) schemaMap.get(PROPERTIES);
        schemaProperties.forEach((key, value) -> processProperty(key, (Map<String, Object>) value, task, previousSubmittedDataForUserTask.get(key), userTaskVariables));

        if (!userTaskVariables.isEmpty() && (processEngine.getTaskService().getVariableLocal(task.getId(), FA_READ_ONLY_USER_TASK_MAP) == null)) {
            processEngine.getTaskService().setVariableLocal(task.getId(), FA_READ_ONLY_USER_TASK_MAP, userTaskVariables);
        }
    }

    /**
     * Process individual property in the schema.
     *
     * @param propertySchema    the property schema
     * @param task              the task
     * @param userTaskVariables
     */
    @SuppressWarnings({"squid:S3776", "squid:S1125"})
    private void processProperty(final String propertyKey, final Map<String, Object> propertySchema, final Task task, final Object data, final Map<String, Object> userTaskVariables) {

        final String type = (String) propertySchema.get(TYPE);
        final boolean readOnly = propertySchema.get(READONLY) != null ? (Boolean) propertySchema.get(READONLY) : false;

        if ((propertySchema.get(DEFAULT) != null) && readOnly) {
            userTaskVariables.put(propertyKey, propertySchema.get(DEFAULT));
        }

        if (Objects.nonNull(propertySchema.get(SCHEMA_GEN))) {
            processSchemaGen(propertyKey, propertySchema, task, data, userTaskVariables, readOnly);
            if (Objects.nonNull(data) && Objects.isNull(propertySchema.get(DEFAULT))) {
                propertySchema.put(DEFAULT, data);
            }
        }
        if (Objects.nonNull(propertySchema.get(ONEOF))) {
            if (Objects.nonNull(data)) { //set the one of selection to the last submitted value
                final Optional<Map.Entry<String, Object>> selection = ((Map<String, Object>) data).entrySet().stream().findFirst();
                selection.ifPresent(stringObjectEntry -> propertySchema.put(DEFAULT, stringObjectEntry.getKey()));
            }
            final List<Map<String, Object>> oneOfEntries = (List<Map<String, Object>>) propertySchema.get(ONEOF);
            oneOfEntries.forEach(entry -> {
                final Map<String, Object> entryProperties = (Map<String, Object>) entry.get(PROPERTIES);
                entryProperties.forEach((key, value) -> processProperty(key, (Map<String, Object>) value, task, Objects.nonNull(data) ? ((Map<String, Object>) data).get(key) : null, userTaskVariables));
            });
        } else if (type.equals(OBJECT)) {
            final String format = (String) propertySchema.get(FORMAT);
            if (Objects.equals(format, CHECKBOX)) { //this is required for checkboxes of type object
                propertySchema.put(DEFAULT, Objects.nonNull(data));
            }
            final Map<String, Object> schemaProperties = (Map<String, Object>) propertySchema.get(PROPERTIES);
            final Map<String, Object> tmpUserTaskVariables = new HashMap<>();
            userTaskVariables.put(propertyKey, tmpUserTaskVariables);

            schemaProperties.forEach((key, value) -> {
                Object previous = null;
                if (Objects.nonNull(data)) {
                    if (data instanceof FileValue) { // File case
                        previous = ((FileValue) data).getFilename();
                    } else {
                        previous = ((Map<String, Object>) data).get(key);
                    }
                }
                processProperty(key, (Map<String, Object>) value, task, previous, tmpUserTaskVariables);
            });

            if (tmpUserTaskVariables.isEmpty()) {
                userTaskVariables.remove(propertyKey);
            }
        } else if (type.equals(ARRAY)) {
            final Map<String, Object> itemsMap = (Map<String, Object>) propertySchema.get(ITEMS);
            if (Objects.nonNull(itemsMap.get(SCHEMA_GEN))) { // for lists schemagen is inside itemas.
                processSchemaGen(propertyKey, itemsMap, task, data, userTaskVariables, readOnly);
            } else {
                final Map<String, Object> properties = (Map<String, Object>) itemsMap.get(PROPERTIES);
                if (Objects.nonNull(data)) {
                    propertySchema.put(DEFAULT, data);
                }
                if (Objects.nonNull(properties)) { //just process if there is any schemagen with in properties in array items.
                    properties.forEach((key, value) -> processProperty(key, (Map<String, Object>) value, task, null, userTaskVariables));
                }
            }
        } else {
            if (Objects.nonNull(data)) {
                propertySchema.put(DEFAULT, data);
            } else {
                propertySchema.remove(DEFAULT);
            }
        }
    }

    /**
     * Process schema gen.
     *
     * @param schemaMap the schema map
     * @param task      the task
     */
    private void processSchemaGen(final String key,
                                  final Map<String, Object> schemaMap, final Task task,
                                  final Object previousSubmittedDataForUserTask,
                                  final Map<String, Object> userTaskVariables, final boolean readOnly) {
        final Object schemaGen = schemaMap.get(SCHEMA_GEN);
        if (schemaGen instanceof List) {
            final List<Map<String, Object>> schemaGenList = (List<Map<String, Object>>) schemaMap.get(SCHEMA_GEN);
            if (schemaGenList.size() <= 2) {
                final Map<String, Object> processedSchemaGenMap = new LinkedHashMap<>();
                for (final Map<String, Object> schemaGenMap : schemaGenList) {
                    processSchemaGenMap(key, schemaGenMap, task, previousSubmittedDataForUserTask, userTaskVariables, readOnly);
                    processedSchemaGenMap.putAll(schemaGenMap);
                }
                schemaMap.remove(SCHEMA_GEN);
                schemaMap.putAll(processedSchemaGenMap);
            }
        } else {
            final Map<String, Object> schemaGenMap = (Map<String, Object>) schemaMap.get(SCHEMA_GEN);
            processSchemaGenMap(key, schemaGenMap, task, previousSubmittedDataForUserTask, userTaskVariables, readOnly);
            schemaMap.remove(SCHEMA_GEN);
            schemaMap.putAll(schemaGenMap);
        }
    }

    private void processSchemaGenMap(final String key, final Map<String, Object> schemaGenMap, final Task task, final Object previousSubmittedDataForUserTask, final Map<String, Object> userTaskVariables, final boolean readOnly) {
        final String schemaGenType = (String) schemaGenMap.get(TYPE);
        String variableName = null;
        final String binding = (String) schemaGenMap.get(BINDING);
        if (binding != null) {
            final String[] split = binding.split(COLON);
            if (split.length == 3 && split[0].equals(VARIABLE) && split[1].equals(LOCAL)) {
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
        if (Objects.equals(DEFAULT, schemaGenType) && Objects.nonNull(previousSubmittedDataForUserTask)) {
            schemaGenMap.clear();
            schemaGenMap.put(DEFAULT, previousSubmittedDataForUserTask);
            return;
        }
        if (schemaGenType.equals(ENUM)) {
            if (variableValue instanceof List) {
                schemaGenMap.clear();
                schemaGenMap.put(schemaGenType, variableValue);
                schemaGenMap.put(DEFAULT, previousSubmittedDataForUserTask);
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
