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

/**
 * The Enum WrapperFlowElement.
 */
public enum WrapperFlowElement {

    SETUP_CALL_ACTIVITY("setup", "setup", "FAInternalSetupProcessId"),

    EXECUTE_CALL_ACTIVITY("execute", "execute", "FAInternalExecuteProcessId"),

    REVIEW_AND_CONFIRM_EXECUTE_USER_TASK("FAInternal-review-confirm-execute", "Review and Confirm Execute") {
        @Override
        public String getCalledElementId() {
            throw new UnsupportedOperationException();
        }
    },
    IS_SETUP("isSetup", "isSetup"),

    CHOOSE_SETUP_USER_TASK("FAInternal-optionally-get-file-based-input", "Choose Setup") {
        @Override
        public String getCalledElementId() {
            throw new UnsupportedOperationException();
        }
    },

    FLOW_INPUT("flowInput", "flowInput") {
        @Override
        public String getCalledElementId() {
            throw new UnsupportedOperationException();
        }
    },

    FA_INTERNAL_FLOW_INPUT_DATA("FAInternalFlowInputData", "FAInternalFlowInputData") {
        @Override
        public String getCalledElementId() {
            throw new UnsupportedOperationException();
        }
    },

    FA_INTERNAL_USER_SETUP("FAInternalUserSetup", "FAInternalUserSetup") {
        @Override
        public String getCalledElementId() {
            throw new UnsupportedOperationException();
        }
    };

    /** The id. */
    private final String id;

    /** The name. */
    private final String name;

    /** The called element id. */
    private final String calledElementId;

    /**
     * Instantiates a new wrapper flow element.
     *
     * @param id   the id
     * @param name the name
     */
    private WrapperFlowElement(final String id, final String name) {
        this(id, name, null);
    }

    /**
     * Instantiates a new wrapper flow element.
     *
     * @param id              the id
     * @param name            the name
     * @param calledElementId the called element id
     */
    private WrapperFlowElement(final String id, final String name, final String calledElementId) {
        this.id = id;
        this.name = name;
        this.calledElementId = calledElementId;
    }

    /**
     * Gets the called element id.
     *
     * @return the called element id
     */
    public String getCalledElementId() {
        return calledElementId;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}
