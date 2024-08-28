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

import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.RETENTION_PERIOD;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.RETENTION_PERIOD_TIME_UNIT;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flow.utils.ServiceLoaderUtil;

/**
 * The type House keeping runtime stopped instances delegate.
 */
public class HouseKeepingRuntimeStoppedInstancesDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(HouseKeepingRuntimeStoppedInstancesDelegate.class);

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        LOGGER.debug("Cleaning up runtime stopped flow instances...");
        final HouseKeeperInternal houseKeeperInternal = ServiceLoaderUtil.loadExactlyOneInstance(HouseKeeperInternal.class);
        final String retentionPeriod = (String) execution.getVariable(RETENTION_PERIOD);
        final String retentionPeriodTimeUnit = (String) execution.getVariable(RETENTION_PERIOD_TIME_UNIT);
        houseKeeperInternal.cleanRuntimeStoppedFlowInstances(retentionPeriod, retentionPeriodTimeUnit);
    }
}
