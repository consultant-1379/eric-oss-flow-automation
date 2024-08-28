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

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Flow execution suspend delegate.
 */
public class FlowExecutionSuspendDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowExecutionSuspendDelegate.class);
    public static final String PROCESS_INSTANCE_ID_TO_BE_SUSPENDED = "processInstanceIdToBeSuspended";

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        final String processInstanceIdToBeSuspended = (String) execution.getVariableLocal(PROCESS_INSTANCE_ID_TO_BE_SUSPENDED);
        LOGGER.debug("Suspending process instance id: {}", processInstanceIdToBeSuspended);
        final RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
        runtimeService.suspendProcessInstanceById(processInstanceIdToBeSuspended);
    }
}
