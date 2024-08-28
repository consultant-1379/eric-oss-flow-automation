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

package com.ericsson.oss.services.flowautomation.internalflows;

/**
 * The Class HouseKeeperConstants.
 */
@SuppressWarnings("squid:S1075")
public class FlowAutomationInternalFlowConstants {

    private FlowAutomationInternalFlowConstants() {
        //
    }

    public static final String DEFAULT_RETENTION_PERIOD = "7";
    public static final String DEFAULT_INTERVAL = "24";
    public static final String RETENTION_PERIOD_TIME_UNIT = "retentionPeriodTimeUnit";
    public static final String RETENTION_PERIOD = "retentionPeriod";
    public static final String INTERVAL_TIME_UNIT = "intervalTimeUnit";
    public static final String INTERVAL = "interval";
    public static final String ADJUSTED_INTERVAL = "adjustedInterval";
    public static final String SCHEDULED_DATE_TIME = "scheduledDateTime";
    public static final String DURATION_DESIGNATOR = "P";
    public static final String TIME_DESIGNATOR = "T";
    public static final String HOUR_DESIGNATOR = "H";
    public static final String MINUTE_DESIGNATOR = "M";
    public static final String SECOND_DESIGNATOR = "S";
    public static final String STOP_FLOW_INSTANCE_DEFAULT_POLLING_INTERVAL = "30";
    public static final String STOP_FLOW_INSTANCE_INTERVAL = "stopFlowInstanceInterval";
    public static final String REINDEX_PATH = "reindexPath";
    public static final String DEFAULT_REINDEX_LOCATION = "/usr/local/bin/reindex.sh";

    // Incident Handling constants

    public static final String INCIDENT_HANDLING_INTERVAL = "incidentHandlingInterval";
    public static final String INCIDENT_HANDLING_DEFAULT_INTERVAL = "20";
    public static final String INCIDENT_ID = "incidentId";
    public static final String PROCESS_INSTANCE_ID = "processInstanceId";
    public static final String BUSINESS_KEY = "businessKey";
    public static final String INCIDENTS = "incidents";
    public static final String INCIDENT = "incident";
    public static final String FA_INCIDENT_HANDLER_START_EVENT_ID = "fa-incident-handler-start-event";

}
