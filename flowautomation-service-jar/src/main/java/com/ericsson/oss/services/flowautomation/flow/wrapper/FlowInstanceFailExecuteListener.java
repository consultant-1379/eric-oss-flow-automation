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

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.FAILED_EXECUTE;

import com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State;

/**
 * The type Flow instance fail execute listener.
 */
public class FlowInstanceFailExecuteListener extends WrapperFlowTaskExecutionListener {

    @Override
    public State getExecutionState() {
        return FAILED_EXECUTE;
    }
}
