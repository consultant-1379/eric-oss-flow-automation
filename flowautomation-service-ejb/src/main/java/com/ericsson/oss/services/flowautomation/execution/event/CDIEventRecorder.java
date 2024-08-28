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

package com.ericsson.oss.services.flowautomation.execution.event;

import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;

/**
 * The interface CDI event recorder, allows the context change from non CDI to CDI.
 */
public interface CDIEventRecorder {
    /**
     * Record event.
     *
     * @param businessKey the business key
     * @param severity    the severity
     * @param message     the message
     * @param target      the target
     * @param eventData   the event data
     */
    void recordEvent(final String businessKey, FlowExecutionEventSeverity severity, final String message, final String target, final String eventData);
}
