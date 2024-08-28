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

import java.util.List;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import com.ericsson.oss.services.flowautomation.execution.resource.FlowPackageResourceLoader;
import com.ericsson.oss.services.flowautomation.execution.resource.FlowPackageResourceSPI;
import com.ericsson.oss.services.flowautomation.jpa.EntityManagerWrapper;

/**
 * Test implementation of FlowPackageResourceSPI to bypass the EJB layer
 */
public class TestFlowPackageResourceSPIImpl implements FlowPackageResourceSPI {
    @Override
    public List<String> list(final String businessKey, final String location) {
        final EntityManagerWrapper emw = JseTestContext.getInstance().getWeldContainer().select(EntityManagerWrapper.class).get();

        if (emw.getEntityManager() != null) {
            return getFlowPackageResourceBean().list(businessKey, location);
        } else {
            try {
                emw.createEntityManager();
                emw.getEntityManager().getTransaction().begin();
                return getFlowPackageResourceBean().list(businessKey, location);
            } catch (final PersistenceException pe) {
                final EntityTransaction tx = emw.getEntityManager().getTransaction();
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw pe;
            } finally {
                emw.getEntityManager().getTransaction().commit();
                emw.closeEntityManager();
            }
        }

    }

    @Override
    public String get(final String businessKey, final String resourcePath) {
        final EntityManagerWrapper emw = JseTestContext.getInstance().getWeldContainer().select(EntityManagerWrapper.class).get();

        if (emw.getEntityManager() != null) {
            return getFlowPackageResourceBean().get(businessKey, resourcePath);
        } else {
            try {
                emw.createEntityManager();
                emw.getEntityManager().getTransaction().begin();
                return getFlowPackageResourceBean().get(businessKey, resourcePath);
            } catch (final PersistenceException pe) {
                final EntityTransaction tx = emw.getEntityManager().getTransaction();
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw pe;
            } finally {
                emw.getEntityManager().getTransaction().commit();
                emw.closeEntityManager();
            }
        }


    }

    /**
     * Using this JNDI lookup, it allows to switch the context from non CDI to CDI.
     *
     * @return the CDIFlowPackageResource instance
     */
    private FlowPackageResourceLoader getFlowPackageResourceBean() {
        return JseTestContext.getInstance().getWeldContainer().select(FlowPackageResourceLoader.class).get();
    }
}
