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

import com.ericsson.oss.services.flowautomation.flow.utils.ServiceLoaderUtil;

public class InternalFlowsHouseKeeperDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalFlowsHouseKeeperDelegate.class);

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        LOGGER.debug("Executing Internal flows House Keeping...");
        final HouseKeeperInternal houseKeeperInternal = ServiceLoaderUtil.loadExactlyOneInstance(HouseKeeperInternal.class);
        houseKeeperInternal.performInternalFlowsHouseKeeping();
    }
}
