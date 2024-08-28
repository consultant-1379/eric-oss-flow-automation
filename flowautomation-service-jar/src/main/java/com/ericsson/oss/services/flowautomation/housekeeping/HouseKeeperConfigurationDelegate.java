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
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.DEFAULT_INTERVAL;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.DEFAULT_REINDEX_LOCATION;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.DEFAULT_RETENTION_PERIOD;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INTERVAL;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.INTERVAL_TIME_UNIT;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.REINDEX_PATH;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.RETENTION_PERIOD;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.RETENTION_PERIOD_TIME_UNIT;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.SCHEDULED_DATE_TIME;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class HouseKeeperConfigurationDelegate.
 */
public class HouseKeeperConfigurationDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(HouseKeeperConfigurationDelegate.class);

    @Override
    public void execute(final DelegateExecution execution) throws Exception {
        LOGGER.info("Initialize House Keeping configurations...");
        final String interval = System.getProperty(INTERVAL, DEFAULT_INTERVAL);
        final String intervalTimeUnit = System.getProperty(INTERVAL_TIME_UNIT, HouseKeepingIntervalTimeUnit.HOUR.getValue());
        final String retentionPeriod = System.getProperty(RETENTION_PERIOD, DEFAULT_RETENTION_PERIOD);
        final String retentionPeriodTimeUnit = System.getProperty(RETENTION_PERIOD_TIME_UNIT, HouseKeepingRetentionPeriodTimeUnit.DAY.getValue());
        final String reindexPath = System.getProperty(REINDEX_PATH, DEFAULT_REINDEX_LOCATION);

        LOGGER.info("House Keeping will be executed every: {}:{} and it will clean data older than: {}:{}", interval, intervalTimeUnit,
                retentionPeriod, retentionPeriodTimeUnit);

        execution.setVariable(INTERVAL, interval);
        execution.setVariable(INTERVAL_TIME_UNIT, intervalTimeUnit);
        execution.setVariable(RETENTION_PERIOD, retentionPeriod);
        execution.setVariable(RETENTION_PERIOD_TIME_UNIT, retentionPeriodTimeUnit);
        final String adjustedInterval = calculateNextHouseKeepingSchedulingTime(interval, intervalTimeUnit);
        execution.setVariable(ADJUSTED_INTERVAL, adjustedInterval);
        execution.setVariable(SCHEDULED_DATE_TIME, getHouseKeepingScheduledTime(adjustedInterval, intervalTimeUnit));
        execution.setVariable(REINDEX_PATH, reindexPath);
    }
}
