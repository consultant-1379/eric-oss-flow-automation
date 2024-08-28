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

package com.ericsson.oss.services.flowautomation.repo;

import com.ericsson.oss.services.flowautomation.entities.ReportVariableEntity;

import java.util.List;
import java.util.Set;

/**
 * The Interface ReportVariableRepository.
 */
public interface ReportVariableRepository extends Repository<ReportVariableEntity, Long> {

    /**
     * Delete report variable.
     *
     * @param businessKeyId the business key id
     * @param variable      the variable
     * @return the int
     */
    int deleteReportVariable(Long businessKeyId, String variable);


    /**
     * Gets summary report for a execution represented by its {@code businessKeyId}.
     *
     * @param businessKeyId the business key id
     * @return the summary report
     */
    String getSummaryReport(final Long businessKeyId);

    /**
     * Gets report variable value.
     *
     * @param businessKeyId the business key id
     * @param variable      the variable
     * @return the report variable value
     */
    String getReportVariableValue(long businessKeyId, String variable);

    /**
     * Gets report variables within given size limit
     * @param flowExecutionId
     * @param size
     * @return
     */
    List<ReportVariableEntity> getReportVariablesWithinSize(long flowExecutionId, int size);

    /**
     * Gets the reports variables beyond  given size limit
     * @param flowExecutionId
     * @param size
     * @return
     */
    Set<String> getVariableNamesBeyondSize(long flowExecutionId, int size);

    /**
     * Gets the hidden report variables
     * @param flowExecutionId
     * @param size
     * @return
     */
    Set<String> getHiddenReportVariableNames(long flowExecutionId);
}
