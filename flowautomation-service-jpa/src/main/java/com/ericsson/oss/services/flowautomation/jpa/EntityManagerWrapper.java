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

/**
 * Wrapper providing access to entity manager.
 * 
 */
public interface EntityManagerWrapper {

    EntityManager createEntityManager();
    
    EntityManager getEntityManager();
    
    void closeEntityManager();
}
