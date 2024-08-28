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

package com.ericsson.oss.services.flowautomation.test.fwk.jse;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType.Value.TEST_JAR_TYPE;

import java.util.List;
import java.util.Map;

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
import com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType;

@TestServiceType(TEST_JAR_TYPE)
public class TestFlowExecutionService implements FlowExecutionService {

    @Inject
    @ServiceType(JAR_TYPE)
    private FlowExecutionService flowExecutionService;

    @Inject
    private TransactionalOperationExecutor transactionalOperationExecutor;

    @Override
    public List<FlowExecution> getExecutions(final FlowExecutionFilter filter) {
        return transactionalOperationExecutor.execute(() -> flowExecutionService.getExecutions(filter));
    }

    @Override
    public List<UserTask> getUserTasks(final String flowId, final String flowExecutionName) {
        return transactionalOperationExecutor.execute(() -> flowExecutionService.getUserTasks(flowId, flowExecutionName));
    }

    @Override
    public void completeUserTask(final String taskId, final Map<String, Object> variables) {
        transactionalOperationExecutor.execute(() -> {
            flowExecutionService.completeUserTask(taskId, variables);
            return (Void) null;
        });
    }

    @Override
    public void completeUserTask(final String taskId, final Map<String, byte[]> userTaskFileInput, final String usertTaskInput) {
        transactionalOperationExecutor.execute(() -> {
            flowExecutionService.completeUserTask(taskId, userTaskFileInput, usertTaskInput);
            return (Void) null;
        });
    }

    @Override
    public UserTaskSchema getUserTaskSchema(final String taskId) {
        return transactionalOperationExecutor.execute(() -> flowExecutionService.getUserTaskSchema(taskId));
    }

    @Override
    public void deleteExecution(final String flowId, final String flowExecutionName) {
        transactionalOperationExecutor.execute(() -> {
            flowExecutionService.deleteExecution(flowId, flowExecutionName);
            return (Void) null;
        });
    }

    @Override
    public String getExecutionReport(final String flowId, final String flowExecutionName) {
        return transactionalOperationExecutor.execute(() -> flowExecutionService.getExecutionReport(flowId, flowExecutionName));
    }


    @Override
    public String getExecutionReportSchema(final String flowId, final String flowExecutionName) {
        return transactionalOperationExecutor.execute(() -> flowExecutionService.getExecutionReportSchema(flowId, flowExecutionName));
    }

    @Override
    public void suspendExecution(final String flowId, final String flowExecutionName) {
        transactionalOperationExecutor.execute(() -> {
            flowExecutionService.suspendExecution(flowId, flowExecutionName);
            return (Void) null;
        });
    }

    @Override
    public void suspendAllExecutionsForFlowVersion(final String flowId, final String flowVersion) {
        transactionalOperationExecutor.execute(() -> {
            flowExecutionService.suspendAllExecutionsForFlowVersion(flowId, flowVersion);
            return (Void) null;
        });
    }

    @Override
    public void stopExecution(final String flowId, final String flowExecutionName) {
        transactionalOperationExecutor.execute(() -> {
            flowExecutionService.stopExecution(flowId, flowExecutionName);
            return (Void) null;
        });
    }

    @Override
    public String getFlowInputSchemaAndData(final String flowId, final String flowExecutionName) {
        return transactionalOperationExecutor.execute(() -> flowExecutionService.getFlowInputSchemaAndData(flowId, flowExecutionName));
    }

    @Override
    public FlowExecutionResource getExecutionResource(final String flowId, final String flowExecutionName, final String resource) {
        return transactionalOperationExecutor.execute(() -> flowExecutionService.getExecutionResource(flowId, flowExecutionName, resource));
    }

    @Override
    public PaginatedData<FlowExecutionEvent> getExecutionEvents(FlowExecutionEventFilter flowExecutionEventFilter) {
        return transactionalOperationExecutor.execute(() -> flowExecutionService.getExecutionEvents(flowExecutionEventFilter));
    }

    @Override
    public FlowExecutionResource getExecutionReportVariable(String flowId, String flowExecutionName, String variableName) {
        return transactionalOperationExecutor.execute(() -> flowExecutionService.getExecutionReportVariable(flowId, flowExecutionName, variableName));
    }

    @Override
    public void revertUserTask(String taskId) {
        transactionalOperationExecutor.execute(() -> {
            flowExecutionService.revertUserTask(taskId);
            return (Void) null;
        });
    }
}
