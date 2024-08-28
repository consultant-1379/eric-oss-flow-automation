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

package com.ericsson.oss.services.flowautomation.test.fwk.jse;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.housekeeping.HouseKeeper;
import com.ericsson.oss.services.flowautomation.housekeeping.HouseKeeperInternal;
import com.ericsson.oss.services.flowautomation.jpa.EntityManagerWrapper;

/**
 * The Class TestHouseKeepingInternal.
 */
public class TestHouseKeepingInternal implements HouseKeeperInternal {

    @Override
    public void performHouseKeeping(final String retentionPeriod, final String retentionPeriodTimeUnit) {

        final EntityManagerWrapper emw = JseTestContext.getInstance().getWeldContainer().select(EntityManagerWrapper.class).get();

        if (emw.getEntityManager() != null) {
            final HouseKeeper houseKeeper = JseTestContext.getInstance().getWeldContainer().select(HouseKeeper.class).get();
            performHouseKeeping(houseKeeper, retentionPeriod, retentionPeriodTimeUnit);
        } else {
            try {
                emw.createEntityManager();
                emw.getEntityManager().getTransaction().begin();
                final HouseKeeper houseKeeper = JseTestContext.getInstance().getWeldContainer().select(HouseKeeper.class).get();
                performHouseKeeping(houseKeeper, retentionPeriod, retentionPeriodTimeUnit);
                emw.getEntityManager().getTransaction().commit();
            } catch (final PersistenceException pe) {
                final EntityTransaction tx = emw.getEntityManager().getTransaction();
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw pe;
            } finally {
                emw.closeEntityManager();
            }
        }
    }

    @Override
    public void performInternalFlowsHouseKeeping() {
        // For testing, this is not required.
    }

    /**
     * Clean runtime stopped flow instances.
     *
     * @param retentionPeriod         the retention period
     * @param retentionPeriodTimeUnit the retention period time unit
     */
    public void cleanRuntimeStoppedFlowInstances(final String retentionPeriod, final String retentionPeriodTimeUnit) {
        final EntityManagerWrapper emw = JseTestContext.getInstance().getWeldContainer().select(EntityManagerWrapper.class).get();

        if (emw.getEntityManager() != null) {
            final HouseKeeper houseKeeper = JseTestContext.getInstance().getWeldContainer().select(HouseKeeper.class).get();
            cleanRuntimeStoppedFlowInstances(houseKeeper, retentionPeriod, retentionPeriodTimeUnit);
        } else {
            try {
                emw.createEntityManager();
                emw.getEntityManager().getTransaction().begin();
                final HouseKeeper houseKeeper = JseTestContext.getInstance().getWeldContainer().select(HouseKeeper.class).get();
                cleanRuntimeStoppedFlowInstances(houseKeeper, retentionPeriod, retentionPeriodTimeUnit);
                emw.getEntityManager().getTransaction().commit();
            } catch (final PersistenceException pe) {
                final EntityTransaction tx = emw.getEntityManager().getTransaction();
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw pe;
            } finally {
                emw.closeEntityManager();
            }
        }
    }

    /**
     * Clean historic stopped flow instances.
     *
     * @param retentionPeriod         the retention period
     * @param retentionPeriodTimeUnit the retention period time unit
     */
    @Override
    public void cleanHistoricStoppedFlowInstances(final String retentionPeriod, final String retentionPeriodTimeUnit) {
        final EntityManagerWrapper emw = JseTestContext.getInstance().getWeldContainer().select(EntityManagerWrapper.class).get();

        if (emw.getEntityManager() != null) {
            final HouseKeeper houseKeeper = JseTestContext.getInstance().getWeldContainer().select(HouseKeeper.class).get();
            cleanHistoricStoppedFlowInstances(houseKeeper, retentionPeriod, retentionPeriodTimeUnit);
        } else {
            try {
                emw.createEntityManager();
                emw.getEntityManager().getTransaction().begin();
                final HouseKeeper houseKeeper = JseTestContext.getInstance().getWeldContainer().select(HouseKeeper.class).get();
                cleanHistoricStoppedFlowInstances(houseKeeper, retentionPeriod, retentionPeriodTimeUnit);
                emw.getEntityManager().getTransaction().commit();
            } catch (final PersistenceException pe) {
                final EntityTransaction tx = emw.getEntityManager().getTransaction();
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw pe;
            } finally {
                emw.closeEntityManager();
            }
        }
    }

    private void performHouseKeeping(final HouseKeeper houseKeeper, final String retentionPeriod, final String retentionPeriodTimeUnit) {
        final List<HistoricProcessInstance> completedParentProcessInstances = houseKeeper.getCompletedParentProcessInstances(retentionPeriod, retentionPeriodTimeUnit);
        if (completedParentProcessInstances != null && !completedParentProcessInstances.isEmpty()) {
            final List<String> processInstanceIdsToBeDeleted = new ArrayList<>();
            final List<String> businessKeyIdsToBeDeleted = new ArrayList<>();
            completedParentProcessInstances.forEach(completedParentProcessInstance -> {
                final String businessKey = completedParentProcessInstance.getBusinessKey();
                final FlowBusinessKey flowBusinessKey = new FlowBusinessKey(businessKey);
                final String businessKeyId = flowBusinessKey.getBusinessKeyId();
                businessKeyIdsToBeDeleted.add(businessKeyId);
                final List<HistoricProcessInstance> historicProcessInstances = houseKeeper.getHistoricProcessInstancesPerBusinessKey(businessKey);
                historicProcessInstances.forEach(processInstance ->
                        processInstanceIdsToBeDeleted.add(processInstance.getId()));
            });
            if (!businessKeyIdsToBeDeleted.isEmpty()) {
                houseKeeper.cleanUpHistoricFlowInstances(businessKeyIdsToBeDeleted, processInstanceIdsToBeDeleted);
            }
        }
    }

    private void cleanRuntimeStoppedFlowInstances(final HouseKeeper houseKeeper, final String retentionPeriod, final String retentionPeriodTimeUnit) {
        List<HistoricProcessInstance> stoppedParentProcessInstances = houseKeeper.getStoppedParentProcessInstances(retentionPeriod, retentionPeriodTimeUnit);
        if (stoppedParentProcessInstances != null && !stoppedParentProcessInstances.isEmpty()) {
            final List<String> processInstanceIdsToBeDeleted = new ArrayList<>();
            stoppedParentProcessInstances.forEach(stoppedParentProcessInstance -> {
                final List<ProcessInstance> processInstances = houseKeeper.getRootProcessInstancesPerBusinessKey(stoppedParentProcessInstance.getBusinessKey());
                processInstances.forEach(processInstance ->
                        processInstanceIdsToBeDeleted.add(processInstance.getId()));
            });
            if (!processInstanceIdsToBeDeleted.isEmpty()) {
                houseKeeper.cleanUpRuntimeStoppedProcessInstances(processInstanceIdsToBeDeleted);
            }
        }
    }

    private void cleanHistoricStoppedFlowInstances(final HouseKeeper houseKeeper, final String retentionPeriod, final String retentionPeriodTimeUnit) {
        List<HistoricProcessInstance> stoppedParentProcessInstances = houseKeeper.getStoppedParentProcessInstances(retentionPeriod, retentionPeriodTimeUnit);
        if (stoppedParentProcessInstances != null && !stoppedParentProcessInstances.isEmpty()) {
            final List<String> processInstanceIdsToBeDeleted = new ArrayList<>();
            final List<String> businessKeyIdsToBeDeleted = new ArrayList<>();
            stoppedParentProcessInstances.forEach(stoppedParentProcessInstance -> {
                final String businessKey = stoppedParentProcessInstance.getBusinessKey();
                final FlowBusinessKey flowBusinessKey = new FlowBusinessKey(businessKey);
                final String businessKeyId = flowBusinessKey.getBusinessKeyId();
                businessKeyIdsToBeDeleted.add(businessKeyId);
                final List<HistoricProcessInstance> historicProcessInstances = houseKeeper.getHistoricProcessInstancesPerBusinessKey(businessKey);
                historicProcessInstances.forEach(processInstance ->
                        processInstanceIdsToBeDeleted.add(processInstance.getId()));
            });
            if (!businessKeyIdsToBeDeleted.isEmpty()) {
                houseKeeper.cleanUpHistoricFlowInstances(businessKeyIdsToBeDeleted, processInstanceIdsToBeDeleted);
            }
        }
    }
}
