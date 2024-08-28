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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;
import com.ericsson.oss.services.utils.JNDIUtil;

/**
 * The Event recorder SPI Implementation, helps to change the context from camunda to the Flowautomation ear for the Event recorder APIs.
 * So, when the flow uses some of the JAVA APIs (EventRecorder in this case) provided within the EAR, this allows to change the context to EAR and hence allows to interact with java code deployed withing EAR.
 */
public class EventRecorderSPIImpl implements EventRecorderSPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventRecorderSPIImpl.class);

    @Override
    public void recordEvent(final String businessKey, final FlowExecutionEventSeverity severity, final String message, final String target, final String eventData) {
        final CDIEventRecorder eventRecorder =
            (CDIEventRecorder) JNDIUtil.findLocalServiceImplementationForInterface(CDIEventRecorder.class.getName());
        if (eventRecorder != null) {
            eventRecorder.recordEvent(businessKey, severity, message, target, eventData);
        } else {
            LOGGER.error("Failed to find CDIEventRecorder Implementation");
        }
    }
}
