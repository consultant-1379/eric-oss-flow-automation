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

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.CANCELLED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.COMPLETED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STOP;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STOPPED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STOPPING;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.SUSPENDED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowTaskExecutionListener.FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.BUSINESS_KEY;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INCIDENTS;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INCIDENT_ID;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.PROCESS_INSTANCE_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows;

/**
 * The type Flow automation incidents retrieve delegate.
 */
public class FlowAutomationIncidentsRetrieveDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowAutomationIncidentsRetrieveDelegate.class);

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        LOGGER.debug("Getting incidents...");
        final List<Map<String, Object>> rootIncidents = new ArrayList<>();
        final List<Incident> activeIncidents = execution.getProcessEngineServices().getRuntimeService().createIncidentQuery().list();
        for (final Incident activeIncident : activeIncidents) {
            if (activeIncident.getId().equals(activeIncident.getRootCauseIncidentId()) && activeIncident.getProcessInstanceId() !=null) {
                final String processInstanceId = activeIncident.getProcessInstanceId();
                final String businessKey = execution.getProcessEngineServices().getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getBusinessKey();
                FlowBusinessKey flowBusinessKey = new FlowBusinessKey(businessKey);
                boolean isInternalFlow = FlowAutomationInternalFlows.isInternalFlow(flowBusinessKey.getFlowId());
                if (!isInternalFlow) {
                    List<HistoricProcessInstance> historicProcessInstances = execution.getProcessEngineServices().getHistoryService().createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).list();
                    final List<String> historicProcessInstanceIds = new ArrayList<>();
                    historicProcessInstances.stream().forEach(
                            historicProcessInstance -> historicProcessInstanceIds.add(historicProcessInstance.getId()));
                    HistoricVariableInstance variableInstance = execution.getProcessEngineServices().getHistoryService().createHistoricVariableInstanceQuery().processInstanceIdIn(historicProcessInstanceIds.toArray(new String[0])).variableName(FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME).singleResult();
                    final String flowExecutionStatus = (String) variableInstance.getValue();
                    boolean isvalidState = Stream.of(SUSPENDED, COMPLETED, CANCELLED, STOP, STOPPING, STOPPED).noneMatch(entry -> entry.name().equals(flowExecutionStatus));
                    if (isvalidState) {
                        final Map<String, Object> incidentMap = new HashMap<>();
                        incidentMap.put(INCIDENT_ID, activeIncident.getId());
                        incidentMap.put(PROCESS_INSTANCE_ID, processInstanceId);
                        incidentMap.put(BUSINESS_KEY, businessKey);
                        rootIncidents.add(incidentMap);
                    }
                }
            }
        }
        LOGGER.info("{} active root incidents found", rootIncidents.size());
        execution.setVariable(INCIDENTS, rootIncidents);
    }
}
