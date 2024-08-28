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

package com.ericsson.oss.services.flowautomation.execution.event.api;

/**
 * The Interface FlowEventRecorderService.
 */
public interface FlowEventRecorderService {

    /**
     * Records warn event.
     *
     * @param processInstanceBusinessKey
     * @param message
     * @param target
     * @param details
     */
    void warn(final String processInstanceBusinessKey, final String message, final String target, final String details);
}
