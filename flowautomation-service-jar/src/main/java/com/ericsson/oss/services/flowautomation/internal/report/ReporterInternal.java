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

package com.ericsson.oss.services.flowautomation.internal.report;

import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;

/**
 * Internal interface to update report summary. Needed to provide production and test versions of reporter functionality.
 */
public interface ReporterInternal {

    /**
     * Stores the updated value of the specified report variable for a particular flow execution.
     *
     * @param businessKey the business key of the flow execution that uniquely identifies a execution
     * @param name        the name of the variable whose value needs to be updated
     * @param value       the value to be updated
     */
    void updateReportVariable(final String businessKey, final String name, final String value);

    /**
     * Generate report stream.
     *
     * @param businessKey
     *         the business key
     * @return the file name and content
     */
    FlowExecutionResource generateReport(final String businessKey);
}
