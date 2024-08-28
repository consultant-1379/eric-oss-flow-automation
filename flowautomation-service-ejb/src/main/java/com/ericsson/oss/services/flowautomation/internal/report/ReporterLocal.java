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

import javax.ejb.Local;

import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;

/**
 * The Interface ReporterLocal.
 */
@Local
public interface ReporterLocal {

    /**
     * Update report variable.
     *
     * @param businessKey the business key
     * @param name        the name
     * @param value       the value
     */
    void updateReportVariable(String businessKey, String name, String value);

    /**
     * Generate report flow execution resource.
     *
     * @param businessKey
     *         the business key
     * @return the flow execution resource
     */
    FlowExecutionResource generateReport(final String businessKey);
}
