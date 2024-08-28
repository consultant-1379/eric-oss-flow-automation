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

import static com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType.Value.TEST_REST_TYPE;
import static com.ericsson.oss.services.flowautomation.test.fwk.rest.FlowAutomationUsers.USER_HEADER;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.utils.JsonParser;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEvent;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventFilter;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionFilter;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import com.ericsson.oss.services.flowautomation.model.PaginatedData;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import com.ericsson.oss.services.flowautomation.model.UserTaskSchema;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationRestResponseUtil;
import com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.viclovsky.swagger.coverage.SwaggerCoverageRestAssured;
import io.restassured.response.Response;

@TestServiceType(TEST_REST_TYPE)
public class TestFlowExecutionServiceRest implements FlowExecutionService {

    public static final String EXECUTIONS = "/v1/executions";

    private static final String FLOW_ID = "flow-id";
    private static final String FLOW_VERSION = "flow-version";
    private static final String FLOW_EXECUTION_NAME = "flow-execution-name";
    private static final String FILTER_BY_USER = "filter-by-user";
    private final AtomicReference<String> securityContext;

    public TestFlowExecutionServiceRest(AtomicReference<String> securityContext) {
        this.securityContext = securityContext;
    }

    @Override
    public List<FlowExecution> getExecutions(final FlowExecutionFilter filter) {
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get())
                .queryParam(FLOW_ID, filter.getFlowId())
                .queryParam(FLOW_VERSION, filter.getFlowVersion())
                .queryParam(FLOW_EXECUTION_NAME, filter.getFlowExecutionName())
                .queryParam(FILTER_BY_USER, filter.isFilterByUser())
                .get(EXECUTIONS);
        throwExceptionIfErrorResponse(response);
        try {
            final ObjectMapper objMapper = getMapper(FlowExecution.class, new FlowExecutionDeserializer());
            final FlowExecution[] flowExecutionsArray = objMapper.readValue(response.getBody().asString(), FlowExecution[].class);
            return Arrays.asList(flowExecutionsArray);
        } catch (final Exception e) {
            throw new FlowAutomationException(e.getMessage(), e);
        }
    }

    @Override
    public List<UserTask> getUserTasks(final String flowId, final String flowExecutionName) {
        final String endpoint = format("%s/%s/usertasks?flow-id=%s", EXECUTIONS, flowExecutionName, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).get(endpoint);
        throwExceptionIfErrorResponse(response);
        try {
            final ObjectMapper objMapper = getMapper(UserTask.class, new UserTaskDeserializer());
            final UserTask[] userTasksArray = objMapper.readValue(response.getBody().asString(), UserTask[].class);
            return Arrays.asList(userTasksArray);
        } catch (final Exception e) {
            throw new FlowAutomationException(e.getMessage(), e);
        }
    }

    @Override
    public void completeUserTask(final String taskId, final Map<String, Object> variables) {
        final String endpoint = format("%s/usertasks/%s/complete", EXECUTIONS, taskId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).multiPart("usertask-input", "null").when().header(USER_HEADER, securityContext.get()).post(endpoint);
        throwExceptionIfErrorResponse(response);
    }

    @Override
    public void completeUserTask(final String taskId, final Map<String, byte[]> userTaskFileInput, final String usertTaskInput) {
        final Response response;
        final String endpoint = format("%s/usertasks/%s/complete", EXECUTIONS, taskId);
        if (userTaskFileInput.size() > 0) {
            // only support 1 file for now
            if (userTaskFileInput.size() > 1) {
                fail("only support 1 file max");
            }
            final String fileName = userTaskFileInput.keySet().iterator().next();
            final byte[] fileBytes = userTaskFileInput.get(fileName);
            response = given().filter(new SwaggerCoverageRestAssured()).multiPart("usertask-input-files", fileName, fileBytes).multiPart("usertask-input", usertTaskInput).when()
                    .header(USER_HEADER, securityContext.get()).post(endpoint);
        } else {
            response = given().filter(new SwaggerCoverageRestAssured()).multiPart("usertask-input", usertTaskInput).when().header("Accept", "application/json").header(USER_HEADER, securityContext.get()).post(endpoint);
        }
        throwExceptionIfErrorResponse(response);
    }

    @Override
    public UserTaskSchema getUserTaskSchema(final String taskId) {
        final String endpoint = format("%s/usertasks/%s/schema", EXECUTIONS, taskId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).get(endpoint);
        throwExceptionIfErrorResponse(response);
        try {
            final ObjectMapper objMapper = getMapper(UserTaskSchema.class, new UserTaskSchemaDeserializer());
            return objMapper.readValue(response.getBody().asString(), UserTaskSchema.class);
        } catch (final Exception e) {
            throw new FlowAutomationException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteExecution(final String flowId, final String flowExecutionName) {
        final String endpoint = format("%s/%s?flow-id=%s", EXECUTIONS, flowExecutionName, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).queryParam("force", true).delete(endpoint);
        throwExceptionIfErrorResponse(response);
    }

    @Override
    public String getExecutionReport(final String flowId, final String flowExecutionName) {
        final String endpoint = format("%s/%s/report?flow-id=%s", EXECUTIONS, flowExecutionName, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).get(endpoint);
        throwExceptionIfErrorResponse(response);
        return response.getBody().asString();
    }

    @Override
    public String getExecutionReportSchema(final String flowId, final String flowExecutionName) {
        final String endpoint = format("%s/%s/report-schema?flow-id=%s", EXECUTIONS, flowExecutionName, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).get(endpoint);
        throwExceptionIfErrorResponse(response);
        return response.getBody().asString();
    }

    @Override
    public void suspendExecution(final String flowId, final String flowExecutionName) {
        final String endpoint = format("%s/%s/suspend?flow-id=%s", EXECUTIONS, flowExecutionName, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).put(endpoint);
        throwExceptionIfErrorResponse(response);
    }

    @Override
    public void suspendAllExecutionsForFlowVersion(final String flowId, final String flowVersion) {
        final String endpoint = format("%s/suspend?flow-id=%s&flow-version=%s", EXECUTIONS, flowId, flowVersion);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).put(endpoint);
        throwExceptionIfErrorResponse(response);
    }

    /**
     * Stop execution.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     */
    @Override
    public void stopExecution(final String flowId, final String flowExecutionName) {
        final String endpoint = format("%s/%s/stop?flow-id=%s", EXECUTIONS, flowExecutionName, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).put(endpoint);
        throwExceptionIfErrorResponse(response);
    }

    /**
     * Gets flow input schema and data.
     *
     * @param flowId            the flow id
     * @param flowExecutionName the flow execution name
     * @return the flow input schema and data
     */
    @Override
    public String getFlowInputSchemaAndData(final String flowId, final String flowExecutionName) {
        final String endpoint = format("%s/%s/flowinput-schema-data?flow-id=%s", EXECUTIONS, flowExecutionName, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).get(endpoint);
        throwExceptionIfErrorResponse(response);
        return response.getBody().asString();
    }

    @Override
    public FlowExecutionResource getExecutionResource(final String flowId, final String flowExecutionName, final String resource) {
        final String endpoint = format("%s/%s/download/%s?flow-id=%s", EXECUTIONS, flowExecutionName, resource, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).get(endpoint);
        throwExceptionIfErrorResponse(response);

        final String contentDisposition = response.getHeader("Content-Disposition");
        final String filename = contentDisposition.substring(contentDisposition.indexOf("filename=") + "filename=".length()).replaceAll("^\"|\"$", "");
        return new FlowExecutionResource(filename, response.getBody().asByteArray());
    }

    @Override
    public PaginatedData<FlowExecutionEvent> getExecutionEvents(final FlowExecutionEventFilter filter) {
        String endpoint = format("%s/%s/events?flow-id=%s", EXECUTIONS, filter.getFlowExecutionName(), filter.getFlowId());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

        if (Objects.nonNull(filter.getStartDate()))
            endpoint += format("&startDate=%s", filter.getStartDate().toInstant().atOffset(ZoneOffset.UTC).format(dateTimeFormatter));

        if (Objects.nonNull(filter.getEndDate()))
            endpoint += format("&endDate=%s", filter.getEndDate().toInstant().atOffset(ZoneOffset.UTC).format(dateTimeFormatter));

        if (Objects.nonNull(filter.getTarget()) && !Objects.equals(filter.getTarget(), ""))
            endpoint += format("&resource=%s", filter.getTarget());

        if (Objects.nonNull(filter.getSortData()))
            endpoint += format("&sort-by=%s&sort-order=%s", filter.getSortData().getSortBy(), filter.getSortData().getSortOrder().toString());

        if (Objects.nonNull(filter.getMessage()) && !Objects.equals(filter.getMessage(), ""))
            endpoint += format("&message=%s", filter.getMessage());

        if (Objects.nonNull(filter.getEventsSeverity()))
            endpoint += format("&severity=%s", filter.getEventsSeverity().stream().map(Object::toString).collect(Collectors.joining("&severity=")));

        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).get(endpoint);
        throwExceptionIfErrorResponse(response);

        try {
            return JsonParser.convertToType(response.getBody().asString(), new TypeReference<PaginatedData<FlowExecutionEvent>>() {});
        } catch (final IOException e) {
            throw new FlowAutomationException("Failed to deserialize execution events response: " + e.getMessage(), e);
        }
    }

    @Override
    public FlowExecutionResource getExecutionReportVariable(final String flowId, final String flowExecutionName, final String variableName) {
        final String endpoint = format("%s/%s/download/report-variable/%s?flow-id=%s", EXECUTIONS, flowExecutionName, variableName, flowId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when().header(USER_HEADER, securityContext.get()).get(endpoint);
        throwExceptionIfErrorResponse(response);

        final String contentDisposition = response.getHeader("Content-Disposition");
        final String filename = contentDisposition.substring(contentDisposition.indexOf("filename=") + "filename=".length()).replaceAll("^\"|\"$", "");
        return new FlowExecutionResource(filename, response.getBody().asByteArray());
    }

    @Override
    public void revertUserTask(String taskId) {
        final String endpoint = format("%s/usertasks/%s/back", EXECUTIONS, taskId);
        final Response response = given().filter(new SwaggerCoverageRestAssured()).when()
                .contentType(APPLICATION_JSON)
                .header(USER_HEADER, securityContext.get())
                .post(endpoint);
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

    private void throwExceptionIfErrorResponse(final Response response) {
        final Optional<FlowAutomationException> flowAutomationException = FlowAutomationRestResponseUtil.checkErrorResponse(response);
        if (flowAutomationException.isPresent()) {
            throw flowAutomationException.get();
        }
    }
}
