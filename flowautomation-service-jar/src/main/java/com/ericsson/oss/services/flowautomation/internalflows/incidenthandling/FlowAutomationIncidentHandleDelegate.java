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

import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.BUSINESS_KEY;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.FA_INCIDENT_HANDLER_START_EVENT_ID;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INCIDENT;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INCIDENT_ID;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.PROCESS_INSTANCE_ID;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The type Flow automation incident handle delegate.
 */
public class FlowAutomationIncidentHandleDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowAutomationIncidentHandleDelegate.class);

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        final Map<String, Object> incidentMap = (Map<String, Object>) execution.getVariable(INCIDENT);
        final String incidentId = (String) incidentMap.get(INCIDENT_ID);
        final String processInstanceId = (String) incidentMap.get(PROCESS_INSTANCE_ID);
        final String businessKey = (String) incidentMap.get(BUSINESS_KEY);
        LOGGER.info("Handling incident, incidentId={}, processInstanceId={}, businessKey={}", incidentId, processInstanceId, businessKey);
        execution.getProcessEngineServices().getRuntimeService().createProcessInstanceModification(processInstanceId).startBeforeActivity(FA_INCIDENT_HANDLER_START_EVENT_ID).execute(true, true);
    }
}
