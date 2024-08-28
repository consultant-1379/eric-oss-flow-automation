/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.services.flowautomation.test.fwk;

/**
 * The Enum TestErrorCodeStatus is a collection of flow errors and http status.
 */
public enum TestErrorCodeStatus {
    FLOW_PACKAGE_NOT_FOUND("flow-2000", 404),
    FLOW_PACKAGE_EMPTY("flow-2001", 400),
    FLOW_PACKAGE_CORRUPT("flow-2002", 400),
    FLOW_DEFINITION_NOT_FOUND("flow-2003", 400),
    FLOW_DEFINITION_PARSE_ERROR("flow-2004", 400),
    FLOW_DEPLOYMENT_FAILED("flow-2005", 400),
    FLOW_VERSION_SYNTAX_INVALID("flow-2006", 400),
    FLOW_VERSION_NOT_ALLOWED("flow-2007", 400),
    FLOW_DEFINITION_INVALID("flow-2009", 400),
    EXECUTION_PHASE_MISSING_IN_FLOW_PACKAGE("flow-2011", 400),
    MISSING_BUSINESS_KEY_PROPAGATION("flow-2012", 400),
    SETUP_PHASE_NOT_AVAILABLE("flow-2014", 404),
    FLOW_NAME_ALREADY_IN_USE("flow-2015", 400),
    FLOW_DOES_NOT_SUPPORT_BACK("flow-execution-2097", 403),
    FLOW_EXECUTION_INVALID_BACK_TARGET("flow-execution-2098", 403),
    FLOW_EXECUTION_NAME_IN_USE("flow-execution-2101", 409),
    FLOW_INPUT_SCHEMA_NOT_FOUND("flow-execution-2103", 404),
    INVALID_JSON_PAYLOAD("flow-execution-2104", 400),
    TASK_NOT_FOUND("flow-execution-2105", 404),
    INVALID_FLOW_INPUT("flow-execution-2106", 400),
    FLOW_EXECUTION_NOT_FOUND("flow-execution-2110", 404),
    FLOW_CANNOT_BE_DELETED("flow-execution-2112", 412),
    SCHEMA_NOT_FOUND("flow-execution-2113", 404),
    INVALID_FLOW_EXECUTION_PHASE_FOR_SUSPEND("flow-execution-2115", 409),
    INVALID_FLOW_EXECUTION_PHASE_FOR_STOP("flow-execution-2118", 409),
    SETUP_PHASE_IN_PROGRESS("flow-execution-2121", 404),
    JSON_PROCESSING_ERROR("flow-execution-2141", 400),
    JSON_VALIDATION_FAILED("flow-execution-2142", 400),
    INVALID_RESOURCE_DOWNLOAD_REQUEST("flow-execution-2143", 404),
    NO_FLOW_EXECUTION_RESOURCE_AVAILABLE("flow-execution-2144", 404),
    JSON_PARSING_ERROR("flow-execution-2146", 400),
    DATE_TIME_PARSING_ERROR("flow-execution-2147", 400),
    FLOW_ALREADY_EXISTS("flow-3000", 409),
    FLOW_DOES_NOT_EXIST("flow-3001", 404),
    ACTIVE_FLOW_VERSION_DOES_NOT_EXIST("flow-3002", 404),
    FLOW_VERSION_IS_ACTIVE("flow-3004", 409),
    FLOW_VERSION_DOES_NOT_EXIST("flow-3005", 404),
    ONGOING_RESTORE("backup-restore-4000", 409),
    RESTORE_NOT_IN_PROGRESS("backup-restore-4001", 409);

    /** The code. */
    private final String code;

    /** The http status. */
    private final int httpStatus;

    /**
     * Instantiates a new test error code.
     *
     * @param code   the code
     * @param httpStatus the http status
     */
    TestErrorCodeStatus(final String code, final int httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
