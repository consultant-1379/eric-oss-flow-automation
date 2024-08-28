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

package com.ericsson.oss.services.flowautomation.test.fwk;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.ericsson.oss.services.flowautomation.error.ErrorCode;
import com.ericsson.oss.services.flowautomation.error.ErrorReason;
import com.ericsson.oss.services.flowautomation.error.FlowErrorCode;
import com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode;
import com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;

/**
 * The type Flow automation rest response util.
 */
public class FlowAutomationRestResponseUtil {

    /**
     * The Constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowAutomationRestResponseUtil.class);

    /**
     * Check error response optional.
     *
     * @param response the response
     * @return the optional
     */
    public static Optional<FlowAutomationException> checkErrorResponse(final Response response) {
        FlowAutomationException flowAutomationException = null;
        if (isErrorResponse(response)) {
            final JsonNode jsonNode;
            try {
                jsonNode = new ObjectMapper().readTree(response.getBody().asString());
                final JsonNode errors = jsonNode.get("errors");
                final JsonNode httpStatus = jsonNode.get("status");
                final String code = errors.get(0).get("code").asText();
                final int status = httpStatus.asInt();
                final String errorMessage = errors.get(0).get("errorMessage").asText();
                flowAutomationException = new FlowAutomationException(singletonList(new ErrorReason(code, errorMessage)), "Error response received", null);
                checkHttpErrorResponse(flowAutomationException.getErrorReasons(), status);
            } catch (final IOException e) {
                flowAutomationException = new FlowAutomationException("checkErrorResponse() Failed to process: " + e.getMessage(), e);
            }
        }

        return Optional.ofNullable(flowAutomationException);
    }

    private static void checkHttpErrorResponse(final List<ErrorReason> code, final int status) {
        LOGGER.info("Getting information for flow-code: {}  with http response of: {}", code, status);
        if (code.get(0).getCode().startsWith("flow-2")) {
            FlowPackageErrorCode[] flowPackageErrorCodes = FlowPackageErrorCode.class.getEnumConstants();
            populateErrorCodes(code.get(0).getCode(), status, flowPackageErrorCodes);
        } else if (code.get(0).getCode().startsWith("flow-3")) {
            FlowErrorCode[] flowErrorCodes = FlowErrorCode.class.getEnumConstants();
            populateErrorCodes(code.get(0).getCode(), status, flowErrorCodes);
        } else if (code.get(0).getCode().startsWith("flow-execution-")) {
            FlowExecutionErrorCode[] flowExecutionErrorCode = FlowExecutionErrorCode.class.getEnumConstants();
            populateErrorCodes(code.get(0).getCode(), status, flowExecutionErrorCode);
        }

    }

    private static void populateErrorCodes(final String code, final int status, final ErrorCode[] flowErrorCodes) {
        TestErrorCodeStatus[] testErrorCodeStatus = TestErrorCodeStatus.class.getEnumConstants();
        for (int i = 0; i < flowErrorCodes.length; i++) {
            if (flowErrorCodes[i].code().equals(code)) {
                for (int j = 0; j < testErrorCodeStatus.length; j++) {
                    if (testErrorCodeStatus[j].code().equals(code)) {
                        assertEquals(testErrorCodeStatus[j].httpStatus(), status);
                    }
                }
            }
        }
    }


    /**
     * Is error response boolean.
     *
     * @param response the response
     * @return the boolean
     */
    public static boolean isErrorResponse(final Response response) {
        return response.getStatusCode() / 200 != 1;
    }


    private FlowAutomationRestResponseUtil() {

    }
}
