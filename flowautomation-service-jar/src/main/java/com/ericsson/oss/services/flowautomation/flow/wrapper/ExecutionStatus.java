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
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.EXECUTING;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.SETTING_UP;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STARTED;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Execution status.
 */
@Getter
@Setter
@ToString
public class ExecutionStatus {

    private State state;

    /**
     * Instantiates a new Execution status.
     *
     * @param state the state
     */
    public ExecutionStatus(final State state) {
        super();
        this.state = state;
    }

    /**
     * Gets running states.
     *
     * @return the running states
     */
    public static List<State> getRunningStates() {
        return Arrays.asList(STARTED, SETTING_UP, CONFIRM_EXECUTE, EXECUTING);
    }

    /**
     * The enum State.
     */
    public enum State {

        STARTED,

        SETTING_UP,

        CONFIRM_EXECUTE,

        EXECUTING,

        COMPLETED,

        CANCELLED,

        SUSPENDED,

        STOP,

        STOPPING,

        STOPPED,

        FAILED,

        FAILED_SETUP,

        FAILED_EXECUTE
    }
}
