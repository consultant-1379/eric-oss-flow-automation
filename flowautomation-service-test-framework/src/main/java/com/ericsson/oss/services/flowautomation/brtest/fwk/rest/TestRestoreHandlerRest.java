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

package com.ericsson.oss.services.flowautomation.brtest.fwk.rest;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType.Value.TEST_REST_TYPE;
import static com.ericsson.oss.services.flowautomation.test.fwk.rest.FlowAutomationUsers.USER_HEADER;
import static io.restassured.RestAssured.given;

import static java.lang.String.format;

import com.ericsson.oss.services.flowautomation.brapi.RestoreHandler;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationRestResponseUtil;
import com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType;
import com.github.viclovsky.swagger.coverage.SwaggerCoverageRestAssured;
import io.restassured.response.Response;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@TestServiceType(TEST_REST_TYPE)
public class TestRestoreHandlerRest implements RestoreHandler {


    public static final String INTERNAL = "internal/v1";
    private final AtomicReference<String> securityContext;

    public TestRestoreHandlerRest(AtomicReference<String> securityContext) {
        this.securityContext = securityContext;
    }

    @Override
    public void preRestoreActions() {
        final String endpoint = format("%s/pre-restore-actions", INTERNAL);
        final Response response = given().filter(new SwaggerCoverageRestAssured())
                .when().header(USER_HEADER, securityContext.get()).post(endpoint);
        throwExceptionIfErrorResponse(response);
    }

    @Override
    public void postRestoreActions() {
        final String endpoint = format("%s/post-restore-actions", INTERNAL);
        final Response response = given().filter(new SwaggerCoverageRestAssured())
                .when().header(USER_HEADER, securityContext.get()).post(endpoint);
        throwExceptionIfErrorResponse(response);
    }

    private void throwExceptionIfErrorResponse(final Response response) {
        final Optional<FlowAutomationException> flowAutomationException = FlowAutomationRestResponseUtil.checkErrorResponse(response);
        if (flowAutomationException.isPresent()) {
            throw flowAutomationException.get();
        }
    }
}
