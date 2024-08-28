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

package com.ericsson.oss.services.flowautomation.error;

import static java.util.Arrays.stream;

/**
 * Collection of error codes related to Flow
 */
public enum FlowErrorCode implements ErrorCode {

    /** flow entity already exists. */
    FLOW_ALREADY_EXISTS("flow-3000", "Flow already exists."),

    /** flow entity does not exist */
    FLOW_DOES_NOT_EXIST("flow-3001", "Flow does not exist."),

    /** flow entity does not exist */
    FLOW_VERSION_NOT_EXIST("flow-3005", "Flow version does not exist."),

    /** active flow version doesnt exist. */
    ACTIVE_FLOW_VERSION_DOESNT_EXIST("flow-3002", "Flow does not have an active version."),

    FLOW_VERSION_IS_ACTIVE("flow-3004", "The selected flow version is active."),

    FLOW_CONFLICT("flow-3012", "Conflict when performing operation in flow");

    /** The code. */
    private final String code;
    /** The reason. */
    private final String reason;

    /**
     * Instantiates a new flow package error code.
     *
     * @param code
     *         the code
     * @param reason
     *         the reason
     */
    FlowErrorCode(final String code, final String reason) {
        this.code = code;
        this.reason = reason;
    }

    /**
     * Converts string error code into {@code ErrorCode} instance.
     *
     * @param code
     *         the code
     * @return the {@code ErrorCode} instance if found or {@code UnknownErrorCode}
     */
    public static ErrorCode from(final String code) {
        return stream((ErrorCode[]) FlowErrorCode.values())
                .filter(errorCode -> errorCode.code().equals(code))
                .findFirst()
                .orElse(UnknownErrorCode.INSTANCE);
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String reason() {
        return reason;
    }
}
