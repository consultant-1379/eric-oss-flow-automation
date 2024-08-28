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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.internal.report.RealReporter;
import com.ericsson.oss.services.flowautomation.internal.report.ReporterInternal;
import com.ericsson.oss.services.flowautomation.jpa.EntityManagerWrapper;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;

public class TestReporterInternal implements ReporterInternal {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestReporterInternal.class);

    @Override
    public void updateReportVariable(final String businessKey, final String name, final String value) {
        LOGGER.debug("ReporterBean: updateReportSummary, businessKey={}, summary={}", businessKey, value);

        final EntityManagerWrapper emw = JseTestContext.getInstance().getWeldContainer().select(EntityManagerWrapper.class).get();

        if (emw.getEntityManager() != null) {
            doUpdate(businessKey, name, value);
        } else {
            try {
                emw.createEntityManager();
                emw.getEntityManager().getTransaction().begin();
                doUpdate(businessKey, name, value);
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
    public FlowExecutionResource generateReport(String businessKey) {
        throw new UnsupportedOperationException();
    }

    private void doUpdate(final String businessKey, final String name, final String value) {
        final RealReporter realReporter = JseTestContext.getInstance().getWeldContainer().select(RealReporter.class).get();
        realReporter.updateReportVariable(businessKey, name, value);
    }
}
