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

import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.DURATION_DESIGNATOR;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.SECOND_DESIGNATOR;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.STOP_FLOW_INSTANCE_DEFAULT_POLLING_INTERVAL;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.STOP_FLOW_INSTANCE_INTERVAL;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.TIME_DESIGNATOR;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Stop flow instance configuration delegate.
 */
public class StopFlowInstanceConfigurationDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopFlowInstanceConfigurationDelegate.class);

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        LOGGER.info("Initialize Stop Flow Instance configurations...");
        String stopFlowInstanceInterval = System.getProperty(STOP_FLOW_INSTANCE_INTERVAL, STOP_FLOW_INSTANCE_DEFAULT_POLLING_INTERVAL);
        LOGGER.info("Stop Flow Instance will be executed every: {} seconds.", stopFlowInstanceInterval);
        stopFlowInstanceInterval = String.join("", DURATION_DESIGNATOR, TIME_DESIGNATOR, stopFlowInstanceInterval, SECOND_DESIGNATOR);
        execution.setVariable(STOP_FLOW_INSTANCE_INTERVAL, stopFlowInstanceInterval);
    }
}
