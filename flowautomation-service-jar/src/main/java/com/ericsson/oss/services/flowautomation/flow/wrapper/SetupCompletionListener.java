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

package com.ericsson.oss.services.flowautomation.flow.wrapper;

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.CONFIRM_EXECUTE;

import com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State;

/**
 * The listener interface for receiving setupCompletion events. The class that is interested in processing a setupCompletion event implements this
 * interface, and the object created with that class is registered with a component using the component's <code>addSetupCompletionListener<code>
 * method. When the setupCompletion event occurs, that object's appropriate method is invoked.
 *
 * @see SetupCompletionEvent
 */
public class SetupCompletionListener extends WrapperFlowTaskExecutionListener {

    @Override
    public State getExecutionState() {
        return CONFIRM_EXECUTE;
    }
}
