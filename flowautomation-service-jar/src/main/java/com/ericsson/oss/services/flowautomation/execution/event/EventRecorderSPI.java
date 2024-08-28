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
 * The interface Event recorder Implementation helps to change the context from camunda to the jboss for the Event recorder APIs.
 * So, when flow uses some of the JAVA APIs (EventRecorder in this case) provided within the EAR, this allows to change the context to EAR and hence allows to interact with java code deployed withing EAR.
 */
public interface EventRecorderSPI {

    /**
     * Records event.
     *
     * @param businessKey the business key
     * @param severity    the severity
     * @param message     the message
     * @param target      the target
     * @param details     the details of the event
     */
    void recordEvent(final String businessKey, final FlowExecutionEventSeverity severity, final String message,
                     final String target, final String details);
}
