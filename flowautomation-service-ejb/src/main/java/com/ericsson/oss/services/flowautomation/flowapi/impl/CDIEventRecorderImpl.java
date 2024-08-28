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

package com.ericsson.oss.services.flowautomation.flowapi.impl;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.services.flowautomation.execution.event.CDIEventRecorder;
import com.ericsson.oss.services.flowautomation.execution.event.FlowEventRecorder;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;

/**
 * The CDI event recorder Stateless bean, allows the context change from non CDI to CDI.
 */
@Stateless
public class CDIEventRecorderImpl implements CDIEventRecorder {

    @Inject
    private FlowEventRecorder flowEventRecorder;

    @Override
    public void recordEvent(final String businessKey, final FlowExecutionEventSeverity severity, final String message, final String target, final String eventData) {
        flowEventRecorder.recordEvent(businessKey, severity, message, target, eventData);
    }
}
