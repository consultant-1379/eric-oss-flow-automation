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

package com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder;

import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ARRAY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.BINDING;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.BODY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.BOOLEAN;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.COLON;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.COMMA;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DEFAULT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DESCRIPTION;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DOWNLOAD_LINK;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.EMPTY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FORMAT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.HEADER;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.INDEX_BINDING;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.INDEX_DATA;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.INDEX_NAME;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.INTEGER;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ITEMS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.NUMBER;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.OBJECT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.OBJECT_GEN;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROGRESS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROPERTIES;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.REQUIRED;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TITLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TYPE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.VARIABLE;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader.FA_INTERNAL_REPORT_SCHEMA_RESOURCE;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader.FLOW_REPORT_SCHEMA_FILE;
import static com.ericsson.oss.services.flowautomation.flow.utils.PayloadTransformer.transformJsonToMap;
import static com.ericsson.oss.services.flowautomation.flow.utils.PayloadTransformer.transformMapToJsonPayload;
import static com.ericsson.oss.services.flowautomation.flow.utils.SchemaUtils.removeSchemaGenerationExtensions;
import static com.ericsson.oss.services.flowautomation.flow.utils.SchemaUtils.removeSchemaHiddenNames;
import static com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil.DATE_TIME_FORMATTER_WITHOUT_TZ;
import static com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil.ISO_RFC822_DATE_TIME_FORMATTER;
import static java.lang.String.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.entities.ReportVariableEntity;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.repo.ReportVariableRepository;

/**
 * The class FlowExecutionReportBuilder is used to binding all the properties given by <b>FAInternal-flow-report-schema.json</b> file.
 */
public class FlowExecutionReportBuilder extends SchemaBuilder {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String VARIABLE_EXCEEDS_SIZE_WARN = "The report variable:[%s] value exceeds size limit and hence won't be available in the report. Please use the format download-link in the report schema, to be able to download the variable.";
    private static final String MAP = "map";
    private static final String CONSTANT = "const";
    private static final String GENERATED = "generated";

    private static final int REPORT_VARIABLE_SIZE_LIMIT = 4096;
    private static final int REPORT_HIDDEN_VARIABLE_PREFIX_SIZE = 11;

    private List<ReportVariableEntity> reportVariables;
    private Set<String> oversizedReportVariableNames;
    private Set<String> hiddenReportVariableNames;
    private FlowExecution flowExecution;

    @Inject
    private Logger logger;

    @Inject
    private ReportVariableRepository reportVariableRepository;


    private long flowExecutionId;

    /**
     * Gets the flow execution report.
     *
     * @param flowExecutionEntity the flow execution
     * @param flowExecution      the deployment id
     * @return the flow execution report
     */
    public String getFlowExecutionReport(final FlowExecutionEntity flowExecutionEntity, final FlowExecution flowExecution) {
        try {
            logger.debug("-----------Report Debug: Start getFlowExecutionReport");
            this.flowExecution = flowExecution;
            this.flowExecutionId = flowExecutionEntity.getId();
            reportVariables = reportVariableRepository.getReportVariablesWithinSize(flowExecutionId, REPORT_VARIABLE_SIZE_LIMIT);
            oversizedReportVariableNames = reportVariableRepository.getVariableNamesBeyondSize(flowExecutionId, REPORT_VARIABLE_SIZE_LIMIT);
            hiddenReportVariableNames = reportVariableRepository.getHiddenReportVariableNames(flowExecutionId);
            final String deploymentId = flowExecutionEntity.getFlowDetailEntity().getDeploymentId();

            final Map<String, Object> report = new LinkedHashMap<>();
            report.put(HEADER, getReportHeader());
            report.put(BODY, getReportBody(deploymentId));

            final String payload = transformMapToJsonPayload(getFlowExecutionReportSchema(flowExecutionEntity), report);
            logger.debug("-----------Report Debug: End getFlowExecutionReport");
            return payload;
        } catch (final FlowAutomationException e) {
            throw e;
        } catch (final Exception e) {
            throw new FlowAutomationException(e.getMessage(), e);
        }
    }

    /**
     * Gets the report header.
     *
     * @return the report header
     */
    private Map<String, Object> getReportHeader() {
        final Map<String, Object> header = new LinkedHashMap<>();
        header.put("reportTime", DateTimeUtil.getTimeStamp(ISO_RFC822_DATE_TIME_FORMATTER));
        header.put("flowId", flowExecution.getFlowId());
        header.put("flowVersion", flowExecution.getFlowVersion());
        header.put("flowName", flowExecution.getFlowName());
        header.put("flowExecutionName", flowExecution.getName());
        header.put("startedBy", flowExecution.getExecutedBy());
        header.put("startTime", DateTimeUtil.changeDateTimeFormat(flowExecution.getStartTime(), DATE_TIME_FORMATTER_WITHOUT_TZ, ISO_RFC822_DATE_TIME_FORMATTER));
        final String endTime = flowExecution.getEndTime();
        if (endTime != null) {
            header.put("endTime", DateTimeUtil.changeDateTimeFormat(endTime, DATE_TIME_FORMATTER_WITHOUT_TZ, ISO_RFC822_DATE_TIME_FORMATTER));
        }
        header.put("status", flowExecution.getState());

        return header;
    }


    /**
     * Gets the report body.
     *
     * @param deploymentId the deployment id
     * @return the report body
     */
    private Object getReportBody(final String deploymentId) {
        final Map<String, Object> reportBody = new LinkedHashMap<>();
        createProperties(BODY, getSchemaMap(deploymentId, FLOW_REPORT_SCHEMA_FILE), reportBody);
        return reportBody.get(BODY);
    }

    /**
     * Gets the flow execution report schema.
     *
     * @param deploymentId the deployment id
     * @return the flow execution report schema
     */
    public String getFlowExecutionReportSchema(final FlowExecutionEntity flowExecuionEntity) {
        try {
            final String deploymentId = flowExecuionEntity.getFlowDetailEntity().getDeploymentId();
            final Map<String, Object> reportSchema = transformJsonToMap(getInternalSchemaResource(FA_INTERNAL_REPORT_SCHEMA_RESOURCE));

            final Map<String, Object> reportBodySchema = getSchemaMap(deploymentId, FLOW_REPORT_SCHEMA_FILE);
            reportSchema.put(TITLE, reportBodySchema.get(TITLE));
            reportSchema.put(DESCRIPTION, reportBodySchema.get(DESCRIPTION));

            final Map<String, Object> reportProperties = (Map<String, Object>) reportSchema.get(PROPERTIES);
            final Map<String, Object> reportBody = (Map<String, Object>) reportProperties.get(BODY);

            reportBody.put(PROPERTIES, reportBodySchema.get(PROPERTIES));
            reportBody.put(REQUIRED, reportBodySchema.get(REQUIRED));
            hiddenReportVariableNames = reportVariableRepository.getHiddenReportVariableNames(flowExecuionEntity.getId());

            return transformMapToJsonPayload(removeSchemaHiddenNames(removeSchemaGenerationExtensions(reportSchema), hiddenReportVariableNames));
        } catch (final FlowAutomationException e) {
            throw e;
        } catch (final Exception e) {
            throw new FlowAutomationException(e.getMessage(), e);
        }
    }

    /**
     * Creates the properties.
     *
     * @param propertyName the property name
     * @param schemaMap    the schema map
     * @param bodyMap      the body map
     */
    @SuppressWarnings("squid:S3776")
    private void createProperties(final String propertyName, final Map<String, Object> schemaMap, final Map<String, Object> bodyMap) {
        final String type = (String) schemaMap.get(TYPE);
        final String format = (String) schemaMap.get(FORMAT);
        final Object defaultValue = schemaMap.get(DEFAULT);
        if (type.equals(OBJECT) && !PROGRESS.equals(format)) {
            final Map<String, Object> schemaProperties = (Map<String, Object>) schemaMap.get(PROPERTIES);
            final Map<String, Object> subBodyMap = new LinkedHashMap<>();

            hideSectionOfReport(schemaMap, schemaProperties);

            bodyMap.put(propertyName, subBodyMap);
            schemaProperties.forEach((key, value) -> createProperties(key, (Map<String, Object>) value, subBodyMap));
        } else if (type.equals(ARRAY)) {
            if (schemaMap.get(OBJECT_GEN) != null) {
                final Map<String, Object> objectGen = (Map<String, Object>) schemaMap.get(OBJECT_GEN);
                final String indexBinding = (String) objectGen.get(INDEX_BINDING);
                final String indexName = (String) objectGen.get(INDEX_NAME);
                final String indexDataNameWithBinding = (String) objectGen.get(INDEX_DATA);

                final Map<String, Object> items = (Map<String, Object>) schemaMap.get(ITEMS);
                final Map<String, Object> itemProperties = (Map<String, Object>) items.get(PROPERTIES);

                //Always query the index bindings from db. they can grow larger than restricted size.
                final String indexBindingValue = reportVariableRepository.getReportVariableValue(flowExecutionId, indexBinding);
                if (indexBindingValue != null) {
                    final List<Object> table = new ArrayList<>();
                    ((List<String>) convertValueType(indexBinding, type, indexBindingValue)).forEach(index -> {
                        final Map<String, Object> row = new LinkedHashMap<>();
                        final String indexDataName = indexDataNameWithBinding != null
                                ? indexDataNameWithBinding.replaceAll(format("\\{%s\\}", indexName), index)
                                : null;
                        final Map<String, Object> indexData = indexDataName != null
                                ? (Map<String, Object>) getVariableValue(indexDataName, MAP, Collections.emptyMap())
                                : Collections.emptyMap();

                        itemProperties.forEach((key, value) -> {
                            final Map<String, Object> propValue = (Map<String, Object>) value;
                            if (OBJECT.equals(propValue.get(TYPE)) && !PROGRESS.equals(propValue.get(FORMAT))) {
                                row.put(key, getObjectTypeProperty(propValue, indexName, index, indexData));
                            } else {
                                final Object colValue = getSimpleProperty(propValue, indexName, index, indexData);
                                if (colValue != null) {
                                    row.put(key, colValue);
                                }
                            }
                        });
                        table.add(row);
                    });
                    bodyMap.put(propertyName, table);
                }
            }
        } else {
            if (schemaMap.get(OBJECT_GEN) != null) {
                final Map<String, Object> objectGen = (Map<String, Object>) schemaMap.get(OBJECT_GEN);
                final String binding = (String) objectGen.get(BINDING);
                final String variableName = binding.substring(1 + binding.lastIndexOf(COLON));

                Object value = getVariableValue(variableName, type, defaultValue);
                // if the value of the variable is available and not null, ideally there shouldn't be any default value for these downloadable variables
                if (DOWNLOAD_LINK.equals(schemaMap.get(FORMAT))) {
                    value = FlowExecutionReportVariableUtil.createDownloadUrl(flowExecution, variableName);
                }

                if (value != null) {
                    bodyMap.put(propertyName, value);
                }
            }
        }
    }

    private void hideSectionOfReport(final Map<String, Object> schemaMap, final Map<String, Object> schemaProperties) {

        for (final String hiddenReportVariable : hiddenReportVariableNames) {
            final String hiddenReportNames = hiddenReportVariable.substring(REPORT_HIDDEN_VARIABLE_PREFIX_SIZE);
            schemaProperties.entrySet().removeIf(e -> e.getKey().equals(hiddenReportNames));
            schemaMap.entrySet().removeIf(e -> e.getKey().equals(hiddenReportNames));
        }
    }

    private Map<String, Object> getObjectTypeProperty(final Map<String, Object> propValue, final String indexName, final String index,
                                                      final Map<String, Object> indexData) {
        final Map<String, Object> cellProperties = (Map<String, Object>) propValue.get(PROPERTIES);
        return cellProperties.entrySet().stream()
                .collect(HashMap::new, (m, entry) -> {
                    final Object propertyValue = getSimpleProperty((Map<String, Object>) entry.getValue(), indexName, index, indexData);
                    if (propertyValue != null) {
                        m.put(entry.getKey(), propertyValue);
                    }
                }, HashMap::putAll);
    }

    /**
     * Gets the table property.
     *
     * @param item      the item
     * @param indexName the index name
     * @param index     the index
     * @return the table property
     */
    private Object getSimpleProperty(final Map<String, Object> item, final String indexName, final String index,
                                     final Map<String, Object> indexData) {
        final Map<String, Object> itemObjectGen = (Map<String, Object>) item.get(OBJECT_GEN);
        final String templateBinding = (String) itemObjectGen.get(BINDING);
        final String variableName = templateBinding.substring(1 + templateBinding.lastIndexOf(COLON));
        final String binding = variableName.replaceAll(format("\\{%s\\}", indexName), index);
        Object variableValue = EMPTY;

        final String[] split = templateBinding.split(COLON);
        if (!indexData.isEmpty() && split.length == 3 && split[0].equals(VARIABLE) && split[1].equals(INDEX_DATA)) {
            variableValue = indexData.get(binding);
            if (variableValue == null) {
                variableValue = item.get(DEFAULT);
            } else if (!(OBJECT.equalsIgnoreCase((String) item.get(TYPE)) && variableValue instanceof LinkedHashMap)) {
                // This will handle the case when a value is put to map with type A in groovy but defined with type B in report schema, to keep the backward compatibility
                variableValue = convertValueType(binding, (String) item.get(TYPE), String.valueOf(variableValue));
            }
        } else if (split.length == 3 && split[0].equals(VARIABLE) && split[1].equals(GENERATED)) {
            variableValue = convertValueType(binding, (String) item.get(TYPE), binding);
        } else if (split.length == 2 && split[0].equals(CONSTANT)) {
            variableValue = convertValueType(binding, (String) item.get(TYPE), split[1]);
        } else {
            variableValue = getVariableValue(binding, (String) item.get(TYPE), item.get(DEFAULT));
        }

        // if the value of the variable is available and not null, ideally there shouldn't be any default value for these downloadable variables
        if (DOWNLOAD_LINK.equals(item.get(FORMAT))) {
            return FlowExecutionReportVariableUtil.createDownloadUrl(flowExecution, binding);
        }

        return variableValue;
    }

    /**
     * Gets the variable value.
     *
     * @param variableName the variable name
     * @param type         the type
     * @return the variable value
     */
    private Object getVariableValue(final String variableName, final String type, final Object defaultValue) {
        final Predicate<? super ReportVariableEntity> variableNameFilter = variable -> variable.getName().equals(variableName.trim());
        final String value = reportVariables.stream().filter(variableNameFilter).map(ReportVariableEntity::getValue).findFirst().orElse(null);
        if (value == null) {
            if (oversizedReportVariableNames.contains(variableName)) {
                logger.warn("Recording FLOW_EXECUTION_WARN. Flow Id: {}, Flow Execution Name: {}, " +
                        "Message: The report variable:{} value exceeds size limit and hence won't be available in the report. " +
                        "Please use the format download-link in the report schema, to be able to download the variable.",
                        flowExecution.getFlowId(), flowExecution.getName(), variableName);
                return EMPTY;
            }
            return defaultValue;
        }
        return convertValueType(variableName, type, value);
    }

    /**
     * Convert value type.
     * @param variableName the variable name
     * @param type         the type
     * @param value        the value
     * @return the object
     */
    private Object convertValueType(final String variableName, final String type, final String value) {
        try {
            switch (type) {
                case INTEGER:
                    return Integer.parseInt(value);
                case NUMBER:
                    return Double.parseDouble(value);
                case BOOLEAN:
                    return Boolean.parseBoolean(value);
                case ARRAY:
                    return Arrays.asList(value.split(COMMA));
                case MAP:
                case OBJECT:
                    return stringToMap(value);
                default:
                    return value;
            }
        } catch (final Exception e) {
            logger.error("Format exception, value {} cannot be processed for property: {}", value, variableName);
            throw new ReportGenerationFailedException(
                    "Failed to generate report. The property: " + variableName + " couldn't be parsed, current value: " + value, e);
        }
    }

    /**
     * Convert json string to object for a map variable
     *
     * @param value the json string of a map variable
     * @return the map object
     */
    private Map<String, Object> stringToMap(final String value) {
        try {
            return objectMapper.readValue(value, Map.class);
        } catch (final IOException e) {
            throw new FlowAutomationException("Error parsing map report variable from json: " + e.getMessage(), e);
}
    }
}