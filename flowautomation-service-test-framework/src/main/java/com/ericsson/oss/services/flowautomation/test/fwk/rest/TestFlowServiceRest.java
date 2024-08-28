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

package com.ericsson.oss.services.flowautomation.test.fwk.rest;

import static java.lang.String.format;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import static org.junit.Assert.fail;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType.Value.TEST_REST_TYPE;
import static com.ericsson.oss.services.flowautomation.test.fwk.rest.FlowAutomationUsers.USER_HEADER;
import static io.restassured.RestAssured.given;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.ericsson.oss.services.flowautomation.api.FlowService;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionInput;
import com.ericsson.oss.services.flowautomation.model.FlowSource;
import com.ericsson.oss.services.flowautomation.model.FlowVersionProcessDetails;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationRestResponseUtil;
import com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.viclovsky.swagger.coverage.SwaggerCoverageRestAssured;
import io.restassured.response.Response;

@TestServiceType(TEST_REST_TYPE)
public class TestFlowServiceRest implements FlowService {

    public static final String FLOWS = "/v1/flows";
    private static final String FORCE = "force";
    private final AtomicReference<String> securityContext;

    public TestFlowServiceRest(AtomicReference<String> securityContext) {
        this.securityContext = securityContext;
    }

    @Override
    public List<FlowDefinition> getFlows() {
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).get(FLOWS);
        throwExceptionIfErrorResponse(response);
        try {
            final ObjectMapper objectMapper = getMapper(FlowDefinition.class, new FlowDefinitionDeserializer());
            final FlowDefinition[] flowsArray = objectMapper.readValue(response.getBody().asString(), FlowDefinition[].class);
            return Arrays.asList(flowsArray);
        } catch (final Exception e) {
            throw new FlowAutomationException(e.getMessage(), e);
        }
    }

    @Override
    public FlowDefinition importFlow(final byte[] flowPackage) {
        final Response response = given().filter(new SwaggerCoverageRestAssured()).multiPart("flow-package", "flow-package.zip", flowPackage).when().header(USER_HEADER, securityContext.get())
                .post(FLOWS);
        throwExceptionIfErrorResponse(response);
        try {
            final ObjectMapper objectMapper = getMapper(FlowDefinition.class, new FlowDefinitionDeserializer());
            return objectMapper.readValue(response.getBody().asString(), FlowDefinition.class);
        } catch (final Exception e) {
            throw new FlowAutomationException(e.getMessage(), e);
        }
    }

    @Override
    public FlowDefinition importFlow(final byte[] flowPackage, final FlowSource flowSource) {
        throw new UnsupportedOperationException("This method is not exposed to REST API");
    }

    @Override
    public void executeFlow(final String flowId, final String flowExecutionName) {
        final String endpoint = format("%s/%s/execute", FLOWS, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).multiPart("name", flowExecutionName )
                .when().header(USER_HEADER, securityContext.get()).post(endpoint);
        throwExceptionIfErrorResponse(response);
    }

    @Override
    public void executeFlow(final String flowId, final FlowExecutionInput flowExecutionInput) {
        final String endpoint = format("%s/%s/execute", FLOWS, flowId);
        final Map<String, byte[]> flowInputFiles = flowExecutionInput.getFlowInputFiles();
        // only support 1 file for now
        if (flowInputFiles.size() > 1) {
            fail("only support 1 file max");
        }

        final String fileName = flowExecutionInput.getFlowInputFileName();
        final byte[] fileBytes = flowExecutionInput.getFlowInput().getBytes(StandardCharsets.UTF_8);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).multiPart("flow-input", fileName, fileBytes).multiPart("name", flowExecutionInput.getName())
                .multiPart("file-name", fileName).when().header(USER_HEADER, securityContext.get()).post(endpoint);
        throwExceptionIfErrorResponse(response);

    }

    @Override
    public void activateFlowVersion(final String flowId, final String flowVersion, final boolean activate) {
        final String bodyContent = format("{\"value\": %b}", activate);
        final String endpoint = format("%s/%s/activate", FLOWS, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).contentType(APPLICATION_JSON).body(bodyContent).when().header(USER_HEADER, securityContext.get()).queryParam("flow-version", flowVersion)
                .put(endpoint);
        throwExceptionIfErrorResponse(response);
    }

    @Override
    public void enableFlow(final String flowId, final boolean enable) {
        final String bodyContent = format("{\"value\": %b}", enable);
        final String endpoint = format("%s/%s/enable", FLOWS, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).contentType(APPLICATION_JSON).body(bodyContent).when().header(USER_HEADER, securityContext.get()).put(endpoint);
        throwExceptionIfErrorResponse(response);
    }

    @Override
    public FlowDefinition getFlowDefinition(final String flowId) {
        final String endpoint = format("%s/%s", FLOWS, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).get(endpoint);
        throwExceptionIfErrorResponse(response);
        try {
            final ObjectMapper objectMapper = getMapper(FlowDefinition.class, new FlowDefinitionDeserializer());
            return objectMapper.readValue(response.getBody().asString(), FlowDefinition.class);
        } catch (final Exception e) {
            throw new FlowAutomationException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteFlow(final String flowId, final boolean force) {
        final String endpoint = format("%s/%s", FLOWS, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).queryParam(FORCE, force).delete(endpoint);
        throwExceptionIfErrorResponse(response);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ObjectMapper getMapper(final Class type, final StdDeserializer deserializer) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(type, deserializer);
        objectMapper.registerModule(module);
        objectMapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);

        return objectMapper;
    }

    @Override
    public FlowVersionProcessDetails getProcessDetailsForFlowVersion(String flowId, String flowVersion) {
        final String endpoint = format("%s/%s/%s/process-details", FLOWS, flowId, flowVersion);
        final Response response = given().filter(new SwaggerCoverageRestAssured())
                .when()
                .header(USER_HEADER, securityContext.get())
                .get(endpoint);

        throwExceptionIfErrorResponse(response);

        try {

            final ObjectMapper objectMapper = getMapper(FlowVersionProcessDetails.class, new FlowVersionProcessDetailsDeserializer());
            return objectMapper.readValue(response.getBody().asString(), FlowVersionProcessDetails.class);

        } catch (final Exception e) {
            throw new FlowAutomationException(e.getMessage(), e);
        }
    }

    @Override
    public String getFlowInputSchema(final String flowId, final String flowVersion) {
        final String endpoint = format("%s/%s/resource/flow-input-schema", FLOWS, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get())
                .queryParam("flow-version", flowVersion).get(endpoint);
        throwExceptionIfErrorResponse(response);
        return response.getBody().asString();

    }

    private void throwExceptionIfErrorResponse(final Response response) {
        final Optional<FlowAutomationException> flowAutomationException = FlowAutomationRestResponseUtil.checkErrorResponse(response);
        if (flowAutomationException.isPresent()) {
            throw flowAutomationException.get();
        }
    }
}
