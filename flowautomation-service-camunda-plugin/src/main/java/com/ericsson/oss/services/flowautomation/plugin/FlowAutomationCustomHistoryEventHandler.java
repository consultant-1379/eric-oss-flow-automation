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

package com.ericsson.oss.services.flowautomation.plugin;

import java.util.List;

import org.camunda.bpm.engine.impl.history.event.HistoricProcessInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.DbHistoryEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Flow automation custom history event handler.
 */
public class FlowAutomationCustomHistoryEventHandler extends DbHistoryEventHandler {

    private static final String FLOW_ID_HOUSE_KEEPING = "com.ericsson.oss.fa.internal.flows.houseKeeping";
    private static final String FLOW_ID_STOP_FLOW_INSTANCE = "com.ericsson.oss.fa.internal.flows.stopflowinstance";
    private static final String FLOW_ID_INCIDENT_HANDLER = "com.ericsson.oss.fa.internal.flows.incidentHandling";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME = "FAInternal_EXECUTION_STATE";
    public static final String FA_INTERNAL = "FAInternal";
    public static final String FA_ACTIVITY_INSTANCE_NAME = "faActivityInstanceName";
    public static final String FA_NAME_EXTRA = "FA_NAME_EXTRA";

    @Override
    public void handleEvent(HistoryEvent historyEvent) {
        final String processDefinitionKey = historyEvent.getProcessDefinitionKey();
        logger.debug("History event received for processDefinitionKey: {}", processDefinitionKey);
        if (processDefinitionKey.startsWith(FLOW_ID_HOUSE_KEEPING) || processDefinitionKey.startsWith(FLOW_ID_STOP_FLOW_INSTANCE) ||
                    processDefinitionKey.startsWith(FLOW_ID_INCIDENT_HANDLER)) {
            logger.debug("Internal flow History event received. ProcessDefinitionKey: {}, EventType: {}, ProcessInstanceId: {}", processDefinitionKey, historyEvent.getEventType(), historyEvent.getProcessInstanceId());
            if (historyEvent instanceof HistoricProcessInstanceEventEntity) {
                logger.debug("HistoricProcessInstanceEventEntity for Internal flow received. ProcessDefinitionKey: {}", processDefinitionKey);
                super.handleEvent(historyEvent);
            } else if (historyEvent instanceof HistoricVariableUpdateEventEntity) {
                HistoricVariableUpdateEventEntity historicVariableUpdateEventEntity = (HistoricVariableUpdateEventEntity) historyEvent;
                logger.debug("HistoricVariableUpdateEventEntity for Internal flow received. ProcessDefinitionKey: {}, Variablename: {}", processDefinitionKey, historicVariableUpdateEventEntity.getVariableName());
                if (historicVariableUpdateEventEntity.getVariableName().equals(FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME)) {
                    super.handleEvent(historyEvent);
                }
            }
        } else {
            customHistoryEventHandlerForNonInternalFlows(historyEvent,processDefinitionKey);
        }
    }


    @Override
    public void handleEvents(List<HistoryEvent> historyEvents) {
        logger.debug("History events received");
        for (HistoryEvent historyEvent : historyEvents) {
            handleEvent(historyEvent);
        }
    }

    private void customHistoryEventHandlerForNonInternalFlows(final HistoryEvent historyEvent, final String processDefinitionKey){
        if (historyEvent instanceof HistoricVariableUpdateEventEntity) {
            HistoricVariableUpdateEventEntity historicVariableUpdateEventEntity = (HistoricVariableUpdateEventEntity) historyEvent;
            final String variableName = historicVariableUpdateEventEntity.getVariableName();
            logger.debug("HistoricVariableUpdateEventEntity received. ProcessDefinitionKey: {}, variableName: {}", processDefinitionKey, variableName);
            if (isVariableRequiredInHistory(variableName)){
                super.handleEvent(historyEvent);
            }
        } else {
            super.handleEvent(historyEvent);
        }
    }

    private boolean isVariableRequiredInHistory(final String variableName) {
        return (variableName.startsWith(FA_INTERNAL) && !variableName.startsWith("FAInternal_FLOW_INPUT_"))||
                variableName.startsWith(FA_ACTIVITY_INSTANCE_NAME) ||
                variableName.equals(FA_NAME_EXTRA);
    }
}
