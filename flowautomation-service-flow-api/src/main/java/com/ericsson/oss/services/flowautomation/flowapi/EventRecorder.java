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

package com.ericsson.oss.services.flowautomation.flowapi;

import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.execution.event.EventRecorderSPI;
import com.ericsson.oss.services.flowautomation.flow.utils.ServiceLoaderUtil;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;

/**
 * The flow API for recoding the events from withing the flows.
 */
public class EventRecorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventRecorder.class);

    /**
     * Records the Info event with the specified message string.
     *
     * @param execution the execution
     * @param message   the message
     */
    public static void info(final DelegateExecution execution, final String message) {
        info(execution, message, (String) null);
    }

    /**
     * Records the Info event with the specified message string and target.
     *
     * @param execution the execution
     * @param message   the message
     * @param target    the target
     */
    public static void info(final DelegateExecution execution, final String message, final String target) {
        info(execution, message, target, null);
    }

    /**
     * Records the Info event with the specified message string and event data.
     *
     * @param execution the execution
     * @param message   the message
     * @param eventData the event data
     */
    public static void info(final DelegateExecution execution, final String message, final Map<String, Object> eventData) {
        info(execution, message, null, eventData);
    }

    /**
     * Records the Info event with the specified message string, target and event data.
     *
     * @param execution the execution
     * @param message   the message
     * @param target    the target
     * @param eventData the event data
     */
    public static void info(final DelegateExecution execution, final String message, final String target, final Map<String, Object> eventData) {
        record(execution, FlowExecutionEventSeverity.INFO, message, target, eventData);
    }

    /**
     * Records the Warn event with the specified message string.
     *
     * @param execution the execution
     * @param message   the message
     */
    public static void warn(final DelegateExecution execution, final String message) {
        warn(execution, message, (String) null);
    }

    /**
     * Records the Warn event with the specified message string and target.
     *
     * @param execution the execution
     * @param message   the message
     * @param target    the target
     */
    public static void warn(final DelegateExecution execution, final String message, final String target) {
        warn(execution, message, target, null);
    }

    /**
     * Records the Warn event with the specified message string and event data.
     *
     * @param execution the execution
     * @param message   the message
     * @param eventData the event data
     */
    public static void warn(final DelegateExecution execution, final String message, final Map<String, Object> eventData) {
        warn(execution, message, null, eventData);
    }

    /**
     * Records the Warn event with the specified message string, target and event data.
     *
     * @param execution the execution
     * @param message   the message
     * @param target    the target
     * @param eventData the event data
     */
    public static void warn(final DelegateExecution execution, final String message, final String target, final Map<String, Object> eventData) {
        record(execution, FlowExecutionEventSeverity.WARNING, message, target, eventData);
    }

    /**
     * Records the Error event with the specified message string.
     *
     * @param execution the execution
     * @param message   the message
     */
    public static void error(final DelegateExecution execution, final String message) {
        error(execution, message, (String) null);
    }

    /**
     * Records the Error event with the specified message string and target.
     *
     * @param execution the execution
     * @param message   the message
     * @param target    the target
     */
    public static void error(final DelegateExecution execution, final String message, final String target) {
        error(execution, message, target, null);
    }

    /**
     * Records the Error event with the specified message string and event data.
     *
     * @param execution the execution
     * @param message   the message
     * @param eventData the event data
     */
    public static void error(final DelegateExecution execution, final String message, final Map<String, Object> eventData) {
        error(execution, message, null, eventData);
    }

    /**
     * Records the Error event with the specified message string, target and event data.
     *
     * @param execution the execution
     * @param message   the message
     * @param target    the target
     * @param eventData the event data
     */
    public static void error(final DelegateExecution execution, final String message, final String target, final Map<String, Object> eventData) {
        record(execution, FlowExecutionEventSeverity.ERROR, message, target, eventData);
    }


    /**
     * Does the actual recording of the event passed to the above flow APIs.
     *
     * @param execution the execution
     * @param severity  the severity
     * @param message   the message
     * @param target    the target
     * @param eventData the event data
     */
    private static void record(final DelegateExecution execution, final FlowExecutionEventSeverity severity, final String message, final String target, final Map<String, Object> eventData) {
        final EventRecorderSPI eventRecorderSPI = ServiceLoaderUtil.loadExactlyOneInstance(EventRecorderSPI.class);

        if (eventRecorderSPI != null) {
            eventRecorderSPI.recordEvent(execution.getProcessBusinessKey(), severity, message, target, formatAsString(eventData));
        } else {
            LOGGER.error("Failed to load the implementation of EventRecorderSPI, event won't be recorded.");
        }
    }

    /**
     * Serializes the content of a map into a string.
     * e.g output is {key1=value1, key2=value2, key3={submapkey1=value1, submapkey2=value2, submapkey3=value3}}
     *
     * @param map the map
     * @return the string
     */
    @SuppressWarnings("unchecked")
    private static String formatAsString(final Map<String, ?> map) {
        if (map == null) {
            return null;
        }
        return map.keySet().stream()
                .map(key -> {
                    final Object value = map.get(key);
                    if (value instanceof Map) {
                        return key + "=" + formatAsString((Map<String, ?>) value);
                    }
                    return key + "=" + value;
                })
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private EventRecorder() {
    }
}
