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

import static com.ericsson.oss.services.flowautomation.housekeeping.HouseKeepingScheduler.calculateNextHouseKeepingSchedulingTime;
import static com.ericsson.oss.services.flowautomation.housekeeping.HouseKeepingScheduler.getHouseKeepingScheduledTime;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.ADJUSTED_INTERVAL;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INTERVAL;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INTERVAL_TIME_UNIT;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.SCHEDULED_DATE_TIME;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class HouseKeepingSchedulerDelegate.
 */
public class HouseKeepingSchedulerDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(HouseKeepingSchedulerDelegate.class);

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        LOGGER.info("Determining next house keeping execution time...");
        final String interval = (String) execution.getVariable(INTERVAL);
        final String intervalTimeUnit = (String) execution.getVariable(INTERVAL_TIME_UNIT);
        final String adjustedInterval = calculateNextHouseKeepingSchedulingTime(interval, intervalTimeUnit);
        execution.setVariable(ADJUSTED_INTERVAL, adjustedInterval);
        execution.setVariable(SCHEDULED_DATE_TIME, getHouseKeepingScheduledTime(adjustedInterval, intervalTimeUnit));
    }
}
