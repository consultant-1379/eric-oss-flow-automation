/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.services.flowautomation.flow.utils;

import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_CANCELLED;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_COMPLETE;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_CONFIRM_EXECUTE;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_EXECUTING;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_FAILED;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_FAILED_EXECUTE;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_FAILED_SETUP;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_SETTING_UP;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_START;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_STOP;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_STOPPED;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_STOPPING;
import static com.ericsson.oss.services.flowautomation.flow.utils.LogRecordingHelper.EventType.FLOW_EXECUTION_SUSPEND;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State;

/**
 * Supporting logger data and methods
 */
public final class LogRecordingHelper {

    private static final String EMPTY = "";
    public static final String FLOW_AUTOMATION_EXECUTION = "FLOW_AUTOMATION_EXECUTION";

    public enum EventType {
        FLOW_IMPORT,
        FLOW_ENABLED,
        FLOW_DISABLED,
        FLOW_DELETED,
        FLOW_EXECUTION_START,
        FLOW_EXECUTION_SETTING_UP,
        FLOW_EXECUTION_DELETE,
        FLOW_EXECUTION_SUSPEND,
        FLOW_EXECUTION_EXECUTING,
        FLOW_EXECUTION_CONFIRM_EXECUTE,
        FLOW_EXECUTION_COMPLETE,
        FLOW_EXECUTION_STOP,
        FLOW_EXECUTION_STOPPING,
        FLOW_EXECUTION_STOPPED,
        FLOW_EXECUTION_FAILED,
        FLOW_EXECUTION_FAILED_SETUP,
        FLOW_EXECUTION_FAILED_EXECUTE,
        FLOW_EXECUTION_CANCELLED,
        FLOW_EXECUTION_USER_PERMISSION
    }

    public static EventType getEventType(State state) {
        switch (state) {
            case STARTED:
                return FLOW_EXECUTION_START;
            case SETTING_UP:
                return FLOW_EXECUTION_SETTING_UP;
            case CONFIRM_EXECUTE:
                return FLOW_EXECUTION_CONFIRM_EXECUTE;
            case COMPLETED:
                return FLOW_EXECUTION_COMPLETE;
            case CANCELLED:
                return FLOW_EXECUTION_CANCELLED;
            case SUSPENDED:
                return FLOW_EXECUTION_SUSPEND;
            case STOP:
                return FLOW_EXECUTION_STOP;
            case STOPPING:
                return FLOW_EXECUTION_STOPPING;
            case STOPPED:
                return FLOW_EXECUTION_STOPPED;
            case FAILED:
                return FLOW_EXECUTION_FAILED;
            case FAILED_SETUP:
                return FLOW_EXECUTION_FAILED_SETUP;
            case FAILED_EXECUTE:
                return FLOW_EXECUTION_FAILED_EXECUTE;
            default:
                return FLOW_EXECUTION_EXECUTING;
        }
    }

    /**
     * Puts data into a Map for logging
     * @param eventType the event type
     * @param flowId the flowId
     * @param flowExecutionName the name of the flow execution
     * @param duration duration of flow execution
     * @return map with logging data
     */
    public static Map<String, Object> logGenerateToExecution(final EventType eventType, final String flowId, final String flowExecutionName,
                                                             final String duration) {
        HashMap<String, Object> logData = new HashMap<>();
        logData.put("EventType", eventType.name());
        logData.put("FlowId", returnVariableOrEmpty(flowId));
        logData.put("FlowExecutionName", returnVariableOrEmpty(flowExecutionName));
        logData.put("Duration", returnVariableOrEmpty(duration));

        return logData;
    }

    private static String returnVariableOrEmpty(final String variable) {
        return variable == null ? EMPTY : variable;
    }
}
