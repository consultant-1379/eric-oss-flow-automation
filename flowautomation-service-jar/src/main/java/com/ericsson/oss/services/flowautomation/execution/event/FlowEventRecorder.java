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

import javax.inject.Inject;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEventEntity;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionEventRepository;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionRepository;

/**
 * The type Flow event recorder allows to record the events from within the flow.
 * This Implementation is done here in JAR so that it can be easily tested in the unit functional tests.
 */
public class FlowEventRecorder {

    /**
     * The Flow execution repository.
     */
    @Inject
    FlowExecutionRepository flowExecutionRepository;

    /**
     * The Flow execution event repository.
     */
    @Inject
    FlowExecutionEventRepository flowExecutionEventRepository;

    /**
     * Record event.
     *
     * @param businessKey the business key
     * @param severity    the severity
     * @param message     the message
     * @param target      the target
     * @param eventData   the event data
     */
    public void recordEvent(final String businessKey, final FlowExecutionEventSeverity severity, final String message, final String target, final String eventData) {
        final long businessKeyId = Long.parseLong(new FlowBusinessKey(businessKey).getBusinessKeyId());
        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.findReferenceOnly(businessKeyId);
        flowExecutionEventRepository.save(new FlowExecutionEventEntity(flowExecutionEntity, severity, message, target, eventData));
    }
}
