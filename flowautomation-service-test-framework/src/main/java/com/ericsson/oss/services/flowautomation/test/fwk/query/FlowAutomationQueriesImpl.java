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
package com.ericsson.oss.services.flowautomation.test.fwk.query;

import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.api.FlowService;
import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.ericsson.oss.services.flowautomation.model.UserTaskStatus.ACTIVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FlowAutomationQueriesImpl implements FlowAutomationQueries {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected FlowService flowService;
    protected FlowExecutionService flowExecutionService;

    public FlowAutomationQueriesImpl(FlowService flowService, FlowExecutionService flowExecutionService) {
        this.flowService = flowService;
        this.flowExecutionService = flowExecutionService;
    }

    @Override
    public int getNumberFlowsImported() {
        flowService.getFlows().forEach(flowList -> logger.info(flowList.toString()));
        return flowService.getFlows().size();
    }

    @Override
    public List<FlowExecution> getActiveExecutionsForFlow(final String flowId) {
        final List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowId).build());
        return extractActiveExecutionsFromExecutions(flowExecutions);
    }

    @Override
    public List<FlowExecution> getActiveExecutions() {
        final List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().build());
        return extractActiveExecutionsFromExecutions(flowExecutions);
    }

    @Override
    public final List<FlowExecution> getExecutionsForFlow(final String flowId) {
        return flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowId).build());
    }

    @Override
    public List<UserTask> getActiveUsertasksForExecution(final FlowExecution flowExecution) {
        final List<UserTask> usertasks = flowExecutionService.getUserTasks(flowExecution.getFlowId(), flowExecution.getName());
        final List<UserTask> activeUserTasks = new ArrayList<>();
        for (final UserTask usertask : usertasks) {
            if (usertask.getStatus().equals(ACTIVE.getValue())) {
                activeUserTasks.add(usertask);
            }
        }
        return activeUserTasks;
    }

    @Override
    public List<UserTask> getAllUsertasksForExecution(final FlowExecution flowExecution) {
        return flowExecutionService.getUserTasks(flowExecution.getFlowId(), flowExecution.getName());
    }

    @Override
    public List<UserTask> getUserTasks(FlowExecution flowExecution, String usertaskName) {
        final List<UserTask> usertasks = getActiveUsertasksForExecution(flowExecution);
        return usertasks.stream()
                .filter(userTask -> (userTask.getName() + " - " + userTask.getNameExtra()).equals(usertaskName) || userTask.getName().equals(usertaskName))
                .collect(Collectors.toList());

    }

    @Override
    public String getExecutionState(final FlowExecution flowExecution) {
        final List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowExecution.getFlowId()).flowExecutionName(flowExecution.getName()).build());
        assertEquals(1, flowExecutions.size());
        return flowExecutions.get(0).getState();
    }

    @Override
    public String getExecutionInput(final FlowExecution flowExecution) {
        final String flowInputSchemaAndData = flowExecutionService.getFlowInputSchemaAndData(flowExecution.getFlowId(), flowExecution.getName());
        assertNotNull(flowInputSchemaAndData);
        return flowInputSchemaAndData;
    }

    private List<FlowExecution> extractActiveExecutionsFromExecutions(final List<FlowExecution> flowExecutions) {
        final List<FlowExecution> activeFlowExecutions = new ArrayList<>();
        for (final FlowExecution flowExecution : flowExecutions) {
            if (flowExecution.getEndTime() == null) {
                activeFlowExecutions.add(flowExecution);
            }
        }
        return activeFlowExecutions;
    }
}
