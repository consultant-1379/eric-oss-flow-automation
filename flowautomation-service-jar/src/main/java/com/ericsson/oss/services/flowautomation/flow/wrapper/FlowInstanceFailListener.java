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

import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.FAILED;

import com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State;

/**
 * The type Flow instance fail listener.
 */
public class FlowInstanceFailListener extends WrapperFlowTaskExecutionListener {

    @Override
    public State getExecutionState() {
        return FAILED;
    }
}
