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
 * The Enum FlowPackageErrorCode is a collection of flow package errors.
 */
public enum FlowPackageErrorCode implements ErrorCode {

    /** The flow package not found. */
    FLOW_PACKAGE_NOT_FOUND("flow-2000", "Missing flow-package field in the flow import operation."),

    /** The flow package empty. */
    FLOW_PACKAGE_EMPTY("flow-2001", "Either the flow package is not a valid zip format or it's an empty zip"),

    /** The flow package corrupt. */
    FLOW_PACKAGE_CORRUPT("flow-2002", "Failed to validate the zip content. Invalid flow package"),

    /** The flow config not found. */
    FLOW_DEFINITION_NOT_FOUND("flow-2003", "flow-definition.json file is missing in the flow package"),

    /** The flow config read error. */
    FLOW_DEFINITION_PARSE_ERROR("flow-2004", "Unexpected error while parsing the flow-definition.json file"),

    /** Flow deployment failed */
    FLOW_DEPLOYMENT_FAILED("flow-2005", "Deployment failed."),

    /** Flow version syntax invalid */
    FLOW_VERSION_SYNTAX_INVALID("flow-2006", "Invalid Flow version syntax."),

    /** Flow version not allowed */
    FLOW_VERSION_NOT_ALLOWED("flow-2007", "Flow version is not allowed"),

    /** The flow definition invalid. */
    FLOW_DEFINITION_INVALID("flow-2009", "Invalid flow-definition.json file"),

    /** The execution phase missing in flow package. */
    EXECUTION_PHASE_MISSING_IN_FLOW_PACKAGE("flow-2011", "Execution phase missing in flow package"),

    /** The missing business key propagation. */
    MISSING_BUSINESS_KEY_PROPAGATION("flow-2012", "Business key propagation is missing in Flow's Call Activity"),

    /** The flow cannot be imported. */
    FLOW_CANNOT_BE_IMPORTED("flow-2013", "Flow cannot be imported"),

    SETUP_PHASE_NOT_AVAILABLE("flow-2014", "Flow does not have setup phase"),

    FLOW_NAME_ALREADY_INUSE("flow-2015", "Flow name is already in use by another flow"),

    FLOW_DEFINITION_ZIPFILE_PRESENT("flow-2016", "No zip file should be present inside the flow package zip file"),

    FLOW_DEFINITION_MAX_FILES_NUMBER_EXCEEDED("flow-2017", "Number of entries in the flow package should not exceed 1024"),

    FLOW_DEFINITION_MAX_FILEPATH_LENGTH_EXCEEDED("flow-2018", "File path length should not exceed 255");

    /** The code. */
    private final String code;

    /** The reason. */
    private final String reason;

    /**
     * Instantiates a new flow package error code.
     *
     * @param code   the code
     * @param reason the reason
     */
    FlowPackageErrorCode(final String code, final String reason) {
        this.code = code;
        this.reason = reason;
    }

    /**
     * Converts string error code into {@code ErrorCode} instance.
     *
     * @param code the code
     * @return the {@code ErrorCode} instance if found or {@code UnknownErrorCode}
     */
    public static ErrorCode from(final String code) {
        return stream((ErrorCode[]) FlowPackageErrorCode.values())
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
