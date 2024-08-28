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

package com.ericsson.oss.services.flowautomation.housekeeping;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class InstantHouseKeeperMessageThrowDelegate.
 */
public class InstantHouseKeeperMessageThrowDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantHouseKeeperMessageThrowDelegate.class);

    private static final String DO_HOUSE_KEEPING_NOW = "DO_HOUSE_KEEPING_NOW";

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        LOGGER.info("Instant House Keeping has been requested...");
        final String businessKey = execution.getBusinessKey();
        execution.getProcessEngineServices().getRuntimeService().createMessageCorrelation(DO_HOUSE_KEEPING_NOW)
                .processInstanceBusinessKey(businessKey).correlateWithResult();
    }
}
