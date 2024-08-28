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

package com.ericsson.oss.services.flowautomation.service.beans;

import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;

/**
 * The type Flow automation new transaction provider bean.
 */
@Stateless
@TransactionAttribute(value = REQUIRES_NEW)
public class FlowAutomationNewTransactionProviderBean {

    /**
     * Execute with new transaction.
     *
     * @param function  the function
     * @param parameter the parameter
     */
    public void executeWithNewTransaction(final Consumer<String> function, final String parameter) {
        function.accept(parameter);
    }

    /**
     * Execute with new transaction.
     *
     * @param function the function
     */
    public void executeWithNewTransaction(final Supplier<Void> function) {
        function.get();
    }


    /**
     * Execute with transaction collection.
     *
     * @param function the function
     * @return the collection
     */
    public Collection<List<String>> executeWithTransaction(final Supplier<Collection<List<String>>> function) {
        return function.get();
    }

    /**
     * Execute with new transaction.
     *
     * @param function  the function
     * @param parameter the parameter
     */
    public void executeWithNewTransaction(final Consumer<List<String>> function, final List<String> parameter) {
        function.accept(parameter);
    }

    /**
     * Execute with new transaction.
     *
     * @param function  the function
     * @param parameter1 the parameter 1
     * @param parameter2 the parameter 2
     */
    public void executeWithNewTransaction(final BiConsumer<List<String>, List<String>> function, final List<String> parameter1, final List<String> parameter2) {
        function.accept(parameter1, parameter2);
    }
}
