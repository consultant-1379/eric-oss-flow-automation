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

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.ericsson.oss.services.flowautomation.jpa.EntityManagerWrapper;

@ApplicationScoped
public class EntityManagerWrapperImpl implements EntityManagerWrapper {

    private EntityManagerFactory entityManagerFactory;

    private ThreadLocal<EntityManager> threadLocalEntityManager = new ThreadLocal<>();

    @Override
    @SuppressWarnings("squid:S2259")
    public synchronized EntityManager createEntityManager() {
        if (entityManagerFactory == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory("FA-TEST-JPA");
        }

        if (threadLocalEntityManager != null) {
            threadLocalEntityManager.remove();
        }
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        threadLocalEntityManager.set(entityManager);
        return entityManager;
    }

    @Override
    public EntityManager getEntityManager() {
        return threadLocalEntityManager.get();
    }

    @Override
    public void closeEntityManager() {
        if (threadLocalEntityManager != null) {
            threadLocalEntityManager.get().close();
            threadLocalEntityManager.remove();
        }
    }

}
