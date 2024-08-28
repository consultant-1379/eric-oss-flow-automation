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

package com.ericsson.oss.services.flowautomation.internalflows;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.service.beans.FlowAutomationNewTransactionProviderBean;

/**
 * The type Flow automation internal flows monitor bean.
 */
@Singleton
@Startup
@TransactionAttribute(value = TransactionAttributeType.REQUIRED)
public class FlowAutomationInternalFlowsMonitorBean {

    /**
     * The Constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowAutomationInternalFlowsMonitorBean.class);

    /**
     * The Constant flowAutomationInternalFlowsId.
     */
    private static final Map<String, String> flowAutomationInternalFlowsId = new HashMap<>();

    static {
        flowAutomationInternalFlowsId.put("com.ericsson.oss.fa.internal.flows.houseKeeping", "FA_HOUSE_KEEPING");
        flowAutomationInternalFlowsId.put("com.ericsson.oss.fa.internal.flows.stopflowinstance", "FA_STOP_FLOW_INSTANCE");
        flowAutomationInternalFlowsId.put("com.ericsson.oss.fa.internal.flows.incidentHandling", "FA_INCIDENT_HANDLING");
    }

    @Inject
    protected FlowAutomationNewTransactionProviderBean flowAutomationNewTransactionProviderBean;

    @Inject
    protected FlowAutomationInternalFlowExecutionMonitor flowAutomationInternalFlowExecutionMonitor;

    @Schedule(hour = "*", minute = "*/2", persistent = false)
    private void monitorFlowAutomationInternalFlows() {
        LOGGER.debug("Monitoring Flow Automation Internal Flows..");

        final Consumer<String> monitorInternalFlow = flowAutomationInternalFlowExecutionMonitor::handleInternalFlowExecution;
        for (final String internalFlowId : flowAutomationInternalFlowsId.keySet()) {
            try {
                flowAutomationNewTransactionProviderBean.executeWithNewTransaction(monitorInternalFlow, internalFlowId);
            } catch (final Exception e) {
                LOGGER.error("Exception while monitoring internal flow: {}", internalFlowId, e);
            }
        }
    }
}
