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

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import com.ericsson.oss.services.flowautomation.execution.event.EventRecorderSPI;
import com.ericsson.oss.services.flowautomation.execution.event.FlowEventRecorder;
import com.ericsson.oss.services.flowautomation.jpa.EntityManagerWrapper;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;

public class TestEventRecorderSPIImpl implements EventRecorderSPI {

    @Override
    public void recordEvent(final String businessKey, final FlowExecutionEventSeverity severity, final String message, final String target, final String eventData) {

        final EntityManagerWrapper emw = JseTestContext.getInstance().getWeldContainer().select(EntityManagerWrapper.class).get();

        if (emw.getEntityManager() != null) {
            recordFlowEvent(businessKey, severity, message, target, eventData);
        } else {
            try {
                emw.createEntityManager();
                emw.getEntityManager().getTransaction().begin();
                recordFlowEvent(businessKey, severity, message, target, eventData);
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

    private void recordFlowEvent(final String businessKey, final FlowExecutionEventSeverity severity, final String message, final String target, final String eventData) {
        final FlowEventRecorder eventRecorder = JseTestContext.getInstance().getWeldContainer().select(FlowEventRecorder.class).get();
        eventRecorder.recordEvent(businessKey, severity, message, target, eventData);
    }
}
