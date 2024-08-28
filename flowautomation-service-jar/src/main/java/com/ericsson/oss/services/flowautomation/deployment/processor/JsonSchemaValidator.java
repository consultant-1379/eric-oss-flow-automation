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

package com.ericsson.oss.services.flowautomation.deployment.processor;

import com.ericsson.oss.services.flowautomation.error.ErrorReason;
import com.ericsson.oss.services.flowautomation.exception.JsonSchemaValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ARRAY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DATE_TIME;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DEFAULT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FORMAT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ITEMS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.OBJECT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ONEOF;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROPERTIES;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.RADIO;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TYPE;
import static com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil.FA_DATE_TIME_FORMATTER;

/**
 * Flow package validation.
 */
public class JsonSchemaValidator {

    /**
     * Validates the schema.
     *
     * @param map          the JSON schema properties
     * @param resourceName the name of the JSON schema
     */
    public void validate(final Map<String, Object> map, String resourceName) {

        List<ErrorReason> errors = new ArrayList<>();
        validateSchema(map, errors, null);

        if (!errors.isEmpty()) {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("MESSAGE: " + resourceName + " is invalid against requested date format(s) [yyyy-MM-dd'T'HH:mm:ss.SSS'Z']." + "\n" + "ERROR REASON(S): " + "\n");
            errors.forEach(error -> {
                messageBuilder.append(error.getErrorMessage());
                messageBuilder.append("\n");
            });
            throw new JsonSchemaValidationException(messageBuilder.toString());
        }
    }

    /**
     * Validates the JSON schemas of the flow package.
     *
     * @param schemaMap the JSON schema properties
     * @param errors    the Date/time error list of the schema
     * @param path      the property path in the JSON schema
     */
    private void validateSchema(final Map<String, Object> schemaMap, final List<ErrorReason> errors, final String path) {
        String type = (String) schemaMap.get(TYPE);
        String format = (String) schemaMap.get(FORMAT);

        if (type == null) {
            return;
        }

        if (OBJECT.equals(type)) {
            Map<String, Object> properties = (Map<String, Object>) schemaMap.get(PROPERTIES);
            if (properties != null) {
                validateObjectTypeProperties(properties, errors, path);
            } else if (RADIO.equals(format)) {
                validateNestedPropertiesOfRadio(schemaMap, errors, path);
            }
        } else if (ARRAY.equals(type)) {
            validateArrayTypeProperties(schemaMap, errors, path);
        } else if (DATE_TIME.equals(format)) {
            validateDateTime(schemaMap, errors, path);
        }
    }

    /**
     * Validates the object type properties.
     *
     * @param properties the child properties of the Object property
     * @param errors     the error list
     * @param path       the JSON property path
     */
    private void validateObjectTypeProperties(final Map<String, Object> properties, final List<ErrorReason> errors, final String path) {
        properties.forEach((key, value) -> {
            if (value instanceof Map) {
                final String currentPath = getCurrentPropertyPath(path, key).toString();
                validateSchema((Map<String, Object>) value, errors, currentPath);
            }
        });
    }

    /**
     * Validates the nested properties of the Radio Button widget.
     *
     * @param schemaMap the JSON schema portion of the Radio Button widget
     * @param errors    the error list
     * @param path      the JSON property path
     */
    private void validateNestedPropertiesOfRadio(final Map<String, Object> schemaMap, final List<ErrorReason> errors, final String path) {
        List<Object> oneOfItems = (List<Object>) schemaMap.get(ONEOF);
        if (oneOfItems != null) {
            for (int i = 0; i < oneOfItems.size(); i++) {
                Map<String, Object> oneOfMap = (Map<String, Object>) oneOfItems.get(i);
                if (oneOfMap != null) {
                    Map<String, Object> oneOfProperties = (Map<String, Object>) oneOfMap.get(PROPERTIES);
                    if (oneOfProperties != null) {
                        oneOfProperties.forEach((key, value) -> {
                            if (value instanceof Map) {
                                final String currentPath = getCurrentPropertyPath(path, key).toString();
                                validateSchema((Map<String, Object>) value, errors, currentPath);
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Validates the Array type properties.
     *
     * @param schemaMap the JSON schema portion of the Array object
     * @param errors    the error list
     * @param path      the JSON property path
     */
    private void validateArrayTypeProperties(final Map<String, Object> schemaMap, final List<ErrorReason> errors, final String path) {
        Map<String, Object> items = (Map<String, Object>) schemaMap.get(ITEMS);
        if (items != null) {
            Map<String, Object> properties = (Map<String, Object>) items.get(PROPERTIES);
            if (properties != null) {
                properties.forEach((key, value) -> {
                    if (value instanceof Map) {
                        final String currentPath = getCurrentPropertyPath(path, key).toString();
                        validateSchema((Map<String, Object>) value, errors, currentPath);
                    }
                });
            }
        }
    }

    /**
     * Validates the default date/time values.
     *
     * @param schemaMap
     * @param errors    the error list
     * @param path      the JSON property path
     */
    private void validateDateTime(final Map<String, Object> schemaMap, final List<ErrorReason> errors, final String path) {
        String defaultValue = (String) schemaMap.get(DEFAULT);
        if (defaultValue != null) {
            try {
                LocalDateTime.parse(defaultValue, FA_DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                errors.add(new ErrorReason("Invalid Date Format for JSON Property: " + path + "\n"));
            }
        }
    }

    /**
     * Returns the JSON schema path of the element.
     *
     * @param path the JSON property path
     * @param key  the Map key
     * @return currentPath
     */
    private StringBuilder getCurrentPropertyPath(final String path, final String key) {
        StringBuilder currentPath = new StringBuilder();

        if (path != null) {
            currentPath.append(path + "/" + key);
        } else {
            currentPath.append(key);
        }
        return currentPath;
    }

}
