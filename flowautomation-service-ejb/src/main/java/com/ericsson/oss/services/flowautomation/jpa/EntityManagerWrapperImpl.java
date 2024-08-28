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

package com.ericsson.oss.services.flowautomation.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Wrapper providing access to entity manager.
 *
 */
public class EntityManagerWrapperImpl implements EntityManagerWrapper {

    @PersistenceContext(unitName = "FA-JPA")
    protected EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public EntityManager createEntityManager() {
        // Does nothing - never called in JEE deployment
        return null;
    }

    @Override
    public void closeEntityManager() {
        // Does nothing - never called in JEE deployment
    }

}
