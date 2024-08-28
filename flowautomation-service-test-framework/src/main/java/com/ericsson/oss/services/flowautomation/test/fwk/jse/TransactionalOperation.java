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

/**
 * The interface Transactional operation to be used by any transactional methods requiring transactions.
 *
 * @param <T> the return type of the method to be executed in a transaction.
 */
public interface TransactionalOperation<T> {
    /**
     * Execute the operation.
     *
     * @return the t
     */
    T execute();
}
