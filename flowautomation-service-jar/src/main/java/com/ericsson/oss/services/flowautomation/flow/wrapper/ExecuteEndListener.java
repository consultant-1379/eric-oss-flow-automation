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

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.COMPLETED;

import com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State;

/**
 * The listener interface for receiving executeEnd events. The class that is interested in processing a executeEnd event implements this interface,
 * and the object created with that class is registered with a component using the component's <code>addExecuteEndListener<code> method. When the
 * executeEnd event occurs, that object's appropriate method is invoked.
 *
 * @see ExecuteEndEvent
 */
public class ExecuteEndListener extends WrapperFlowTaskExecutionListener {

    @Override
    public State getExecutionState() {
        return COMPLETED;
    }
}
