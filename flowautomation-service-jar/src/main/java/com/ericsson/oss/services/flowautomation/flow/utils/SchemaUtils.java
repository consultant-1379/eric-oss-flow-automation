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

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.JSON_PARSING_ERROR_OCCURRED;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.JSON_PARSING_ERROR;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.OBJECT_GEN;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SCHEMA_GEN;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flow.exception.FlowServiceException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class SchemaUtils.
 */
public class SchemaUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaUtils.class);

    /** The Constant objectMapper. */
    protected static final ObjectMapper objectMapper = new ObjectMapper();

    private SchemaUtils() {
    }

    public static Map<String, Object> removeSchemaGenerationExtensions(final Map<String, Object> schema) {
        final Map<String, Object> updatedSchema = new LinkedHashMap<>();
        for (final Map.Entry<?, ?> entry : schema.entrySet()) {
            final String key = (String) entry.getKey();
            final Object value = entry.getValue();
            if (!key.equals(SCHEMA_GEN) && !key.equals(OBJECT_GEN)) {
                if (value instanceof Map) {
                    updatedSchema.put(key, removeSchemaGenerationExtensions((Map<String, Object>) value));
                } else {
                    updatedSchema.put(key, value);
                }
            }
        }

        return updatedSchema;
    }

    public static Map<String, Object> removeSchemaHiddenNames(final Map<String, Object> schema, Set<String> hiddenReportVariableNames) {
        final Map<String, Object> updatedSchema = new LinkedHashMap<>();
        for (final Map.Entry<?, ?> entry : schema.entrySet()) {
            final String key = (String) entry.getKey();
            final Object value = entry.getValue();
            if (!hiddenReportVariableNames.contains("FaInternal_" + key)) {
                if (value instanceof Map) {
                    updatedSchema.put(key, removeSchemaHiddenNames((Map<String, Object>) value, hiddenReportVariableNames));
                } else {
                    updatedSchema.put(key, value);
                }
            }
        }

        return updatedSchema;
    }


    public static Map<String, Object> getSchemaMap(String schema) {
        Map<String, Object> schemaMap;
        try {
            schemaMap = objectMapper.readValue(schema, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (final IOException e) {
            LOGGER.debug("Flow: {} does not exist.", schema);
            throw new FlowServiceException(JSON_PARSING_ERROR, JSON_PARSING_ERROR_OCCURRED + " : " + e.getMessage());
        }
        return schemaMap;
    }
}
