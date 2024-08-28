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

package com.ericsson.oss.services.flowautomation.flow.utils;

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FILE_ATTACHMENT_IS_MISSING;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.INVALID_FILE_NAME_FOUND_IN_PAYLOAD;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.JSON_PROCESSING_ERROR_OCCURED;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.JSON_SCHEMA_IS_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FILE_ATTACHMENT_MISSING;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_FILE_NAME;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_FLOW_INPUT_VARIABLE_MAP;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_JSON_PAYLOAD;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.JSON_PROCESSING_ERROR;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.JSON_SCHEMA_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.JSON_VALIDATION_FAILED;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ARRAY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DATE_TIME;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.EDIT_TABLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FILE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FILE_NAME;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FORMAT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ITEMS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.OBJECT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ONEOF;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.POINTER;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROPERTIES;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.RADIO;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.STRING;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SCHEMA_TEXT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TYPE;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static java.lang.String.format;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flow.exception.InvalidPayloadException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

/**
 * The Class PayloadTransformer.
 */
@SuppressWarnings("squid:S2629")
public class PayloadTransformer {

    /** The Constant objectMapper. */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** The Constant jsonSchemaFactory. */
    private static final JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.byDefault();

    private static final Logger logger = LoggerFactory.getLogger(PayloadTransformer.class);

    private PayloadTransformer() {
        //
    }

    /**
     * Transform map to json payload by validating it against the schema.
     *
     * @param jsonSchema  the json schema
     * @param variableMap the variable map
     * @return the string
     */
    public static String transformMapToJsonPayload(final String jsonSchema, final Map<String, Object> variableMap) {
        try {
            logger.debug("Variables Map: {}", variableMap);
            if (StringUtils.isEmpty(jsonSchema)) {
                logger.error(JSON_SCHEMA_NOT_FOUND.reason());
                throw new InvalidPayloadException(JSON_SCHEMA_NOT_FOUND, JSON_SCHEMA_IS_NOT_FOUND);
            }
            if (MapUtils.isEmpty(variableMap)) {
                logger.error(INVALID_FLOW_INPUT_VARIABLE_MAP.reason());
                throw new InvalidPayloadException(INVALID_FLOW_INPUT_VARIABLE_MAP);
            }

            final String jsonPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(variableMap);
            validateJsonPayload(jsonSchema, jsonPayload);
            return jsonPayload;
        } catch (final JsonProcessingException e) {
            logger.error("transformMapToJsonPayload() Failed to process: {}. Exception: {}", JSON_PROCESSING_ERROR.reason(), e.getMessage());
            throw new InvalidPayloadException(JSON_PROCESSING_ERROR, JSON_PROCESSING_ERROR_OCCURED + ": " + e.getMessage());
        }

    }

    /**
     * Transform map to json payload without validations.
     *
     * @param input the input
     * @return the string
     * @throws JsonProcessingException the json processing exception
     */
    public static String transformMapToJsonPayload(final Map<String, Object> input) throws JsonProcessingException {
        final ObjectMapper jsonWriter = new ObjectMapper();
        jsonWriter.configure(INDENT_OUTPUT, true);
        return jsonWriter.writeValueAsString(input);
    }

    /**
     * Transform json payload to map.
     *
     * @param jsonSchema  the json schema
     * @param jsonPayload the json payload
     * @return the map
     */
    public static Map<String, Object> transformJsonPayloadToMap(final String jsonSchema, final String jsonPayload) {
        if (StringUtils.isEmpty(jsonSchema)) {
            logger.error(JSON_SCHEMA_NOT_FOUND.reason());
            throw new InvalidPayloadException(JSON_SCHEMA_NOT_FOUND, JSON_SCHEMA_IS_NOT_FOUND);
        }
        if (StringUtils.isEmpty(jsonPayload)) {
            logger.error(INVALID_JSON_PAYLOAD.reason());
            throw new InvalidPayloadException(INVALID_JSON_PAYLOAD);
        }
        try {
            logger.debug("Validating json payload: {}", jsonPayload);
            validateJsonPayload(jsonSchema, jsonPayload);
            return transformJsonToMap(jsonPayload);
        } catch (final IOException e) {
            logger.error("transformJsonPayloadToMap() Failed to process: {}. Exception: {}", JSON_PROCESSING_ERROR.reason(), e.getMessage());
            throw new InvalidPayloadException(JSON_PROCESSING_ERROR, JSON_PROCESSING_ERROR_OCCURED + ". JsonPayload: " + jsonPayload + " " + e.getMessage());
        }
    }

    /**
     * Transform json payload to map.
     *
     * @param jsonPayload the json payload
     * @return the map
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Map<String, Object> transformJsonToMap(final String jsonPayload) throws IOException {
        return objectMapper.readValue(jsonPayload, new TypeReference<LinkedHashMap<String, Object>>() {
        });
    }

    /**
     * Transform json payload to map.
     *
     * @param jsonSchema        the json schema
     * @param jsonPayload       the json payload
     * @param userTaskFileInput the user task file input
     * @return the map
     */
    public static Map<String, Object> transformJsonPayloadToMap(final String jsonSchema, final String jsonPayload,
                                                                final Map<String, byte[]> userTaskFileInput) {
        return transformJsonPayloadToMap(jsonSchema, jsonPayload, userTaskFileInput, Collections.emptyMap());

    }

    /**
     * Transform json payload to map while processing through the schema, the usertask payload, attached files and any previously uploaded files.
     * The previous uploaded files are only considered if the user task contains a file upload widget but the user hasn't uploaded the file again. e.g back feature
     *
     * @param jsonSchema                      the json schema
     * @param jsonPayload                     the json payload
     * @param userTaskFileInput               the user task file input
     * @param previouslySubmittedUserTaskData the previously submitted user task data
     * @return the map
     */
    public static Map<String, Object> transformJsonPayloadToMap(final String jsonSchema,
                                                                final String jsonPayload,
                                                                final Map<String, byte[]> userTaskFileInput,
                                                                final Map<String, Object> previouslySubmittedUserTaskData) {
        if (StringUtils.isEmpty(jsonSchema)) {
            logger.error(JSON_SCHEMA_NOT_FOUND.reason());
            throw new InvalidPayloadException(JSON_SCHEMA_NOT_FOUND, JSON_SCHEMA_IS_NOT_FOUND);
        }
        if (StringUtils.isEmpty(jsonPayload)) {
            logger.error(INVALID_JSON_PAYLOAD.reason());
            throw new InvalidPayloadException(INVALID_JSON_PAYLOAD);
        }

        try {
            validateJsonPayload(jsonSchema, jsonPayload);
            final Map<String, Object> payloadMap = transformJsonToMap(jsonPayload);
            final Map<String, Object> schemaMap = transformJsonToMap(jsonSchema);
            validateDateTime(schemaMap, payloadMap);
            processFileInputs(schemaMap, payloadMap, userTaskFileInput, previouslySubmittedUserTaskData);
            return payloadMap;
        } catch (final IOException e) {
            logger.error("transformJsonPayloadToMap() Failed to process: {}. Exception: {}", JSON_PROCESSING_ERROR.reason(), e.getMessage());
            throw new InvalidPayloadException(JSON_PROCESSING_ERROR, JSON_PROCESSING_ERROR_OCCURED + ". JsonPayload: " + jsonPayload + " " + e.getMessage());
        }
    }

    /**
     * Process file inputs - creates FileValue variables.
     *
     * @param schemaMap                       the schema map
     * @param payloadMap                      the payload map
     * @param userTaskFileInput               the user task file input
     * @param previouslySubmittedUserTaskData
     */
    private static void processFileInputs(final Map<String, Object> schemaMap, final Map<String, Object> payloadMap,
                                          final Map<String, byte[]> userTaskFileInput, final Map<String, Object> previouslySubmittedUserTaskData) {
        final Map<String, Object> schemaProperties = (Map<String, Object>) schemaMap.get(PROPERTIES);
        for (final Map.Entry<?, ?> entry : schemaProperties.entrySet()) {
            final Map<String, Object> propertySchema = (Map<String, Object>) entry.getValue();
            final Object variableObject = payloadMap.get(entry.getKey());
            if (variableObject != null) {
                processFileInputsProperty(propertySchema, variableObject, userTaskFileInput, previouslySubmittedUserTaskData.get(entry.getKey()));
            }
        }
    }

    /**
     * Process property.
     *
     * @param propertySchema                  the property schema
     * @param variableObject                  the variable object
     * @param userTaskFileInput               the user task file input
     * @param previouslySubmittedUserTaskData
     * @return the object
     */
    private static Object processFileInputsProperty(final Map<String, Object> propertySchema, final Object variableObject,
                                                    final Map<String, byte[]> userTaskFileInput, final Object previouslySubmittedUserTaskData) {
        final String type = (String) propertySchema.get(TYPE);
        if (type.equals(OBJECT)) {
            Object oneOf = propertySchema.get(ONEOF);
            if (oneOf != null) {
                processFileInputsOneOfProperty(oneOf, variableObject, userTaskFileInput, previouslySubmittedUserTaskData);
            } else {
                if (propertySchema.get(FORMAT) != null && propertySchema.get(FORMAT).equals(FILE)) {
                    return processFileInputsFileProperty(variableObject, userTaskFileInput, previouslySubmittedUserTaskData);
                } else {
                    processFileInputsPropertyObject(propertySchema, variableObject, userTaskFileInput, (Map<String, Object>) previouslySubmittedUserTaskData);
                }
            }
        } else if (type.equals(ARRAY)) {
            return null;
        }
        return null;
    }

    private static void processFileInputsOneOfProperty(final Object oneOfProperty,
                                                       final Object variableObject,
                                                       final Map<String, byte[]> userTaskFileInput,
                                                       final Object previouslyPropertyValue) {
        if (oneOfProperty instanceof List) {
            List<Map<String, Object>> oneOfOptions = (List<Map<String, Object>>) oneOfProperty;
            for (Map<String, Object> oneOfOption : oneOfOptions) {
                processFileInputsPropertyObject(oneOfOption, variableObject, userTaskFileInput, (Map<String, Object>) previouslyPropertyValue);
            }
        }
    }

    private static Object processFileInputsFileProperty(final Object variableObject, final Map<String, byte[]> userTaskFileInput, final Object previouslyPropertyValue) {
        final Map<String, Object> fileProperties = (Map<String, Object>) variableObject;
        final String fileName = (String) fileProperties.get(FILE_NAME);

        if (FlowAutomationServiceUtil.isInvalidFileNameString(fileName)) {
            logger.error(INVALID_FILE_NAME.reason());
            throw new InvalidPayloadException(INVALID_FILE_NAME, INVALID_FILE_NAME_FOUND_IN_PAYLOAD);
        }

        final byte[] attachedFileContent = userTaskFileInput.get(fileName);
        if (attachedFileContent == null) { // not file uploaded. consider previous available data
            if (Objects.nonNull(previouslyPropertyValue) && previouslyPropertyValue instanceof FileValue) {
                final FileValue previousUploadFile = (FileValue) previouslyPropertyValue;
                if (Objects.equals(fileName, previousUploadFile.getFilename())) {
                    logger.debug("Found previous submitted data for file: {}", fileName);
                    return Variables.fileValue(fileName).file(previousUploadFile.getValue()).create();
                }
            }
            logger.error(FILE_ATTACHMENT_MISSING.reason());
            throw new InvalidPayloadException(FILE_ATTACHMENT_MISSING, format(FILE_ATTACHMENT_IS_MISSING, variableObject));
        }

        return Variables.fileValue(fileName).file(attachedFileContent).create();
    }

    private static void processFileInputsPropertyObject(final Map<String, Object> propertySchema, final Object variableObject,
                                                        final Map<String, byte[]> userTaskFileInput, final Map<String, Object> previouslySubmittedUserTaskData) {
        final Map<String, Object> schemaProperties = (Map<String, Object>) propertySchema.get(PROPERTIES);
        for (final Map.Entry<?, ?> entry : schemaProperties.entrySet()) {
            final Map<String, Object> subPropertySchema = (Map<String, Object>) entry.getValue();
            final Object subPropertyVariableObject = ((Map<String, Object>) variableObject).get(entry.getKey());
            if (subPropertyVariableObject != null) {
                Object previousPropertyValue = null;
                if (Objects.nonNull(previouslySubmittedUserTaskData)) {
                    previousPropertyValue = previouslySubmittedUserTaskData.get(entry.getKey());
                }
                final Object value = processFileInputsProperty(subPropertySchema, subPropertyVariableObject, userTaskFileInput, previousPropertyValue);
                if (value != null) {
                    ((Map<String, Object>) variableObject).put((String) entry.getKey(), value);
                }
            }
        }
    }

    /**
     * Validate json payload.
     *
     * @param schema      the schema
     * @param jsonPayload the json payload
     */
    private static void validateJsonPayload(final String schema, final String jsonPayload) {
        logger.debug("Validating json payload: {} with schema: {}", jsonPayload, schema);
        try {
            final JsonNode jsonSchemaNode = objectMapper.readTree(schema);
            final JsonSchema jsonSchema = jsonSchemaFactory.getJsonSchema(jsonSchemaNode);
            final JsonNode jsonPayloadNode = objectMapper.readTree(jsonPayload);
            validateJsonPayload(jsonSchema, jsonPayloadNode);
        } catch (final ProcessingException | IOException e) {
            logger.error("validateJsonPayload() Failed to process: {}. Exception: {}", JSON_PROCESSING_ERROR.reason(), e.getMessage());
            throw new InvalidPayloadException(JSON_PROCESSING_ERROR, JSON_PROCESSING_ERROR_OCCURED + ". JsonPayload: " + jsonPayload + " " + e.getMessage());
        }
    }

    /**
     * Validate the date time inputs of the payload.
     *
     * @param properties  the schema properties
     * @param jsonPayload the json payload
     * @param parent      the parent element of the json schema
     * @return the date-time validation error message
     */
    private static StringBuilder getDateTimeValidationMessage(final Map<String, Object> properties, final Map<String, Object> jsonPayload, final String parent) {
        final StringBuilder completeErrorMessageBuilder = new StringBuilder();
        if (properties != null && jsonPayload != null) {
            properties.entrySet().forEach(property -> {
                final Map<String, Object> schemaObject = (Map<String, Object>) property.getValue();
                final String type = (String) schemaObject.get(TYPE);
                final String format = (String) schemaObject.get(FORMAT);

                if (ARRAY.equals(type) && EDIT_TABLE.equals(format)) {
                    dateTimeInEditTable(schemaObject, jsonPayload, property, parent, completeErrorMessageBuilder);
                } else if (OBJECT.equals(type) && RADIO.equals(format)) {
                    nestedDateTimeWithRadioButton(schemaObject, jsonPayload, property, parent, completeErrorMessageBuilder);
                } else if (OBJECT.equals(type)) {
                    dateTimeInObjectProperty(schemaObject, jsonPayload, property, parent, completeErrorMessageBuilder);
                } else if (STRING.equals(type) && DATE_TIME.equals(format)) {
                    final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    final String stringDate = (String) jsonPayload.get(property.getKey());
                    if (stringDate != null) {
                        final String currentPath = getCurrentPropertyPath(parent, property);
                        try {
                            LocalDateTime.parse(stringDate, dateFormatter);
                        } catch (DateTimeParseException e) {
                            final String message = "string \\"+ stringDate + "\\ is invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ss.SSS'Z']";
                            completeErrorMessageBuilder.append(String.format("%nMESSAGE: %s%n", message));
                            completeErrorMessageBuilder.append(String.format("JSON PROPERTY: %s%n", currentPath));
                        }
                    }
                }
            });
        }
        return completeErrorMessageBuilder;
    }

    /**
     * Returns the path of the json element
     *
     * @param parent   the parent element of the json schema
     * @param property the schema property
     * @return
     */
    private static String getCurrentPropertyPath(final String parent, final Map.Entry<String, Object> property) {
        return parent + "/" + property.getKey();
    }

    /**
     * Capture the child properties when the type of the parent element is object and build the date-time validation error message.
     *
     * @param schemaObject                the schema object
     * @param jsonPayload                 the json payload
     * @param property                    the schema property
     * @param parent                      the parent element of the json schema
     * @param completeErrorMessageBuilder the error message builder object
     */
    private static void dateTimeInObjectProperty(final Map<String, Object> schemaObject, final Map<String, Object> jsonPayload, final Map.Entry<String, Object> property, final String parent, final StringBuilder completeErrorMessageBuilder) {
        final String currentPath = getCurrentPropertyPath(parent, property);
        final Map<String, Object> subProperties = (Map<String, Object>) schemaObject.get(PROPERTIES);
        final StringBuilder errorMessageBuilder = getDateTimeValidationMessage(subProperties, (Map<String, Object>) jsonPayload.get(property.getKey()), currentPath);
        completeErrorMessageBuilder.append(errorMessageBuilder);
    }

    /**
     * Capture the date-time picker that used as a nested widget in the radio button and build the date-time validation error message.
     *
     * @param schemaObject                the schema object
     * @param jsonPayload                 the json payload
     * @param property                    the schema property
     * @param parent                      the parent element of the json schema
     * @param completeErrorMessageBuilder the error message builder object
     */
    private static void nestedDateTimeWithRadioButton(final Map<String, Object> schemaObject, final Map<String, Object> jsonPayload, final Map.Entry<String, Object> property, final String parent, final StringBuilder completeErrorMessageBuilder) {
        final List<Map<String, Object>> oneOfItems = (List<Map<String, Object>>) schemaObject.get(ONEOF);
        oneOfItems.forEach(item -> {
            final Map<String, Object> itemProperties = (Map<String, Object>) item.get(PROPERTIES);
            final String currentPath = getCurrentPropertyPath(parent, property);
            final Map<String, Object> itemPayload = (Map<String, Object>) jsonPayload.get(property.getKey());

            final StringBuilder errorMessageBuilder = getDateTimeValidationMessage(itemProperties, itemPayload, currentPath);
            completeErrorMessageBuilder.append(errorMessageBuilder);
        });
    }

    /**
     * Capture the date-time picker that used in the editable table and build the date-time validation error message.
     *
     * @param schemaObject                the schema object
     * @param jsonPayload                 the json payload
     * @param property                    the schema property
     * @param parent                      the parent element of the json schema
     * @param completeErrorMessageBuilder the error message builder object
     */
    private static void dateTimeInEditTable(final Map<String, Object> schemaObject, final Map<String, Object> jsonPayload, final Map.Entry<String, Object> property, final String parent, final StringBuilder completeErrorMessageBuilder) {
        final Map<String, Object> arrayItems = (Map<String, Object>) schemaObject.get(ITEMS);
        final String arrayType = (String) arrayItems.get(TYPE);
        if (OBJECT.equals(arrayType)) {
            final Map<String, Object> arrayProperties = (Map<String, Object>) arrayItems.get(PROPERTIES);
            final List<Map<String, Object>> arrayValues = (List<Map<String, Object>>) jsonPayload.get(property.getKey());
            if (arrayValues != null) {
                final String currentPath = getCurrentPropertyPath(parent, property);
                for (int i = 0; i < arrayValues.size(); i++) {
                    String itemPath = currentPath + "/" + i;
                    StringBuilder errorMessageBuilder = getDateTimeValidationMessage(arrayProperties, arrayValues.get(i), itemPath);
                    completeErrorMessageBuilder.append(errorMessageBuilder);
                }
            }
        }
    }

    /**
     * Validate the date-time of the payload.
     *
     * @param schemaMap  the schema map object
     * @param payloadMap the payload map object
     */
    private static void validateDateTime(final Map<String, Object> schemaMap, final Map<String, Object> payloadMap) {
        final Map<String, Object> schemaProperties = (Map<String, Object>) schemaMap.get(PROPERTIES);
        final StringBuilder completeErrorMessageBuilder = getDateTimeValidationMessage(schemaProperties, payloadMap, "");

        if (StringUtils.isNotEmpty(completeErrorMessageBuilder)) {
            logger.error("Schema validation failed! schema: [{}], payload: [{}], error: [{}] reason: [{}]", schemaMap, payloadMap, completeErrorMessageBuilder,
                    JSON_VALIDATION_FAILED.reason());
            throw new InvalidPayloadException(JSON_VALIDATION_FAILED, completeErrorMessageBuilder.toString());
        }
    }

    /**
     * Validate json payload.
     *
     * @param jsonSchema      the json schema
     * @param jsonPayloadNode the json payload node
     * @throws ProcessingException the processing exception
     */
    private static void validateJsonPayload(final JsonSchema jsonSchema, final JsonNode jsonPayloadNode) throws ProcessingException {
        final ProcessingReport processingReport = jsonSchema.validate(jsonPayloadNode, true);
        if (!processingReport.isSuccess()) {
            final StringBuilder messageBuilder = new StringBuilder();
            processingReport.forEach(jsonError -> {
                final LogLevel logLevel = jsonError.getLogLevel();
                if (logLevel == LogLevel.ERROR || logLevel == LogLevel.FATAL) {
                    final JsonNode jsonNode = jsonError.asJson();
                    appendValidationErrorResponse(messageBuilder, jsonNode, "%nMESSAGE: %s %n", "message");
                    final String loadingURI = getJsonNode(jsonNode, SCHEMA_TEXT, "loadingURI").toString().replaceAll("\"", "");
                    final String pointer = getJsonNode(jsonNode, SCHEMA_TEXT, POINTER).toString().replaceAll("\"", "");
                    final String keyword = getJsonNode(jsonNode, "keyword").toString().replaceAll("\"", "");
                    if (StringUtils.isNotEmpty(loadingURI) && StringUtils.isNotEmpty(pointer) && StringUtils.isNotEmpty(keyword)) {
                        messageBuilder.append(String.format("SCHEMA PATH: %s%s/%s %n", loadingURI, pointer, keyword));
                    } else if (StringUtils.isNotEmpty(loadingURI) && StringUtils.isNotEmpty(keyword)) {
                        messageBuilder.append(String.format("SCHEMA PATH: %s/%s %n", loadingURI, keyword));
                    }

                    appendValidationErrorResponse(messageBuilder, jsonNode, "INSTANCE: %s%n", "instance", "pointer");
                }
            });
            logger.error("Schema validation failed! schema: [{}], payload: [{}], error: [{}] reason: [{}]", jsonSchema, jsonPayloadNode, messageBuilder,
                    JSON_VALIDATION_FAILED.reason());
            throw new InvalidPayloadException(JSON_VALIDATION_FAILED, messageBuilder.toString());
        }
        logger.debug("Schema validation is successful");
    }

    private static void appendValidationErrorResponse(final StringBuilder sb, final JsonNode jsonNode, final String appendment, final String... keys) {
        final JsonNode keyNode = getJsonNode(jsonNode, keys);
        if (keyNode != null) {
            String errorMessage = keyNode.toString().replaceAll("\"", "");
            errorMessage = errorMessage.replaceAll("yyyy-MM-dd'T'HH:mm:ssZ, yyyy-MM-dd'T'HH:mm:ss.\\[0-9\\]\\{1,12\\}Z", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            if (StringUtils.isNotEmpty(errorMessage)) {
                sb.append(String.format(appendment, errorMessage));
            }
        }
    }

    private static JsonNode getJsonNode(JsonNode jsonNode, final String... keys) {
        JsonNode keyNode = null;
        for (final String key : keys) {
            keyNode = jsonNode.get(key);
            jsonNode = keyNode;
        }
        return keyNode;
    }
}