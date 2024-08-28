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

import javax.inject.Inject;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import com.ericsson.oss.services.flowautomation.jpa.EntityManagerWrapper;

/**
 * The type Transactional operation executor executes the TransactionalOperation by taking care of starting and committing the transaction.
 */
public class TransactionalOperationExecutor {

    @Inject
    private EntityManagerWrapper emw;

    private void createEntityManager() {
        emw.createEntityManager();
    }


    /**
     * Execute the {@code operation} transactionally.
     *
     * @param <T>       the type parameter
     * @param operation the operation
     * @return the t
     */
    public <T> T execute(final TransactionalOperation<T> operation) {
        T result = null;
        try {
            createEntityManager();
            startTransaction();
            result = operation.execute();
            commitTransaction();
        } catch (final PersistenceException pe) {
            handlePersistenceException(pe);
        } finally {
            closeEntityManager();
        }
        return result;
    }

    private void startTransaction() {
        emw.getEntityManager().getTransaction().begin();
    }

    private void commitTransaction() {
        emw.getEntityManager().getTransaction().commit();
    }

    private void closeEntityManager() {
        emw.closeEntityManager();
    }

    private void handlePersistenceException(final PersistenceException pe) {
        final EntityTransaction tx = emw.getEntityManager().getTransaction();
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
        throw pe;
    }
}
