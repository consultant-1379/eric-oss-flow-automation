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

import java.util.List;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionFilter;

/**
 * The Interface FlowExecutionRepository.
 */
public interface FlowExecutionRepository extends Repository<FlowExecutionEntity, Long> {

    /**
     * Flow execution name exists.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     * @return true, if successful
     */
    boolean flowExecutionNameExists(final String flowId, final String flowExecutionName);

    List<FlowExecutionEntity> getFlowExecutions(final FlowExecutionFilter flowExecutionFilter);
    /**
     * Gets the process instance id.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     * @return the process instance id
     */
    String getProcessInstanceId(final String flowId, final String flowExecutionName);

    /**
     * Gets the flow execution.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     * @return the flow execution
     */
    FlowExecutionEntity getFlowExecution(String flowId, String flowExecutionName);

    /**
     * Gets all process instance ids.
     *
     * @return the list of process instance ids
     */
    List getAllProcessInstanceIds();


    /**
     * Gets the flow execution.
     *
     * @param processInstanceId the process instance id
     * @return the flow execution
     */
    FlowExecutionEntity getFlowExecution(final String processInstanceId);

    /**
     * Delete flow execution.
     *
     * @param processInstanceId the process instance id
     * @return the int
     */
    int deleteFlowExecution(String processInstanceId);
}
