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

package com.ericsson.oss.services.flowautomation.housekeeping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.service.beans.FlowAutomationNewTransactionProviderBean;

/**
 * The Class HouseKeeperBean.
 */
@Stateless
public class HouseKeeperBean implements HouseKeeperLocal {

    @Inject
    private HouseKeeper houseKeeper;

    @Inject
    private InternalFlowsHouseKeeper internalFlowsHouseKeeper;

    @Inject
    private FlowAutomationNewTransactionProviderBean flowAutomationNewTransactionProviderBean;

    private static final Logger LOGGER = LoggerFactory.getLogger(HouseKeeperBean.class);

    @Override
    public void performHouseKeeping(final String retentionPeriod, final String retentionPeriodTimeUnit) {
        LOGGER.debug("Perform house keeping...");
        BiConsumer<List<String>, List<String>> cleanUpHistoricFlowInstances = houseKeeper::cleanUpHistoricFlowInstances;
        final List<HistoricProcessInstance> completedParentProcessInstances = houseKeeper.getCompletedParentProcessInstances(retentionPeriod, retentionPeriodTimeUnit);

        deleteHistoricProcessInstances(completedParentProcessInstances, cleanUpHistoricFlowInstances);
    }

    @Override
    public void performInternalFlowsHouseKeeping() {
        String houseKeepingFlowID = "com.ericsson.oss.fa.internal.flows.houseKeeping%";
        String stopFlowinstanceFlowID = "com.ericsson.oss.fa.internal.flows.stopflowinstance%";
        String incidentHandlingFlowID = "com.ericsson.oss.fa.internal.flows.incidentHandling%";
        ArrayList<String> internalFlowIds = new ArrayList<>(Arrays.asList(houseKeepingFlowID, stopFlowinstanceFlowID, incidentHandlingFlowID));
        Supplier<Void> retriesForDeleteHistoricProcess = () -> houseKeeper.setRetriesForDeleteHistoricProcess();
        Supplier<Void> cleanUpUnknownIncidents = () -> houseKeeper.cleanUpUnknownIncidents();
        Consumer<String> cleanUpInternalFlowHistoricJobLogs = internalFlowsHouseKeeper::cleanUpInternalFlowHistoricJobLogs;
        Consumer<String> cleanUpInternalFlowHistoricTaskInstances = internalFlowsHouseKeeper::cleanUpinternalFlowHistoricTaskInstances;
        Consumer<String> cleanUpInternalFlowHistoricVariableInstances = internalFlowsHouseKeeper::cleanUpInternalFlowHistoricVariableInstances;
        Consumer<String> cleanUpInternalFlowHistoricActivityInstances = internalFlowsHouseKeeper::cleanUpInternalFlowHistoricActivityInstances;
        Consumer<String> cleanUpInternalFlowHistoricProcessInstances = internalFlowsHouseKeeper::cleanUpInternalFlowHistoricProcessInstances;
        Supplier<Collection<List<String>>> getFlowByteArrayVariables = internalFlowsHouseKeeper::getFlowByteArrayVariables;
        Consumer<List<String>> cleanUpInternalFlowByteArrayVariables = internalFlowsHouseKeeper::cleanUpInternalFlowByteArrayVariables;

        ArrayList<Consumer<String>> cleanupJobsConsumer = new ArrayList<>(Arrays.asList(cleanUpInternalFlowHistoricJobLogs, cleanUpInternalFlowHistoricTaskInstances, cleanUpInternalFlowHistoricVariableInstances, cleanUpInternalFlowHistoricActivityInstances));
        ArrayList<Supplier<Void>> cleanupJobsSupplier = new ArrayList<>(Arrays.asList(retriesForDeleteHistoricProcess,cleanUpUnknownIncidents));
        LOGGER.debug("Perform house keeping for internal flows...");

        for (String internalFlowId : internalFlowIds) {
            for (Consumer<String> cleanup : cleanupJobsConsumer) {
                try {
                    flowAutomationNewTransactionProviderBean.executeWithNewTransaction(cleanup, internalFlowId);
                } catch (Exception e) {
                    LOGGER.error("Failed to {} for {}", cleanup, internalFlowId.substring(internalFlowId.lastIndexOf('.')+1,internalFlowId.length()-1), e);
                }
            }
        }
        for (Supplier<Void> cleanup : cleanupJobsSupplier) {
            try {
                flowAutomationNewTransactionProviderBean.executeWithNewTransaction(cleanup);
            } catch (Exception e) {
                LOGGER.error("Failed to {}", cleanup, e);
            }
        }
        try {
            flowAutomationNewTransactionProviderBean.executeWithNewTransaction(cleanUpInternalFlowHistoricProcessInstances, stopFlowinstanceFlowID);
        } catch (Exception e) {
            LOGGER.error("Failed to delete internal flow historic process instances", e);
        }
        Collection<List<String>> cleanupByteArrayTableList = flowAutomationNewTransactionProviderBean.executeWithTransaction(getFlowByteArrayVariables);
        if (!cleanupByteArrayTableList.isEmpty()) {
            cleanupByteArrayTable(cleanUpInternalFlowByteArrayVariables,cleanupByteArrayTableList);
        }
    }


    /**
     * Clean runtime stopped flow instances.
     *
     * @param retentionPeriod         the retention period
     * @param retentionPeriodTimeUnit the retention period time unit
     */
    @Override
    public void cleanRuntimeStoppedFlowInstances(final String retentionPeriod, final String retentionPeriodTimeUnit) {
        LOGGER.debug("Clean runtime stopped flow instances..");
        Consumer<List<String>> cleanUpRuntimeStoppedProcessInstances = houseKeeper::cleanUpRuntimeStoppedProcessInstances;
        List<HistoricProcessInstance> stoppedParentProcessInstances = houseKeeper.getStoppedParentProcessInstances(retentionPeriod, retentionPeriodTimeUnit);

        if (stoppedParentProcessInstances != null && !stoppedParentProcessInstances.isEmpty()) {
            final List<String> processInstanceIdsToBeDeleted = new ArrayList<>();
            stoppedParentProcessInstances.forEach(stoppedParentProcessInstance -> {
                final List<ProcessInstance> processInstances = houseKeeper.getRootProcessInstancesPerBusinessKey(stoppedParentProcessInstance.getBusinessKey());
                processInstances.forEach(processInstance ->
                        processInstanceIdsToBeDeleted.add(processInstance.getId()));
                if (processInstanceIdsToBeDeleted.size() >= 1000) {
                    flowAutomationNewTransactionProviderBean.executeWithNewTransaction(cleanUpRuntimeStoppedProcessInstances, processInstanceIdsToBeDeleted);
                    processInstanceIdsToBeDeleted.clear();
                }
            });
            if (!processInstanceIdsToBeDeleted.isEmpty()) {
                flowAutomationNewTransactionProviderBean.executeWithNewTransaction(cleanUpRuntimeStoppedProcessInstances, processInstanceIdsToBeDeleted);
            }
        }
    }

    @Override
    public void cleanHistoricStoppedFlowInstances(final String retentionPeriod, final String retentionPeriodTimeUnit) {
        LOGGER.debug("Clean historic stopped flow instances..");
        BiConsumer<List<String>, List<String>> cleanUpHistoricFlowInstances = houseKeeper::cleanUpHistoricFlowInstances;
        List<HistoricProcessInstance> stoppedParentProcessInstances = houseKeeper.getStoppedParentProcessInstances(retentionPeriod, retentionPeriodTimeUnit);

        deleteHistoricProcessInstances(stoppedParentProcessInstances, cleanUpHistoricFlowInstances);
    }

    private void cleanupByteArrayTable(Consumer<List<String>> cleanUpInternalFlowByteArrayVariables, Collection<List<String>> cleanupByteArrayTableList){
        for (List<String> innerList : cleanupByteArrayTableList) {
            try {
                flowAutomationNewTransactionProviderBean.executeWithNewTransaction(cleanUpInternalFlowByteArrayVariables, innerList);
            } catch (Exception e) {
                LOGGER.error("Failed to clean up bytearray for batch", e);
            }
        }
    }

    private void deleteHistoricProcessInstances(List<HistoricProcessInstance> parentProcessInstances, BiConsumer<List<String>, List<String>> historicFlowInstances) {
        if (parentProcessInstances != null && !parentProcessInstances.isEmpty()) {
            final List<String> processInstanceIdsToBeDeleted = new ArrayList<>();
            final List<String> businessKeyIdsToBeDeleted = new ArrayList<>();
            parentProcessInstances.forEach(parentProcessInstance -> {
                final String businessKey = parentProcessInstance.getBusinessKey();
                final FlowBusinessKey flowBusinessKey = new FlowBusinessKey(businessKey);
                final String businessKeyId = flowBusinessKey.getBusinessKeyId();
                businessKeyIdsToBeDeleted.add(businessKeyId);
                final List<HistoricProcessInstance> historicProcessInstances = houseKeeper.getHistoricProcessInstancesPerBusinessKey(businessKey);
                historicProcessInstances.forEach(processInstance ->
                        processInstanceIdsToBeDeleted.add(processInstance.getId()));
                if (businessKeyIdsToBeDeleted.size() >= 100 || processInstanceIdsToBeDeleted.size() >= 1000) {
                    flowAutomationNewTransactionProviderBean.executeWithNewTransaction(historicFlowInstances, businessKeyIdsToBeDeleted, processInstanceIdsToBeDeleted);
                    businessKeyIdsToBeDeleted.clear();
                    processInstanceIdsToBeDeleted.clear();
                }
            });
            if (!businessKeyIdsToBeDeleted.isEmpty()) {
                flowAutomationNewTransactionProviderBean.executeWithNewTransaction(historicFlowInstances, businessKeyIdsToBeDeleted, processInstanceIdsToBeDeleted);
            }
        }
    }
}
