/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.services.flowautomation.backuprestore;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;
import static com.ericsson.oss.services.flowautomation.entities.ReportVariableEntity.FA_INTERNAL_VARIABLE_SUMMARY_REPORT;
import static com.ericsson.oss.services.flowautomation.error.BackupRestoreErrorCode.ACTION_NOT_COMPLETED;

import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.exception.BackupRestoreException;
import com.ericsson.oss.services.flowautomation.exception.EntityNotFoundException;
import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.flow.AbstractFlowAutomationService;
import com.ericsson.oss.services.flowautomation.flow.exception.OperationNotAllowedException;
import com.ericsson.oss.services.flowautomation.flow.utils.ServiceLoaderUtil;
import com.ericsson.oss.services.flowautomation.internal.report.ReporterInternal;
import com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows;
import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.brapi.RestoreHandler;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;

@ServiceType(JAR_TYPE)
public class RestoreHandlerImpl extends AbstractFlowAutomationService implements RestoreHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestoreHandlerImpl.class);
    private static final String RESTORED_SUMMARY_REPORT_VALUE = "Restored";

    @Inject
    @ServiceType(JAR_TYPE)
    FlowExecutionService flowExecutionService;

    private final ReporterInternal reporterInternal = ServiceLoaderUtil.loadExactlyOneInstance(ReporterInternal.class);

    public void suspendExecutionsAfterRestore() {
        LOGGER.info("The system is in restore. Trying to mark all the flow executions in SUSPENDED state.");
        AtomicBoolean allExecutionsSuspended = new AtomicBoolean(true);
        flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().build()).stream()
                .filter(execution -> !FlowAutomationInternalFlows.isInternalFlow(execution.getFlowId()))
                .forEach(flowExecution -> {
                    try {
                        LOGGER.info("Trying to suspend flow execution as part of restore procedure.. {}", flowExecution);
                        flowExecutionService.suspendExecution(flowExecution.getFlowId(), flowExecution.getName());
                        reporterInternal.updateReportVariable(flowExecution.getProcessInstanceBusinessKey(), FA_INTERNAL_VARIABLE_SUMMARY_REPORT, RESTORED_SUMMARY_REPORT_VALUE);
                    } catch (final OperationNotAllowedException | EntityNotFoundException e) { // OperationNotAllowedException can be thrown if the execution is not suspendable, e.g in setup phase etc.
                        LOGGER.warn("The flow execution: {} can't be suspended, reason: {}", flowExecution, e.getMessage());
                    } catch (final Exception e) { // Since it will be a partial restore (not all executions suspended) so setting allExecutionsSuspended to false
                        allExecutionsSuspended.set(false);
                        LOGGER.warn("Failed to suspend flow execution: {}, Continuing with suspending others.", flowExecution, e);
                    }
                });
        if (!allExecutionsSuspended.get()) {
            LOGGER.error("Unable to suspend all flow executions");
            throw new BackupRestoreException(ACTION_NOT_COMPLETED, ACTION_NOT_COMPLETED.reason());
        }
    }

    @Override
    public void preRestoreActions() {
        //  making this method as close to idempotent as possible. If a second request is received, it will be considered duplicate if the process engine not active.
        LOGGER.info("Executing pre-restore actions.");
        try {
            LOGGER.info("Checking if Job Executor for {} Process Engine is active.", processEngine.getName());
            if (!isProcessEngineActive()) {
                // we assume it's a duplicate request, we want to make this idempotent
                LOGGER.warn("Process engine is not active. This seems like a duplicate request.");
            } else {
                LOGGER.info("Deactivating Job Executor for {} Process Engine.", processEngine.getName());
                ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getJobExecutor().shutdown();
            }
        } catch (final Exception e) {
            LOGGER.error("An error occurred in stop job executor operation.", e);
            throw new BackupRestoreException(ACTION_NOT_COMPLETED, ACTION_NOT_COMPLETED.reason());
        }
    }

    @Override
    public void postRestoreActions() {
        // making this method as close to idempotent as possible. If a second request is received to do the same method,
        // the state of the process engine will be ignored.
        LOGGER.info("Executing post-restore actions.");
        try {
            LOGGER.info("Checking if Job Executor for {} Process Engine is active.", processEngine.getName());
            if (isProcessEngineActive()) {
                LOGGER.warn("Process engine is active. This seems like a duplicate request.");
            } else {
                suspendExecutionsAfterRestore();
                LOGGER.info("Activating Job Executor for {} Process Engine.", processEngine.getName());
                ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getJobExecutor().start();
            }
        } catch (final Exception e) {
            LOGGER.error("An error occurred in start job executor operation.", e);
            throw new BackupRestoreException(ACTION_NOT_COMPLETED, ACTION_NOT_COMPLETED.reason());
        }
    }

    private boolean isProcessEngineActive() {
        return ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getJobExecutor().isActive();
    }

}