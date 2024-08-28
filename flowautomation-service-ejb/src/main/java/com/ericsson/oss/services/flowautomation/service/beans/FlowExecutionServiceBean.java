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

package com.ericsson.oss.services.flowautomation.service.beans;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.EJB_TYPE;
import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEvent;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventFilter;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionFilter;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import com.ericsson.oss.services.flowautomation.model.PaginatedData;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import com.ericsson.oss.services.flowautomation.model.UserTaskSchema;

/**
 * The {@code FlowServiceBean} is stateless session bean and implements interface {@code Flow}}.
 */
@Stateless
@ServiceType(EJB_TYPE)
public class FlowExecutionServiceBean implements FlowExecutionService {

    /** The flow service */
    @Inject
    @ServiceType(JAR_TYPE)
    private FlowExecutionService flowExecutionService;

    @Inject
    private FlowExecutionsPreMaterializer flowExecutionsPreMaterializer;

    @Inject
    private Event<FlowExecutionDeleteEvent> flowExecutionDeleteEvent;

    /**
     * Default constructor.
     */
    public FlowExecutionServiceBean() {
        //
    }

    @Override
    public List<FlowExecution> getExecutions(final FlowExecutionFilter filter) {
        final List<FlowExecution> executions;
        if (isNotEmpty(filter.getFlowId()) && isNotEmpty(filter.getFlowExecutionName())) { //Always query db if the request is for a single execution or else get from pre materialized cache.
            executions = flowExecutionService.getExecutions(filter);
        } else {
            if (flowExecutionsPreMaterializer.isCacheEmpty()) { //Seed the execution in the cache if empty.
                flowExecutionsPreMaterializer.initializeCache();
            }
            executions = flowExecutionsPreMaterializer.getExecutions();
        }

        return getFilteredByUserExecutions(executions, filter);
    }

    public List<FlowExecution> getFilteredByUserExecutions(final List<FlowExecution> executions, final FlowExecutionFilter filter) {
        return executions.stream()
                .filter(execution -> {
                    boolean includeExecution = true;
                    if (filter.isFilterByUser()) { // Exclude the executions by other users if userFilter is set.
                        if (filter.getUser() == null) {
                            includeExecution = false;
                        } else {
                            includeExecution = filter.getUser().equals(execution.getExecutedBy());
                        }
                    }
                    if (includeExecution && filter.getFlowId() != null) { // If the request is for executions of a specific flow.
                        includeExecution = filter.getFlowId().equals(execution.getFlowId());
                    }

                    return includeExecution;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UserTask> getUserTasks(final String flowId, final String flowExecutionName) {
        return flowExecutionService.getUserTasks(flowId, flowExecutionName);
    }

    @Override
    public void completeUserTask(final String taskId, final Map<String, Object> variables) {
        flowExecutionService.completeUserTask(taskId, variables);
    }

    @Override
    public void completeUserTask(final String taskId, final Map<String, byte[]> userTaskFileInput, final String usertTaskInput) {
        flowExecutionService.completeUserTask(taskId, userTaskFileInput, usertTaskInput);
    }

    @Override
    public UserTaskSchema getUserTaskSchema(final String taskId) {
        return flowExecutionService.getUserTaskSchema(taskId);
    }

    @Override
    public void deleteExecution(final String flowId, final String flowExecutionName) {
        flowExecutionService.deleteExecution(flowId, flowExecutionName);
        flowExecutionDeleteEvent.fire(new FlowExecutionDeleteEvent(flowId, flowExecutionName));
    }

    @Override
    public String getExecutionReport(final String flowId, final String flowExecutionName) {
        return flowExecutionService.getExecutionReport(flowId, flowExecutionName);
    }

    @Override
    public String getExecutionReportSchema(final String flowId, final String flowExecutionName) {
        return flowExecutionService.getExecutionReportSchema(flowId, flowExecutionName);
    }

    @Override
    public void suspendExecution(final String flowId, final String flowExecutionName) {
        flowExecutionService.suspendExecution(flowId, flowExecutionName);
    }

    @Override
    public void suspendAllExecutionsForFlowVersion(final String flowId, final String flowVersion) {
        flowExecutionService.suspendAllExecutionsForFlowVersion(flowId, flowVersion);
    }

    @Override
    public void stopExecution(final String flowId, final String flowExecutionName) {
        flowExecutionService.stopExecution(flowId, flowExecutionName);
    }

    @Override
    public String getFlowInputSchemaAndData(final String flowId, final String flowExecutionName) {
        return flowExecutionService.getFlowInputSchemaAndData(flowId, flowExecutionName);
    }

    @Override
    public FlowExecutionResource getExecutionResource(final String flowId, final String flowExecutionName, final String resource) {
        return flowExecutionService.getExecutionResource(flowId, flowExecutionName, resource);
    }

    @Override
    public PaginatedData<FlowExecutionEvent> getExecutionEvents(final FlowExecutionEventFilter flowExecutionEventFilter) {
        return flowExecutionService.getExecutionEvents(flowExecutionEventFilter);
    }

    @Override
    public FlowExecutionResource getExecutionReportVariable(final String flowId, final String flowExecutionName, final String variableName) {
        return flowExecutionService.getExecutionReportVariable(flowId, flowExecutionName, variableName);
    }

    @Override
    public void revertUserTask(final String taskId) {
        flowExecutionService.revertUserTask(taskId);
    }
}