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

package com.ericsson.oss.services.flowautomation.model;

import lombok.Getter;
import lombok.ToString;

/**
 * The POJO Class FlowExecution.
 */
@Getter
@ToString
public class FlowExecution {

    private final String name;
    private final String flowId;
    private final String flowName;
    private final String flowVersion;
    private final String createdBy;
    private final String executedBy;
    private final String startTime;
    private final String endTime;
    private final String duration;
    private final String state;
    private final Long numberActiveUserTasks;
    private final String flowSource;
    private String summaryReport;
    private String processInstanceId;
    private String processInstanceBusinessKey;

    /**
     * Instantiates a new flow execution.
     *
     * @param name
     *         the name
     * @param flowId
     *         the flow id
     * @param flowName
     *         the flow name
     * @param flowVersion
     *         the flow version
     * @param createdBy
     *         the created by
     * @param executedBy
     *         the executed by
     * @param startTime
     *         the start time
     * @param endTime
     *         the end time
     * @param duration
     *         the duration
     * @param summaryReport
     *         the summary report
     * @param state
     *         the state
     * @param numberActiveUserTasks
     *         the number active user tasks
     * @param flowSource
     *         the flow source
     * @param processInstanceId
     *         the process instance id
     * @param processInstanceBusinessKey
     *         the process instance business key
     */
    @SuppressWarnings("squid:S00107")
    public FlowExecution(final String name, final String flowId, final String flowName, final String flowVersion, final String createdBy,
                         final String executedBy, final String startTime, final String endTime, final String duration, final String summaryReport, final String state,
                         final Long numberActiveUserTasks, final String flowSource, final String processInstanceId, final String processInstanceBusinessKey) {
        this.name = name;
        this.flowId = flowId;
        this.flowName = flowName;
        this.flowVersion = flowVersion;
        this.createdBy = createdBy;
        this.executedBy = executedBy;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.summaryReport = summaryReport;
        this.state = state;
        this.numberActiveUserTasks = numberActiveUserTasks;
        this.flowSource = flowSource;
        this.processInstanceId = processInstanceId;
        this.processInstanceBusinessKey = processInstanceBusinessKey;
    }

    /**
     * Sets summary report.
     *
     * @param summaryReport
     *         the summaryReport to set
     */
    public void setSummaryReport(final String summaryReport) {
        this.summaryReport = summaryReport;
    }

}
