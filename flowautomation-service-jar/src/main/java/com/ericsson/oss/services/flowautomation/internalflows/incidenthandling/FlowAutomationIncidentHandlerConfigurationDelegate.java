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

package com.ericsson.oss.services.flowautomation.internalflows.incidenthandling;

import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.DURATION_DESIGNATOR;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INCIDENT_HANDLING_DEFAULT_INTERVAL;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INCIDENT_HANDLING_INTERVAL;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.SECOND_DESIGNATOR;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.TIME_DESIGNATOR;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The type Flow automation incident handler configuration delegate.
 */
public class FlowAutomationIncidentHandlerConfigurationDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowAutomationIncidentHandlerConfigurationDelegate.class);

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        String incidentHandlingInterval = System.getProperty(INCIDENT_HANDLING_INTERVAL, INCIDENT_HANDLING_DEFAULT_INTERVAL);
        LOGGER.info("Incident handling will be executed in {} seconds from now", incidentHandlingInterval);
        incidentHandlingInterval = String.join("", DURATION_DESIGNATOR, TIME_DESIGNATOR, incidentHandlingInterval, SECOND_DESIGNATOR);
        execution.setVariable(INCIDENT_HANDLING_INTERVAL, incidentHandlingInterval);
    }
}
