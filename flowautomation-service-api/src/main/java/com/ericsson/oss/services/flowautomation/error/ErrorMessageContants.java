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

/**
 * The class contains all the error messages.
 */
public final class ErrorMessageContants {

    public static final String FLOW_EXECUTION_NAME_ALREADY_EXISTS = "Flow Execution name: %s already exists for the Flow with id: %s";
    public static final String FLOW_INPUT_SCHEMA_IS_NOT_FOUND = "Flow input schema is not found for flow with id: %s, version: %s";
    public static final String FLOW_PACKAGE_DEPLOYMENT_FAILED = "Deployment failed for flow package with id: %s, name: %s, version: %s";
    public static final String FLOW_EXECUTION_FAILED = "Failed to execute the flow with id: %s and Execution name: %s";
    public static final String FLOW_PACKAGE_ALREADY_EXISTS = "Flow package with id: %s, version: %s already exists.";
    public static final String ACTIVE_FLOW_VERSION_NOT_FOUND = "The flow might be disabled or No active flow version available for the flow with id: %s. Please contact the Flow Administrator.";
    public static final String FLOW_VERSION_SYNTAX_IS_INVALID = "Invalid flow version syntax. Flow version should be of pattern \"00.00.00\"";
    public static final String FLOW_VERSION_IS_NOT_ALLOWED = "Higher version of flow with id: %s already exists. Lower version: %s of flow cannot be deployed.";
    public static final String FLOW_ID_DOES_NOT_EXIST = "Flow with id: %s does not exist.";
    public static final String FLOW_VERSION_DOES_NOT_EXIST = "Flow version: %s, is not found for flow with id: %s";
    public static final String FLOW_ID_VERSION_DOES_NOT_EXIST = "Flow with id: %s, version: %s does not exist.";
    public static final String TASK_IS_NOT_FOUND = "The task with id: %s might have been completed already or the task id is invalid.";
    public static final String USER_TASK_SCHEMA_IS_NOT_FOUND = "No schema found for the user task with id: %s, name: %s";
    public static final String SCHEMA_IS_NOT_FOUND = "No schema found for resource: %s";
    public static final String INVALID_INPUT_FILE_TYPE_UPLOADED = "Please provide a valid input file type.";
    public static final String FLOW_INPUT_FILE_NOT_UPLOADED = "flow-input.json file not found";
    public static final String FLOW_EXECUTION_SETUP_INPUT_FILE_NOT_FOUND = "Please select flow execution setup input file.";
    public static final String FLOW_EXECUTION_SETUP_MORE_THAN_ONE_FILE_UPLOADED = "More than one file uploaded. Please upload a single zip file.";
    public static final String FILE_ATTACHMENT_IS_MISSING = "File: %s is missing in the request. Please attach the file and resend";
    public static final String INVALID_FILE_NAME_FOUND_IN_PAYLOAD = "Invalid file name has been found in request payload";
    public static final String FLOW_EXECUTION_IS_NOT_FOUND = "No such flow execution found for flow id: %s, flow execution name: %s";
    public static final String DELETE_FLOW_EXECUTION_IS_NOT_ALLOWED = "Unsupported operation, you cannot delete the flow instance for flow id: %s and flow execution name: %s during execute phase";
    public static final String FLOW_CAN_NOT_BE_DELETED = "The flow with id: %s cannot be deleted. The requested operation is not supported";
    public static final String JSON_SCHEMA_IS_NOT_FOUND = "The json schema is not found or might be invalid.";
    public static final String JSON_PROCESSING_ERROR_OCCURED = "Invalid file! Please provide a valid json file";
    public static final String JSON_PARSING_ERROR_OCCURRED = "Error in parsing json schema";
    public static final String JSON_PROCESSING_INVALID_FIELD = "Json processing %s with invalid value.";
    public static final String DATE_TIME_PARSING_FAILED = "Error parsing date-time, expected ISO-8601 date-time format.";
    public static final String FLOW_DEFINITION_IS_INVALID = "Invalid value: %s for attribute: %s";
    public static final String MISSING_BUSINESS_KEY_PROPAGATION_ON_CALL_ACTIVITY = "The business key propagation is missing in flow with id: %s, version: %s, call activity id: %s, call activity name: %s";
    public static final String OPERATION_IS_NOT_ALLOWED_ON_STATE = "You are not allowed to perform this operation on flow id: %s on state: %s";
    public static final String FLOW_SETUP_PHASE_NOT_AVAILABLE = "The flow with id: %s, version: %s does not have setup phase. Hence flow input data is not available";
    public static final String FLOW_INPUT_DATA_NOT_AVAILABLE_SETUP_PHASE_ERROR = "The setup phase is not finished successfully. Hence flow input data is not available";
    public static final String FLOW_NAME_ALREADY_INUSE = "The flow name:%s is already in use by another flow";

    private ErrorMessageContants() {
        //
    }
}
