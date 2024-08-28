/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

package com.ericsson.oss.services.flowautomation.execution.event.impl;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.execution.event.EventRecorderSPI;
import com.ericsson.oss.services.flowautomation.execution.event.api.FlowEventRecorderService;
import com.ericsson.oss.services.flowautomation.flow.utils.ServiceLoaderUtil;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;

/**
 * The Class FlowEventRecorderServiceImpl.
 */
@ServiceType(JAR_TYPE)
public class FlowEventRecorderServiceImpl implements FlowEventRecorderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowEventRecorderServiceImpl.class);

    /**
     * Records event.
     *
     * @param processInstanceBusinessKey
     *            the business key
     * @param severity
     *            the severity
     * @param message
     *            the message
     * @param target
     *            the target
     * @param details
     *            the details of the event
     */
    private void recordEvent(final String processInstanceBusinessKey, final FlowExecutionEventSeverity severity,
                             final String message, final String target, final String details) {
        final EventRecorderSPI eventRecorderSPI = ServiceLoaderUtil.loadExactlyOneInstance(EventRecorderSPI.class);

        if (eventRecorderSPI != null) {
            eventRecorderSPI.recordEvent(processInstanceBusinessKey, severity, message, target, details);
        } else {
            LOGGER.error("Failed to load the implementation of EventRecorderSPI, event won't be recorded.");
        }

    }

    @Override
    public void warn(final String processInstanceBusinessKey, final String message, final String target,
                     final String details) {
        recordEvent(processInstanceBusinessKey, FlowExecutionEventSeverity.WARNING, message, target, details);

    }
}
