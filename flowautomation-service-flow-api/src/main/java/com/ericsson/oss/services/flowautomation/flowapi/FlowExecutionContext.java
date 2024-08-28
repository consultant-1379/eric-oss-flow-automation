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

import org.camunda.bpm.engine.delegate.DelegateExecution;

import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;


/**
 * This Flow API allows the flow designers to know the execution name and flowId using the {@code ExecutionEntity}.
 */
public class FlowExecutionContext {

    private final FlowBusinessKey flowBusinessKey;

    private FlowExecutionContext(final DelegateExecution execution) {
        flowBusinessKey = new FlowBusinessKey(execution.getBusinessKey());
    }


    /**
     * Create flow execution context.
     *
     * @param execution the execution
     * @return the flow execution context
     */
    public static FlowExecutionContext create(final DelegateExecution execution) {
        return new FlowExecutionContext(execution);
    }

    /**
     * Returns flow execution name.
     *
     * @return the flow execution name
     */
    public String getFlowExecutionName() {
        return flowBusinessKey.getFlowExecutionName();
    }

    /**
     * Returns flow id.
     *
     * @return the flow id
     */
    public String getFlowId() {
        return flowBusinessKey.getFlowId();
    }
}
