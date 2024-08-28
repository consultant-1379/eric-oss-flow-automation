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

import static com.ericsson.oss.services.flowautomation.housekeeping.HouseKeepingIntervalTimeUnit.HOUR;
import static com.ericsson.oss.services.flowautomation.housekeeping.HouseKeepingIntervalTimeUnit.MINUTE;
import static com.ericsson.oss.services.flowautomation.housekeeping.HouseKeepingIntervalTimeUnit.SECOND;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.DURATION_DESIGNATOR;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.HOUR_DESIGNATOR;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.MINUTE_DESIGNATOR;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.SECOND_DESIGNATOR;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlowConstants.TIME_DESIGNATOR;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class HouseKeepingScheduler.
 */
public class HouseKeepingScheduler {

    private HouseKeepingScheduler() {
        //
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HouseKeepingScheduler.class);

    /**
     * Calculate next house keeping execution time.
     *
     * @param interval         the interval
     * @param intervalTimeUnit the interval time unit
     * @return the string
     */
    public static String calculateNextHouseKeepingSchedulingTime(final String interval, final String intervalTimeUnit) {
        final StringBuilder intervalBuilder = new StringBuilder();
        intervalBuilder.append(DURATION_DESIGNATOR + TIME_DESIGNATOR);
        if (HOUR.getValue().equals(intervalTimeUnit)) {
            handleHourIntervalTimeUnit(interval, intervalBuilder);
        } else if (MINUTE.getValue().equals(intervalTimeUnit)) {
            intervalBuilder.append(interval + MINUTE_DESIGNATOR);
        } else if (SECOND.getValue().equals(intervalTimeUnit)) {
            intervalBuilder.append(interval + SECOND_DESIGNATOR);
        } else {
            LOGGER.warn("Unsupported intervalTimeUnit: [{}], hence default intervalTimeUnit: [{}] has been chosen.", intervalTimeUnit,
                    HOUR.getValue());
            handleHourIntervalTimeUnit(interval, intervalBuilder);
        }
        LOGGER.info("House Keeping is scheduled to execute after: {}", intervalBuilder);
        return intervalBuilder.toString();
    }

    private static void handleHourIntervalTimeUnit(final String interval, final StringBuilder intervalBuilder) {
        final Calendar currentDateTime = Calendar.getInstance();
        final int currentHourOfDay = currentDateTime.get(Calendar.HOUR_OF_DAY);
        final int currentMin = currentDateTime.get(Calendar.MINUTE);
        int scheduledHour = 0;
        int scheduledMinute = 0;
        if (currentHourOfDay == 0 && currentMin == 0) {
            scheduledHour = Integer.parseInt(interval);
            scheduledMinute = currentMin;
        } else {
            scheduledHour = Integer.parseInt(interval) - currentHourOfDay - 1;
            scheduledMinute = 60 - currentMin;
        }
        intervalBuilder.append(scheduledHour + HOUR_DESIGNATOR + scheduledMinute + MINUTE_DESIGNATOR);
    }

    /**
     * Gets the house keeping scheduled time.
     *
     * @param adjustedInterval the adjusted interval
     * @param intervalTimeUnit the interval time unit
     * @return the house keeping scheduled time
     */
    public static String getHouseKeepingScheduledTime(final String adjustedInterval, final String intervalTimeUnit) {
        final Calendar currentDateTime = Calendar.getInstance();

        if (HOUR.getValue().equals(intervalTimeUnit)) {
            final String scheduledHour = adjustedInterval.substring(adjustedInterval.indexOf(TIME_DESIGNATOR) + 1,
                    adjustedInterval.indexOf(HOUR_DESIGNATOR));
            final String scheduledMinute = adjustedInterval.substring(adjustedInterval.indexOf(HOUR_DESIGNATOR) + 1,
                    adjustedInterval.indexOf(MINUTE_DESIGNATOR));
            currentDateTime.add(Calendar.HOUR, Integer.parseInt(scheduledHour));
            currentDateTime.add(Calendar.MINUTE, Integer.parseInt(scheduledMinute));
        } else if (MINUTE.getValue().equals(intervalTimeUnit)) {
            final String scheduledMinute = adjustedInterval.substring(adjustedInterval.indexOf(TIME_DESIGNATOR) + 1,
                    adjustedInterval.indexOf(MINUTE_DESIGNATOR));
            currentDateTime.add(Calendar.MINUTE, Integer.parseInt(scheduledMinute));
        } else if (SECOND.getValue().equals(intervalTimeUnit)) {
            final String scheduledSecond = adjustedInterval.substring(adjustedInterval.indexOf(TIME_DESIGNATOR) + 1,
                    adjustedInterval.indexOf(SECOND_DESIGNATOR));
            currentDateTime.add(Calendar.SECOND, Integer.parseInt(scheduledSecond));
        }
        final long timeInMillis = currentDateTime.getTimeInMillis();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(timeInMillis));
    }
}
