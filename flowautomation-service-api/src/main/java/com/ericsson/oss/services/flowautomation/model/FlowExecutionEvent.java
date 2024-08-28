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

package com.ericsson.oss.services.flowautomation.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The type Flow execution event.
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class FlowExecutionEvent {

    private String eventTime;
    private String severity;
    private String target;
    private String message;
    private String eventData;

    /**
     * Instantiates a new Flow execution event.
     *
     * @param eventTime the event time
     * @param severity  the severity
     * @param target    the target
     * @param message   the message
     * @param eventData the event data
     */
    public FlowExecutionEvent(final String eventTime, final String severity, final String target, final String message, final String eventData) {
        this.eventTime = eventTime;
        this.severity = severity;
        this.target = target;
        this.message = message;
        this.eventData = eventData;
    }
}
