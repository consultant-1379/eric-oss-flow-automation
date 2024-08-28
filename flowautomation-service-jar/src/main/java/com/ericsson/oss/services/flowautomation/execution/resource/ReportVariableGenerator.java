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

package com.ericsson.oss.services.flowautomation.execution.resource;

import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.NO_FLOW_EXECUTION_RESOURCE_AVAILABLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ARRAY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.BODY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DOWNLOAD_LINK;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FORMAT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ITEMS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.OBJECT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROPERTIES;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TYPE;
import static com.ericsson.oss.services.flowautomation.flow.utils.PayloadTransformer.transformJsonToMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder.FlowExecutionReportBuilder;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder.FlowExecutionReportVariableUtil;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import com.ericsson.oss.services.flowautomation.repo.ReportVariableRepository;

/**
 * This class is responsible for generating the flow report variable.
 */
@FlowExecutionResourceType("report-variable")
public class ReportVariableGenerator implements FlowExecutionResourceGenerator {

    @Inject
    private ReportVariableRepository reportVariableRepository;

    @Inject
    private FlowExecutionReportBuilder flowExecutionReportBuilder;

    @Inject
    private Logger logger;

    public FlowExecutionResource generate(final FlowExecutionEntity flowExecutionEntity, final String variableName) {
        final String reportVariableValue = reportVariableRepository.getReportVariableValue(flowExecutionEntity.getId(), variableName);
        if (reportVariableValue == null) {
            throw new FlowResourceNotFoundException(NO_FLOW_EXECUTION_RESOURCE_AVAILABLE, String.format("The report variable [%s] for flow execution [%s] is null.", variableName, flowExecutionEntity.getFlowExecutionName()));
        }
        return new FlowExecutionResource(variableName, reportVariableValue.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the collection of {@code FlowExecutionResource} where each resource in the collection points to a report variable.
     * It does it by reading the report schema and data together and then parsing the report variables names where the the format of the variable in the schema is data-link.
     * Once it gets all these variables with format data-link, it then reads the value of each of these variables and creates the collection of FlowExecutionResource.
     *
     * @param flowExecutionEntity the flow execution entity
     * @param flowExecution       the flow execution
     * @return the report variables
     */
    @Override
    public Collection<FlowExecutionResource> generateForZipBundle(final FlowExecutionEntity flowExecutionEntity, final FlowExecution flowExecution) {
        try {
            final Map<String, Object> reportBodyPropertiesSchema = getReportBodyPropertiesSchema(flowExecutionEntity);
            final Map<String, Object> reportBodyData = getReportBodyData(flowExecutionEntity, flowExecution);
            return getDownloadableReportVariables(reportBodyPropertiesSchema, reportBodyData).stream() //process schema and data together and get all the variable values where format is data-link in the schema
                    .map(variableName -> {
                        final String reportVariableValue = reportVariableRepository.getReportVariableValue(flowExecutionEntity.getId(), variableName);
                        if (reportVariableValue != null) {
                            return new FlowExecutionResource(variableName, "report", reportVariableValue.getBytes(StandardCharsets.UTF_8));
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (final IOException e) {
            throw new FlowAutomationException(e.getMessage(), e);
        }
    }

    /**
     * The report has two properties on the top level, the header and the body. This method returns the properties from the report body property from inside the report schema.
     *
     * @param flowExecutionEntity the flow execution entity
     * @return the map of properties inside report body
     * @throws IOException if I/O error happens
     */
    private Map<String, Object> getReportBodyPropertiesSchema(final FlowExecutionEntity flowExecutionEntity) throws IOException {
        final Map<String, Object> reportSchema = transformJsonToMap(flowExecutionReportBuilder.getFlowExecutionReportSchema(flowExecutionEntity));
        final Map<String, Object> reportBodySchema = (Map<String, Object>) ((Map<String, Object>) reportSchema.get(PROPERTIES)).get(BODY);
        return (Map<String, Object>) reportBodySchema.get(PROPERTIES);
    }


    /**
     * This method returns the value of the body property from inside the report data.
     *
     * @param flowExecutionEntity the flow execution entity
     * @param flowExecution       the flow execution
     * @return the map of properties inside report body
     * @throws IOException if I/O error happens
     */
    private Map<String, Object> getReportBodyData(final FlowExecutionEntity flowExecutionEntity, final FlowExecution flowExecution) throws IOException {
        final Map<String, Object> reportData = transformJsonToMap(flowExecutionReportBuilder.getFlowExecutionReport(flowExecutionEntity, flowExecution));
        return (Map<String, Object>) reportData.get(BODY);
    }

    private Collection<String> getDownloadableReportVariables(final Map<String, Object> reportBodyPropertiesSchema, final Map<String, Object> reportBodyData) {
        final Collection<String> downloadableVariables = new ArrayList<>();

        if (reportBodyData != null) {
            reportBodyPropertiesSchema.forEach((key, value) -> {
                final Map<String, Object> valueMap = (Map<String, Object>) value;
                if (DOWNLOAD_LINK.equals(valueMap.get(FORMAT))) {
                    final String variableDownloadUrl = String.valueOf(reportBodyData.get(key));
                    downloadableVariables.add(FlowExecutionReportVariableUtil.extractVariableNameFromUrl(variableDownloadUrl));
                } else if (OBJECT.equals(valueMap.get(TYPE))) { //process all the properties inside the object
                    downloadableVariables.addAll(getDownloadableReportVariables((Map<String, Object>) valueMap.get(PROPERTIES), (Map<String, Object>) reportBodyData.get(key)));
                } else if (ARRAY.equals(valueMap.get(TYPE))) { //process all the properties inside items
                    final Map<String, Object> items = (Map<String, Object>) valueMap.get(ITEMS);
                    final List<Object> arrayData = (List<Object>) reportBodyData.get(key);
                    if (OBJECT.equals(items.get(TYPE)) && arrayData != null) {
                        final Map<String, Object> itemProperties = (Map<String, Object>) items.get(PROPERTIES);
                        arrayData.forEach(itemData -> downloadableVariables.addAll(getDownloadableReportVariables(itemProperties, (Map<String, Object>) itemData)));
                    }
                }
            });
        }

        return downloadableVariables;
    }
}
