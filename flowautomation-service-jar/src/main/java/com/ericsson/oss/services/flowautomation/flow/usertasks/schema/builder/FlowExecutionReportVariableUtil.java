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

import com.ericsson.oss.services.flowautomation.model.FlowExecution;

/**
 * The type Flow execution report variable utilities.
 */
public final class FlowExecutionReportVariableUtil {
    private static final String REPORT_VARIABLE_URI_FORMAT = "/flow-automation/v1/executions/%s/download/report-variable/%s?flow-id=%s";
    private static final String REPORT_ALL_URI_FORMAT = "/flow-automation/v1/executions/%s/download/all?flow-id=%s";

    private FlowExecutionReportVariableUtil() {
    }

    /**
     * Create download url for the report variable.
     *
     * @param flowExecution the flow execution
     * @param variableName  the variable name
     * @return the string
     */
    public static String createDownloadUrl(final FlowExecution flowExecution, final String variableName) {
        return String.format(REPORT_VARIABLE_URI_FORMAT, flowExecution.getName(), variableName, flowExecution.getFlowId());
    }

    /**
     * Extract variable name from the download url string.
     *
     * @param variableDownloadUrl the variable download url
     * @return the string
     */
    public static String extractVariableNameFromUrl(final String variableDownloadUrl) {
        final int reportVariableUrlPathLen = "report-variable/".length();
        final int variableNameStartIndex = variableDownloadUrl.indexOf("report-variable/") + reportVariableUrlPathLen;
        return variableNameStartIndex < reportVariableUrlPathLen ? "" : variableDownloadUrl.substring(variableNameStartIndex, variableDownloadUrl.indexOf("?flow-id="));
    }
}
