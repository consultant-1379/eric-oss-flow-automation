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
 * The Enum FlowExecutionErrorCode is a collection of flow package errors.
 */
public enum FlowExecutionErrorCode implements ErrorCode {

    FLOW_EXECUTION_INPUT_FILE_PARSE_ERROR("flow-execution-2004", "Unexpected error while parsing the flow input zip file"),

    FLOW_DOES_NOT_SUPPORT_BACK("flow-execution-2097", "The Flow isn't back enabled. Please include the required property in flow definition."),

    FLOW_EXECUTION_INVALID_BACK_TARGET("flow-execution-2098", "Invalid back target for the usertask, it cannot be null"),

    FLOW_EXECUTION_MODIFICATION_ERROR("flow-execution-2099", "Failed to go back to previous task."),

    FLOW_EXECUTION_NAME_IN_USE("flow-execution-2101", "The flow execution name is already in use."),

    FLOW_INPUT_SCHEMA_NOT_FOUND("flow-execution-2103", "No flow input schema found"),

    INVALID_JSON_PAYLOAD("flow-execution-2104", "Invalid json payload"),

    TASK_NOT_FOUND("flow-execution-2105", "Task not found"),

    INVALID_FLOW_INPUT("flow-execution-2106", "Invalid flow input"),

    USER_TASK_SCHEMA_NOT_FOUND("flow-execution-2107", "No user task schema found"),

    FILE_ATTACHMENT_MISSING("flow-execution-2108", "File attachment missing"),

    INVALID_FILE_NAME("flow-execution-2109", "Select a valid file"),

    FLOW_EXECUTION_NOT_FOUND("flow-execution-2110", "The flow execution is not found for specified flow id and flow execution name."),

    INVALID_FLOW_EXECUTION_DELETE("flow-execution-2111", "Flow execution cannot be deleted in current phase of the execution."),

    FLOW_CANNOT_BE_DELETED("flow-execution-2112", "Flow cannot be deleted."),

    SCHEMA_NOT_FOUND("flow-execution-2113", "Schema file not found"),

    INVALID_FLOW_EXECUTION_PHASE_FOR_SUSPEND("flow-execution-2115", "The flow execution is in the invalid phase for suspension."),

    INVALID_FLOW_EXECUTION_PHASE_FOR_STOP("flow-execution-2118", "The flow execution is in the invalid phase for stop."),

    SETUP_PHASE_AMBIGUOUS_ACTIVE_USERTASKS("flow-execution-2005", "There is ambiguity in active usertasks in setup phase to query the flow input."),

    /** Flow execution failed */
    FLOW_EXECUTION_FAILED_ERROR("flow-execution-2119", "Flow execution failed."),

    INVALID_FLOW_INPUT_VARIABLE_MAP("flow-execution-2120", "Input variable map does not exist"),

    SETUP_PHASE_IN_PROGRESS("flow-execution-2121", "Setup phase in progress"),

    SETUP_PHASE_ERROR("flow-execution-2122", "Setup phase not successful"),

    /**
     * Json error codes start from 2140
     */
    JSON_SCHEMA_NOT_FOUND("flow-execution-2140", "Json schema not found"),

    JSON_PROCESSING_ERROR("flow-execution-2141", "Json processing error"),

    JSON_VALIDATION_FAILED("flow-execution-2142", "Json validation failed"),

    INVALID_RESOURCE_DOWNLOAD_REQUEST("flow-execution-2143", "Invalid resource download request for the flow execution."),

    NO_FLOW_EXECUTION_RESOURCE_AVAILABLE("flow-execution-2144", "No resource available to download for the flow execution."),

    FLOW_EXECUTION_RESOURCE_GENERATION_FAILED("flow-execution-2145", "Failed to generate the flow execution resource."),

    JSON_PARSING_ERROR("flow-execution-2146", "Json parsing error"),

    INVALID_DATE_TIME_FORMAT("flow-execution-2147", "Invalid date-time format."),

    /**
     * Flow execution input file errors start from 2160
     */
    FLOW_EXECUTION_SETUP_INPUT_FILE_NOT_FOUND("flow-execution-2161", "Flow execution setup input file not found"),

    EMPTY_FLOW_EXECUTION_INPUT("flow-execution-2162", "Empty flow execution input"),

    FLOW_INPUT_FILE_NOT_UPLOADED("flow-execution-2163", "flow-input.json file not found."),

    SETUP_FOLDER_NOT_FOUND("flow-execution-2164", "Setup folder not found in the uploaded zip."),

    FLOW_INPUT_ZIP_CORRUPT("flow-execution-2165", "Corrupt flow input zip");


    private final String code;

    private final String reason;

    /**
     * Instantiates a new flow package error code.
     *
     * @param code
     *            the code
     * @param reason
     *            the reason
     */
    FlowExecutionErrorCode(final String code, final String reason) {
        this.code = code;
        this.reason = reason;
    }

    /**
     * Converts string error code into {@code ErrorCode} instance.
     *
     * @param code
     *            the code
     * @return the {@code ErrorCode} instance if found or {@code UnknownErrorCode}
     */
    public static ErrorCode from(final String code) {
        return stream((ErrorCode[]) FlowExecutionErrorCode.values())
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
