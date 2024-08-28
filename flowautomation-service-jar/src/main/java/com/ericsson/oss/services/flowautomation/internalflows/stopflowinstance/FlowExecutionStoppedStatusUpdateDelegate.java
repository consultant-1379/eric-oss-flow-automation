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

package com.ericsson.oss.services.flowautomation.internalflows.stopflowinstance;

import static com.ericsson.oss.services.flowautomation.entities.ReportVariableEntity.FA_INTERNAL_VARIABLE_SUMMARY_REPORT;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STOPPED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowTaskExecutionListener.FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME;
import static com.ericsson.oss.services.flowautomation.internalflows.stopflowinstance.FlowExecutionSuspendCheckDelegate.SUSPENDED_FLOW_EXECUTION_IDS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flow.utils.ServiceLoaderUtil;
import com.ericsson.oss.services.flowautomation.internal.report.ReporterInternal;


/**
 * The type Flow execution stopped status update delegate.
 */
public class FlowExecutionStoppedStatusUpdateDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowExecutionStoppedStatusUpdateDelegate.class);

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        final List<String> suspendedFlowExecutionIds = (List<String>) execution.getVariableLocal(SUSPENDED_FLOW_EXECUTION_IDS);
        LOGGER.debug("Number of flow executions whose state will be updated from STOPPING to STOPPED: [{}]", suspendedFlowExecutionIds.size());
        suspendedFlowExecutionIds.forEach(suspendedFlowExecutionId ->
                execution.getProcessEngineServices().getRuntimeService().setVariable(suspendedFlowExecutionId, FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME, STOPPED.name()));

        final Set<String> updateSummaryReportForSuspendedFlowExecutionIds = new HashSet<>();
        updateSummaryReportForSuspendedFlowExecutionIds.addAll(suspendedFlowExecutionIds);
        final RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
        final List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceIds(updateSummaryReportForSuspendedFlowExecutionIds).list();

        final ReporterInternal reporterInternal = ServiceLoaderUtil.loadExactlyOneInstance(ReporterInternal.class);
        processInstances.forEach(processInstance -> {
            LOGGER.debug("Reporter: updateReportVariable, businessKey={},name={}, value={}", processInstance.getBusinessKey(), FA_INTERNAL_VARIABLE_SUMMARY_REPORT, "Manually Stopped");
            reporterInternal.updateReportVariable(processInstance.getBusinessKey(), FA_INTERNAL_VARIABLE_SUMMARY_REPORT, "Manually Stopped");
        });
    }
}
