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

import static com.ericsson.oss.services.flowautomation.entities.FlowEntityStatus.ENABLED;
import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.ACTIVE_FLOW_VERSION_DOESNT_EXIST;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_PACKAGE_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.test.fwk.BasicUtils.niceJson;
import static com.ericsson.oss.services.flowautomation.test.fwk.rest.FlowAutomationUsers.USER_HEADER;
import static com.ericsson.oss.services.flowautomation.test.fwk.rest.FlowAutomationUsers.admin;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.ericsson.oss.services.flowautomation.exception.EntityNotFoundException;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowPackageUtil;
import com.ericsson.oss.services.flowautomation.flowapi.usertask.UsertaskInputProcessingError;
import com.ericsson.oss.services.flowautomation.model.FlowVersionProcessDetails;
import com.ericsson.oss.services.flowautomation.test.fwk.check.FlowAutomationCheck;
import com.ericsson.oss.services.flowautomation.test.fwk.check.FlowAutomationCheckWithRetries;
import com.ericsson.oss.services.flowautomation.test.fwk.query.FlowAutomationQueries;
import com.ericsson.oss.services.flowautomation.test.fwk.query.FlowAutomationQueriesImpl;
import com.ericsson.oss.services.flowautomation.test.fwk.util.SimpleRetryManager;
import org.camunda.bpm.engine.ScriptEvaluationException;
import org.junit.After;
import org.junit.Before;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.api.FlowService;
import com.ericsson.oss.services.flowautomation.error.ErrorCode;
import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionInput;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import com.ericsson.oss.services.flowautomation.model.UserTaskSchema;
import com.ericsson.oss.services.flowautomation.test.fwk.jse.JseTestContext;
import com.ericsson.oss.services.flowautomation.test.fwk.rest.TestFlowExecutionServiceRest;
import com.ericsson.oss.services.flowautomation.test.fwk.rest.TestFlowServiceRest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * The class FlowAutomationBaseTest contains the list of methods to be used for testing Flow Automation framework.
 */
public abstract class FlowAutomationBaseTest implements FlowAutomationQueries, FlowAutomationCheck {

    private static final String SEPARATOR = "----------------------------------------------------------------------------";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String CHOOSE_SETUP = "Choose Setup";
    private static final String REVIEW_AND_CONFIRM_EXECUTE = "Review and Confirm Execute";

    private static final String V1_FLOWS = "/v1/flows";

    protected FlowService flowService;
    protected FlowExecutionService flowExecutionService;

    protected FlowAutomationCheck flowAutomationCheck;
    protected FlowAutomationQueries flowAutomationQueries;

    protected SimpleRetryManager retryManager = new SimpleRetryManager();

    protected TestClientType clientType;

    protected TestClientType selectClientType() throws InterruptedException {
        return TestClientType.JSE;
    }

    private AtomicReference<String> securityContext = new AtomicReference<>(admin());

    /* Idun-2351 */
    protected boolean outputToConsole = false;

    protected void setOutputToConsole(final boolean outputToConsole) {
        this.outputToConsole = outputToConsole;
    }

    /**
     * Sets up the initial environment for test
     */
    @Before
    @SuppressWarnings({"squid:S2696", "squid:S1075"})
    public void setup() throws InterruptedException {
        clientType = selectClientType();
        if (clientType == TestClientType.JSE) {
            flowService = JseTestContext.getInstance().getFlowServiceClient();
            flowExecutionService = JseTestContext.getInstance().getFlowExecutionServiceClient();
        } else if (clientType == TestClientType.REST) {
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = 8080;
            if(System.getenv("REST_RANDOM_PORT") != null){
                RestAssured.port = Integer.parseInt(System.getenv("REST_RANDOM_PORT"));
            }
            logger.info(" ############### RestAssured.port " + RestAssured.port);
            RestAssured.basePath = "/flow-automation";
            awaitUntilInternalFlowsDeployed();

            flowService = new TestFlowServiceRest(securityContext);
            flowExecutionService = new TestFlowExecutionServiceRest(securityContext);
        }
        flowAutomationQueries = new FlowAutomationQueriesImpl(flowService, flowExecutionService);
        flowAutomationCheck = new FlowAutomationCheckWithRetries(flowService, flowExecutionService, flowAutomationQueries);
    }

    /**
     * Restores the initial state by deleting  the flows
     */
    @After
    public void teardown() {
        final List<FlowDefinition> flowDefinitions = flowService.getFlows();
        for (final FlowDefinition flowDefinition : flowDefinitions) {
            if (!flowDefinition.getId().equals("com.ericsson.oss.fa.internal.flows.houseKeeping")
                    && !flowDefinition.getId().equals("com.ericsson.oss.fa.internal.flows.stopflowinstance")
                    && !flowDefinition.getId().equals("com.ericsson.oss.fa.internal.flows.incidentHandling")) {
                flowService.deleteFlow(flowDefinition.getId(), true);
            }
        }
    }

    /**
     * This method makes sure that all the internal flows are deployed
     */
//    private void awaitUntilFAEarDeployed() {
//    TODO: fix the impl to check /health end point and confirm the deployment
//        logger.info("WAITING FA EAR TO DEPLOY...");
//        await().atMost(1200, TimeUnit.SECONDS)
//                .until(() -> {
//                    final Response response = given().when().header(USER_HEADER, admin()).get(V1_FLOWS);
//                    final int statusCode = response.getStatusCode();
//                    return statusCode != 500;
//                });
//    }

    /**
     * This method makes sure that all the internal flows are deployed
     */
    private static void awaitUntilInternalFlowsDeployed() throws InterruptedException {
        await()
                .atMost(900, TimeUnit.SECONDS)
                .until(() -> {
            final Response response = given().when().header(USER_HEADER, admin()).get(V1_FLOWS);
            final String body = response.getBody().asString();
            final String[] flows = {"com.ericsson.oss.fa.internal.flows.houseKeeping",
                    "com.ericsson.oss.fa.internal.flows.stopflowinstance", "com.ericsson.oss.fa.internal.flows.incidentHandling"};
            return stringContainsItemFromList(body, flows);
        });
    }

    private static boolean stringContainsItemFromList(final String inputBody, final String[] flows) {
        return Arrays.stream(flows).parallel().allMatch(inputBody::contains);
    }

    /**
     * Generates a unique flow instance name
     * @param flowId
     * @return
     */
    protected String createUniqueInstanceName(final String flowId) {
        return String.join("-", flowId, UUID.randomUUID().toString());
    }

    /**
     * Imports a flow
     * @param flowId
     * @param flowPackageBytes
     * @return
     */
    protected FlowDefinition importFlow(final String flowId, final byte[] flowPackageBytes) {
        logger.info("Importing flow, flowId = {}", flowId);

        FlowPackageUtil.validate(flowPackageBytes);
        try {
            final FlowDefinition flowDefinition = flowService.importFlow(flowPackageBytes);
            assertEquals(flowId, flowDefinition.getId());
            assertEquals(ENABLED.getStatus(), flowDefinition.getStatus());
            return flowDefinition;
        }
        catch (IllegalArgumentException e){
            throw new EntityNotFoundException(FLOW_PACKAGE_NOT_FOUND, FLOW_PACKAGE_NOT_FOUND.reason());
        }

    }

    /**
     * Checks if importing a flow fails with a given error code
     * @param flowId
     * @param flowPackageBytes
     * @param error
     */
    protected void checkFlowImportFails(final String flowId, final byte[] flowPackageBytes, ErrorCode error) {
        try {
            importFlow(flowId, flowPackageBytes);
            fail("Expected to fail with an exception here.");
        } catch (FlowAutomationException e){
            assertEquals(error.code(), e.getErrorReasons().get(0).getCode());
        }
    }

    /**
     * Checks if a given Flow Execution is in active state
     * @param flowExecution
     */
    protected void checkExecutionIsActive(final FlowExecution flowExecution) {
        checkExecutionIsActive(flowExecution.getFlowId(), flowExecution.getName());
    }

    /**
     * Starts a flow execution
     * @param flowDefinition
     * @param executionName
     * @return
     */
    protected FlowExecution startFlowExecution(final FlowDefinition flowDefinition, final String executionName) {
        final String flowId = flowDefinition.getId();

        logger.info("Executing flow, flowId={}, executionName={}", flowId, executionName);

        flowService.executeFlow(flowId, executionName);
        final List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowId).flowExecutionName(executionName).build());
        assertEquals(1, flowExecutions.size());
        return flowExecutions.get(0);
    }

    /**
     * Starts a flow exeuction by providing input file with inputs for all the user tasks
     * @param flowDefinition
     * @param executionName
     * @param fileName
     * @param fileBytes
     * @return
     */
    protected FlowExecution startFlowExecutionWithFile(final FlowDefinition flowDefinition, final String executionName, final String fileName,
                                                       final byte[] fileBytes) {
        final Map<String, byte[]> flowInputFiles = new HashMap<>();
        flowInputFiles.put(fileName, fileBytes);
        final String flowId = flowDefinition.getId();
        final FlowExecutionInput flowExecutionInput = new FlowExecutionInput(executionName, fileName, flowInputFiles);

        logger.info("Executing flow, flowId={}, flowExecutionInput={}", flowId, flowExecutionInput);

        flowService.executeFlow(flowId, flowExecutionInput);

        final List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowId).flowExecutionName(executionName).build());
        assertEquals(1, flowExecutions.size());
        return flowExecutions.get(0);
    }

    /**
     * Checks if a given user task is active
     * @param flowExecution
     * @param usertaskName
     * @return
     */
    protected UserTask checkUsertaskActive(final FlowExecution flowExecution, final String usertaskName) {
        return flowAutomationCheck.checkUsertaskActive(flowExecution, usertaskName, outputToConsole);
    }

    /**
     * Completes a user task
     * @param usertask
     * @param userTaskFileInput
     * @param userTaskInput
     */
    @SuppressWarnings("squid:S106")
    private void doCompleteUsertask(final UserTask usertask, final Map<String, byte[]> userTaskFileInput,
                                    final String userTaskInput) {
        logger.info("Completing usertask {}", usertask.getName());

        final UserTaskSchema schema = flowExecutionService.getUserTaskSchema(usertask.getId());
        assertNotNull(schema);

        /* Idun-2351 */
        if (outputToConsole) {
            try {
                System.out.println("-------------------------------- Usertask Submit ---------------------------");
                UsertaskRenderer.renderUsertask(usertask, schema, userTaskInput);
                System.out.println(SEPARATOR);

                showUsertaskCompletionData(userTaskFileInput, userTaskInput);

            } catch (final IOException e) {
                logger.error("Unable to show usertask: {}", e.getMessage());
                fail("Unable to show usertask: " + e.getMessage());
            }
        }
        flowExecutionService.completeUserTask(usertask.getId(), userTaskFileInput, userTaskInput);
    }

    /**
     * This method is used for going back to last user task for a running flow execution
     * @param flowExecution
     */
    protected void backUsertask(final FlowExecution flowExecution) {
        try {
            flowExecutionService.revertUserTask(flowAutomationQueries.getActiveUsertasksForExecution(flowExecution).get(0).getId());
        } catch (Exception e) {
            logger.error("Failed to go back to a task: {}", e.getMessage());
            fail("Failed to go back to a task: " + e.getMessage());
        }
    }

    /**
     * Checks if a completed user task has valid input values
     * @param flowExecution
     * @param usertaskName
     * @param checkBuilder
     */
    protected void checkUsertask(final FlowExecution flowExecution, final String usertaskName, final UsertaskCheckBuilder checkBuilder) {
        final UserTask userTask = flowAutomationCheck.checkUsertaskActive(flowExecution, usertaskName, false);
        final UserTaskSchema usertaskSchema = flowExecutionService.getUserTaskSchema(userTask.getId());
        assertNotNull(usertaskSchema);
        try {
            checkBuilder.performCheck(usertaskSchema.getSchema());
        } catch (final Exception e) {
            logger.error("checkUsertask() Failed to process: {}", e.getMessage());
            fail("checkUsertask() Failed to process: " + e.getMessage());
        }
    }

    /**
     * Checks if after completing all the user tasks in setup phase all the inputs to the user tasks are reflecting properly in Review and Confirm User task
     * @param flowExecution
     * @param checkBuilder
     */
    protected void checkUsertaskReviewAndConfirm(final FlowExecution flowExecution, final UsertaskCheckBuilder checkBuilder) {
        checkUsertask(flowExecution, REVIEW_AND_CONFIRM_EXECUTE, checkBuilder);
    }

    /**
     * Checks if Informational user task has valid values after completion
     * @param userTask
     * @param checkBuilder
     */
    protected void checkInformationalUsertask(UserTask userTask, final UsertaskCheckBuilder checkBuilder) {
        final UserTaskSchema usertaskSchema = flowExecutionService.getUserTaskSchema(userTask.getId());
        assertNotNull(usertaskSchema);
        try {
            checkBuilder.performCheck(usertaskSchema.getSchema());
        } catch (final Exception e) {
            logger.error("checkInformationalUsertask() Failed to process: {}", e.getMessage());
            fail("checkInformationalUsertask() Failed to process: " + e.getMessage());
        }
    }

    /**
     * Completes a user task in interactive mode
     * @param flowExecution
     */
    protected void completeUsertaskChooseSetupInteractive(final FlowExecution flowExecution) {
        final UserTask usertask = checkUsertaskActive(flowExecution, CHOOSE_SETUP);
        final String userTaskInput = "{\"setupType\":{\"interactive\":true}}";

        doCompleteUsertask(usertask, new HashMap<>(), userTaskInput);
    }

    /**
     * Completes a user task in non interactive mode by providing inputs using file input
     * @param flowExecution
     * @param filename
     * @param fileBytes
     */
    protected void completeUsertaskChooseSetupFile(final FlowExecution flowExecution, final String filename, final byte[] fileBytes) {
        final UserTask userTask = checkUsertaskActive(flowExecution, CHOOSE_SETUP);

        final Map<String, byte[]> userTaskFileInput = new HashMap<>();
        userTaskFileInput.put(filename, fileBytes);
        final String userTaskInput = "{\"setupType\":{\"fileInput\":{\"fileName\":\"" + filename + "\"}}}";
        doCompleteUsertask(userTask, userTaskFileInput, userTaskInput);
    }

    /**
     * Completes user task in non interactive mode by providing file input in zip format that has a referenced file
     * @param flowExecution
     * @param filename
     * @param fileBytes
     * @param extraFilename
     * @param extraFileBytes
     */
    protected void completeUsertaskChooseSetupFileWithOneReferencedFile(final FlowExecution flowExecution, final String filename,
                                                                        final byte[] fileBytes, final String extraFilename, final byte[] extraFileBytes) {
        final UserTask userTask = checkUsertaskActive(flowExecution, CHOOSE_SETUP);

        final Map<String, byte[]> userTaskFileInput = new HashMap<>();
        userTaskFileInput.put(filename, fileBytes);
        userTaskFileInput.put(extraFilename, extraFileBytes);
        final String userTaskInput = "{\"setupType\":{\"fileInput\":{\"fileName\":\"" + filename + "\"}}}";
        doCompleteUsertask(userTask, userTaskFileInput, userTaskInput);
    }

    /**
     * Completes a user task with File input
     * @param flowExecution
     * @param usertaskName
     * @param fileBytes
     */
    protected void completeUsertaskWithFile(final FlowExecution flowExecution, final String usertaskName, final byte[] fileBytes) {
        final UserTask userTask = checkUsertaskActive(flowExecution, usertaskName);
        final String userTaskInput = new String(fileBytes, StandardCharsets.UTF_8);
        doCompleteUsertask(userTask, new HashMap<>(), userTaskInput);
    }

    /**
     * Completes a user task
     * @param flowExecution
     * @param usertaskName
     * @param inputBuilder
     */
    protected void completeUsertask(final FlowExecution flowExecution, final String usertaskName, final UsertaskInputBuilder inputBuilder) {
        try {
            final UserTask userTask = checkUsertaskActive(flowExecution, usertaskName);
            final UserTaskSchema usertaskSchema = flowExecutionService.getUserTaskSchema(userTask.getId());
            assertNotNull(usertaskSchema);
            final String userTaskInput = inputBuilder.buildInputString(usertaskSchema.getSchema());
            final Map<String, byte[]> userTaskFileInput = inputBuilder.getFileInput();

            doCompleteUsertask(userTask, userTaskFileInput, userTaskInput);
        } catch (final Exception e) {
            logger.error("completeUsertask() Failed to process: {}", e.getMessage());
            fail("completeUsertask() Failed to process: " + e.getMessage());
        }
    }

    protected void completeUserTaskWithProcessingError(final FlowExecution flowExecution, final String usertaskName,
                                                       final UsertaskInputBuilder inputBuilder, String expectedErrorMessage,
                                                       String expectedErrorCode) {
        try {
            final UserTask userTask = checkUsertaskActive(flowExecution, usertaskName);
            final UserTaskSchema usertaskSchema = flowExecutionService.getUserTaskSchema(userTask.getId());
            assertNotNull(usertaskSchema);
            final String userTaskInput = inputBuilder.buildInputString(usertaskSchema.getSchema());
            final Map<String, byte[]> userTaskFileInput = inputBuilder.getFileInput();

            doCompleteUsertask(userTask, userTaskFileInput, userTaskInput);
        } catch (Exception exception) {
            Throwable rootCause = getRootCause(exception);
            if (FlowAutomationException.class.isAssignableFrom(rootCause.getClass())) {
                FlowAutomationException error = (FlowAutomationException) rootCause;
                assertEquals(expectedErrorCode, error.getErrorReasons().get(0).getCode());
                assertEquals(expectedErrorMessage, error.getErrorReasons().get(0).getErrorMessage());
            } else {
                fail("Unexpected exception happened: " + exception.getMessage());
            }
        }
    }

    /**
     * Completes a user task without any input
     * @param flowExecution
     * @param usertaskName
     */
    protected void completeUsertaskNoInput(final FlowExecution flowExecution, final String usertaskName) {
        final UserTask usertask = checkUsertaskActive(flowExecution, usertaskName);
        completeUsertask(flowExecution, usertask);
    }

    /**
     * Completes Review and Confirm user task
     * @param flowExecution
     */
    protected void completeUsertaskReviewAndConfirm(final FlowExecution flowExecution) {
        completeUsertaskNoInput(flowExecution, REVIEW_AND_CONFIRM_EXECUTE);
    }

    /**
     * Gets flow execution report
     * @param flowExecution
     * @return
     */
    protected String getExecutionReport(final FlowExecution flowExecution) {
        final String executionReport = flowExecutionService.getExecutionReport(flowExecution.getFlowId(), flowExecution.getName());
        assertNotNull(executionReport);
        return executionReport;
    }

    /**
     * Gets flow execution report schema
     * @param flowExecution
     * @return
     */
    protected String getExecutionReportSchema(final FlowExecution flowExecution) {
        final String executionReportSchema = flowExecutionService.getExecutionReportSchema(flowExecution.getFlowId(), flowExecution.getName());
        assertNotNull(executionReportSchema);
        return executionReportSchema;
    }

    /**
     * Deletes a flow execution
     * @param flowId
     * @param flowExecutionName
     */
    protected void deleteExecution(final String flowId, final String flowExecutionName) {
        retryManager.executeWithRetries(() -> {
            flowExecutionService.deleteExecution(flowId, flowExecutionName);
            return null;
        });
    }

    /**
     * Completes a user task
     * @param flowExecution
     * @param usertask
     */
    protected void completeUsertask(final FlowExecution flowExecution, final UserTask usertask) {
        checkUsertaskActive(flowExecution, usertask.getName());
        doCompleteUsertask(usertask, new HashMap<>(), "{}");
    }

    /**
     * Deletes a flow
     * @param flowId
     */
    protected void removeFlow(final String flowId) {
        retryManager.executeWithRetries(() -> {
            flowService.deleteFlow(flowId, true);
            return null;
        });
    }

    /**
     * Disables a flow
     * @param flowId
     */
    protected void disableFlow(String flowId){
        flowService.enableFlow(flowId, false);
    }

    /**
     * Enables a flow
     * @param flowId
     */
    protected void enableFlow(String flowId){
        flowService.enableFlow(flowId, true);
    }

    /**
     * Activates a flow version
     * @param flowId
     * @param flowVersion
     */
    protected void activateFlow(String flowId, String flowVersion){
        flowService.activateFlowVersion(flowId, flowVersion, true);
    }

    /**
     * Deactivates a flow version
     * @param flowId
     * @param flowVersion
     */
    protected void deactivateFlow(String flowId, String flowVersion){
        flowService.activateFlowVersion(flowId, flowVersion, false);
    }

    /**
     * Checks that a disabled flow can not be executed
     * @param flowDefinition
     * @param executionName
     */
    protected void checkDisabledFlowCantBeExecuted(FlowDefinition flowDefinition, String executionName) {
        try {
            startFlowExecution(flowDefinition, executionName);
            fail("Expected an exception while initiating a disabled flow.");
        }catch (FlowAutomationException e) {
            logger.debug("Failed to process: {}", e.getMessage());
            assertEquals(ACTIVE_FLOW_VERSION_DOESNT_EXIST.code(), e.getErrorReasons().get(0).getCode());
        }
    }

    /**
     * Shows flow execution report and report JSON object
     * @param flowExecution
     */
    protected void checkExecutionReport(final FlowExecution flowExecution) {
        final String report = flowExecutionService.getExecutionReport(flowExecution.getFlowId(), flowExecution.getName());
        final String reportSchema = flowExecutionService.getExecutionReportSchema(flowExecution.getFlowId(), flowExecution.getName());
        showExecutionReportSchema(reportSchema);
        showExecutionReportObject(report);
        showExecutionReport(reportSchema, report);
    }

    /**
     * Checks if report contains a report table
     * @param flowExecution
     * @param tableIdentifier
     */
    protected void checkReportTable(final FlowExecution flowExecution, final String tableIdentifier) {
        final String executionReport = getExecutionReport(flowExecution);
        assertTrue(executionReport.contains(tableIdentifier));
    }

    protected Throwable getRootCause(final Throwable throwable) {
        return Stream.iterate(throwable, Throwable::getCause).filter(element -> element.getCause() == null).findFirst().orElse(throwable);
    }

    /**
     * Shows flow execution report in json object format
     * @param report
     */
    @SuppressWarnings("squid:S106")
    private void showExecutionReportObject(final String report) {
        /* Idun-2351 */
        if (outputToConsole) {
            System.out.println("------------------------- Execution Report Object --------------------------");
            if (report == null || report.isEmpty()) {
                System.out.println("None");
            } else {
                System.out.println(niceJson(report));
            }
            System.out.println(SEPARATOR);
        }
    }

    /**
     * Shows flow execution report
     * @param reportSchema
     * @param report
     */
    @SuppressWarnings("squid:S106")
    private void showExecutionReport(final String reportSchema, final String report) {
        /* Idun-2351 */
        if (outputToConsole) {
            System.out.println("---------------------------- Execution Report ------------------------------");
            if (report == null || report.isEmpty()) {
                System.out.println("None");
            } else {
                try {
                    ReportRenderer.renderReport(reportSchema, report);
                } catch (final IOException e) {
                    logger.error("showExecutionReport() Failed to process: {}", e.getMessage());
                    fail("Unable to show report: " + e.getMessage());
                }
            }
            System.out.println(SEPARATOR);
        }
    }

    private void showExecutionReportSchema(final String reportSchema) {
        /* Idun-2351 */
        if (outputToConsole) {
            System.out.println("------------------------- Execution Report Object Schema --------------------------");
            if (reportSchema == null || reportSchema.isEmpty()) {
                System.out.println("None");
            } else {
                System.out.println(niceJson(reportSchema));
            }
            System.out.println(SEPARATOR);
        }
    }

    /**
     * Suspends a running flow execution
     * @param flowExecution
     */
    protected void suspendExecution(final FlowExecution flowExecution) {
        flowExecutionService.suspendExecution(flowExecution.getFlowId(), flowExecution.getName());
    }

    /**
     * Suspends all the running execution of a given flow version
     * @param flowId
     * @param flowVersion
     */
    protected void suspendAllExecutionsForFlowVersion(final String flowId, final String flowVersion) {
        flowExecutionService.suspendAllExecutionsForFlowVersion(flowId, flowVersion);
    }

    /**
     * Stops a running flow execution
     * @param flowExecution
     */
    protected void stopExecution(final FlowExecution flowExecution) {
        flowExecutionService.stopExecution(flowExecution.getFlowId(), flowExecution.getName());
    }

    /**
     * Checks if all the flow instances of a given flow version are stopped
     * @param flowId
     * @param flowVersion
     */
    protected void checkAllExecutionsSuspendedForFlowVersion(final String flowId, final String flowVersion) {
        final List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowId).flowVersion(flowVersion).build());
        flowExecutions.forEach(flowExecution -> assertEquals("SUSPENDED", flowExecution.getState()));
    }

    /**
     * Checks if a flow execution is in SUSPENDED state
     * @param flowExecution
     */
    protected void checkExecutionSuspended(final FlowExecution flowExecution) {
        checkExecutionState(flowExecution,"SUSPENDED");
    }

    /**
     * Checks if a flow execution is in COMPLETED state
     * @param flowExecution
     */
    protected void checkExecutionExecuted(final FlowExecution flowExecution) {
        checkExecutionState(flowExecution,"COMPLETED");
    }

    /**
     * Checks if a flow execution is in STOPPED state
     * @param flowExecution
     */
    protected void checkExecutionStopped(final FlowExecution flowExecution) {
        checkExecutionState(flowExecution,"STOPPED");
    }

    /**
     * Checks if a flow execution is in FAILED state
     * @param flowExecution
     */
    protected void checkExecutionFailed(final FlowExecution flowExecution) {
        checkExecutionState(flowExecution,"FAILED");
    }

    /**
     * Checks if a flow execution is in FAILED_SETUP state
     * @param flowExecution
     */
    protected void checkExecutionFailedSetup(final FlowExecution flowExecution) {
        checkExecutionState(flowExecution,"FAILED_SETUP");
    }

    /**
     * Checks if a flow execution is in FAILED_EXECUTE state
     * @param flowExecution
     */
    protected void checkExecutionFailedExecute(final FlowExecution flowExecution) {
        checkExecutionState(flowExecution, "FAILED_EXECUTE");
    }

    /**
     * Checks if a flow execution is in COMPLETED state. Returns true if yes otherwise false
     * @param flowExecution
     */
    protected boolean isExecutionExecuted(final FlowExecution flowExecution) {
        return getExecutionState(flowExecution).equals("COMPLETED");
    }

    /**
     * Gets Execution Report of a running flow instance
     * @param flowExecution
     * @return
     */
    protected FlowExecutionResource downloadFlowExecutionReport(final FlowExecution flowExecution) {
        return downloadFlowExecutionResource(flowExecution, "report");
    }

    /**
     * Gets flow input data of a running flow instance
     * @param flowExecution
     * @return
     */
    protected FlowExecutionResource downloadFlowExecutionInput(final FlowExecution flowExecution) {
        return downloadFlowExecutionResource(flowExecution, "flowinput");
    }

    /**
     * Gets flow input data as well as report for a running flow instance
     * @param flowExecution
     * @return
     */
    protected FlowExecutionResource downloadFlowExecutionAllResources(final FlowExecution flowExecution) {
        return downloadFlowExecutionResource(flowExecution, "all");
    }


    private FlowExecutionResource downloadFlowExecutionResource(final FlowExecution flowExecution, final String resource) {
        /* Idun-2141 */
        try {
            final FlowExecutionResource executionResource = flowExecutionService.getExecutionResource(flowExecution.getFlowId(), flowExecution.getName(), resource);
            /* IDUN-2141 */
            final String errMsg = String.format("Failed to download FlowExecutionResource: id %s, name %s, resource %s", flowExecution.getFlowId(), flowExecution.getName(), resource);
            assertNotNull(errMsg, executionResource);
            return executionResource;
        } catch (FlowResourceNotFoundException e) {
            throw(e);
        }
    }

    /**
     * Checks if a flow execution has any input data
     * @param flowExecution
     */
    protected void checkNoExecutionInputAvailable(final FlowExecution flowExecution) {
        try {
            getExecutionInput(flowExecution);
        } catch (final FlowResourceNotFoundException e) {
            return;
        }
        fail("Expected to receive FlowResourceNotFoundException");
    }

    @SuppressWarnings("squid:S106")
    private void showUsertaskCompletionData(final Map<String, byte[]> userTaskFileInput, final String userTaskInput) {
        /* Idun-2351 */
        if (outputToConsole) {
            System.out.println("--------------------------- Usertask Completion Data -----------------------");
            if (userTaskInput == null || userTaskInput.isEmpty()) {
                System.out.println("None");
            } else {
                System.out.println(niceJson(userTaskInput));
                System.out.println("Files: " + filesInfo(userTaskFileInput));
            }
            System.out.println(SEPARATOR);
        }
    }

    /**
     * Checks if a user task has a given nameExtra field
     * @param userTask
     * @param nameExtra
     */
    protected void checkUserTaskNameExtra(UserTask userTask,String nameExtra){
        assertNotNull(userTask);
        assertNotNull(nameExtra);
        if(!userTask.getNameExtra().equals(nameExtra)){
            fail("Expected nameExtra : " + nameExtra + " but received "+userTask.getNameExtra());
        }
    }

    @SuppressWarnings({"squid:S1643"})
    private String filesInfo(final Map<String, byte[]> files) {
        String info = "[ ";

        final Iterator<Map.Entry<String, byte[]>> iterator = files.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, byte[]> file = iterator.next();
            info += file.getKey() + "(size:" + file.getValue().length + ") ";
        }

        info += "]";
        return info;
    }

    /**
     * Checks if a given flow execution event with message and severity in string format is recorded
     * @param execution
     * @param severity
     * @param message
     */
    protected void checkExecutionEventIsRecorded(FlowExecution execution, String severity, String message) {
        checkExecutionEventIsRecorded(execution, FlowExecutionEventSeverity.valueOf(severity), message, null);
    }

    /**
     *  Checks if a given flow execution event with message and severity  is recorded
     * @param execution
     * @param severity
     * @param message
     */
    protected void checkExecutionEventIsRecorded(FlowExecution execution, FlowExecutionEventSeverity severity, String message) {
        checkExecutionEventIsRecorded(execution, severity, message, null);
    }

    /**
     * Checks if a givne report variable and its value is present in the flow execution report
     * @param flowExecution
     * @param variable
     * @param content
     */
    protected void downloadAndCheckReportVariableContentContains(FlowExecution flowExecution, String variable, String ... contents) {
        final FlowExecutionResource executionReportVariable = flowExecutionService.getExecutionReportVariable(flowExecution.getFlowId(), flowExecution.getName(), variable);
        assertEquals(variable, executionReportVariable.getName());
        final String variableContent = new String(executionReportVariable.getData(), StandardCharsets.UTF_8);
        for (String content : contents) {
            assertTrue(variableContent.contains(content));
        }
    }

    /**
     * Checks if a givne report variable has null value in the flow execution report
     * @param flowExecution
     * @param variableName
     */
    protected void checkIfNullExecutionReportVariable(final FlowExecution flowExecution, final String variableName) {
        FlowExecutionResource executionResource;
        try {
            executionResource = flowExecutionService.getExecutionReportVariable(flowExecution.getFlowId(), flowExecution.getName(), variableName);
        } catch (FlowResourceNotFoundException e) {
            logger.warn("checkIfNullExecutionReportVariable() Failed to process: {}", e.getMessage());
            executionResource = null;
        }
        assertNull(executionResource);
    }

    /**
     * Gets the value of a given report variable
     * @param flowExecution
     * @param variableName
     * @return
     */
    protected String getExecutionReportVariableContent(final FlowExecution flowExecution, final String variableName) {
        final FlowExecutionResource executionResource = flowExecutionService.getExecutionReportVariable(flowExecution.getFlowId(), flowExecution.getName(), variableName);
        assertNotNull(executionResource);
        assertNotNull(executionResource.getData());
        return new String(executionResource.getData(), StandardCharsets.UTF_8);
    }

    /**
     * Gets and check if the given report variable name and value are present in the report
     * @param flowExecution
     * @param variable
     * @param content
     */
    protected void downloadAndCheckReportVariableContentEqualsTo(FlowExecution flowExecution, String variable, String content) {
        final FlowExecutionResource executionReportVariable = flowExecutionService.getExecutionReportVariable(flowExecution.getFlowId(), flowExecution.getName(), variable);
        assertEquals(variable, executionReportVariable.getName());
        final String variableContent = new String(executionReportVariable.getData(), StandardCharsets.UTF_8);
        assertTrue(variableContent.equals(content));
    }

    //Compatibility methods / use FlowAutomationCheck and FlowAutomationQueries directly

    /**
     * Gets numbers of flow imported
     * @return
     */
    @Override
    public int getNumberFlowsImported() {
        return flowAutomationQueries.getNumberFlowsImported();
    }

    /**
     * Checks if number of  flow instances match the given number
     * @param number
     */
    @Override
    public void checkNumberOfExecutions(int number) {
        flowAutomationCheck.checkNumberOfExecutions(number);
    }

    /**
     * Checks if number of imported flows match the given number
     * @param number
     */
    @Override
    public void checkNumberOfFlowsImported(int number) {
        flowAutomationCheck.checkNumberOfFlowsImported(number);
    }

    /**
     * Checks if number of active flow instances match the given number
     * @param number
     */
    @Override
    public void checkNumberOfActiveExecutions(int number) {
        flowAutomationCheck.checkNumberOfActiveExecutions(number);
    }

    /**
     * Checks if number of flow instances of given flow match the given number
     * @param number
     */
    @Override
    public void checkNumberOfExecutionsForFlow(String flowId, int number) {
        flowAutomationCheck.checkNumberOfExecutionsForFlow(flowId, number);
    }

    /**
     * Checks if number of active flow instances of a given flow match the given number
     * @param number
     */
    @Override
    public void checkNumberOfActiveExecutionsForFlow(String flowId, int number) {
        flowAutomationCheck.checkNumberOfActiveExecutionsForFlow(flowId, number);
    }

    /**
     *  Checks if given flow execution of a flow is deleted
     * @param flowId
     * @param executionName
     */
    @Override
    public void checkExecutionIsDeleted(String flowId, String executionName) {
        flowAutomationCheck.checkExecutionIsDeleted(flowId, executionName);
    }


    /**
     *  Checks if given flow execution of a flow is active
     * @param flowId
     * @param executionName
     */
    @Override
    public void checkExecutionIsActive(String flowId, String executionName) {
        flowAutomationCheck.checkExecutionIsActive(flowId, executionName);
    }

    /**
     *  Checks if given flow execution of a flow is inactive
     * @param flowId
     * @param executionName
     */
    @Override
    public void checkExecutionIsInactive(String flowId, String executionName) {
        flowAutomationCheck.checkExecutionIsInactive(flowId, executionName);
    }

    /**
     * Checks if a flow execution is in given state
     * @param flowExecution
     * @param expectedState
     */
    @Override
    public void checkExecutionState(FlowExecution flowExecution, String expectedState) {
        flowAutomationCheck.checkExecutionState(flowExecution, expectedState);
    }

    /**
     * Checks if a flow exeuction has a given summary report
     * @param flowExecution
     * @param summaryReport
     */
    public void checkExecutionSummaryReport(FlowExecution flowExecution, String summaryReport) {
        flowAutomationCheck.checkExecutionSummaryReport(flowExecution, summaryReport, outputToConsole);
    }

    /**
     * Checks if a flow exeuction has a given summary report
     * @param flowExecution
     * @param summaryReport
     * @param outputToConsole
     */
    @Override
    public void checkExecutionSummaryReport(FlowExecution flowExecution, String summaryReport, boolean outputToConsole) {
        flowAutomationCheck.checkExecutionSummaryReport(flowExecution, summaryReport, outputToConsole);
    }

    /**
     * Checks if a flow execution event with given severity and message is recorded
     * @param execution
     * @param severity
     * @param message
     * @param target
     */
    @Override
    public void checkExecutionEventIsRecorded(FlowExecution execution, FlowExecutionEventSeverity severity, String message, String target) {
        flowAutomationCheck.checkExecutionEventIsRecorded(execution, severity, message, target);
    }

    /**
     * Checks if a flow executions has given input
     * @param flowExecution
     * @param checkBuilder
     */
    @Override
    public void checkExecutionInput(FlowExecution flowExecution, AbstractFlowInputCheckBuilder checkBuilder) {
        flowAutomationCheck.checkExecutionInput(flowExecution, checkBuilder);
    }

    /**
     * Checks if a given user task is active and returns that user task
     * @param flowExecution
     * @param usertaskName
     * @param display
     * @return
     */
    @Override
    public UserTask checkUsertaskActive(FlowExecution flowExecution, String usertaskName, boolean display) {
        return flowAutomationCheck.checkUsertaskActive(flowExecution, usertaskName, display);
    }

    /***
     * Gets active executions of a flow
     * @param flowId
     * @return
     */
    @Override
    public List<FlowExecution> getActiveExecutionsForFlow(String flowId) {
        return flowAutomationQueries.getActiveExecutionsForFlow(flowId);
    }

    /**
     * Gets all the active executions
     * @return
     */
    @Override
    public List<FlowExecution> getActiveExecutions() {
        return flowAutomationQueries.getActiveExecutions();
    }

    /**
     * Gets all the executions of a flow
     * @param flowId
     * @return
     */
    @Override
    public List<FlowExecution> getExecutionsForFlow(String flowId) {
        return flowAutomationQueries.getExecutionsForFlow(flowId);
    }

    /**
     * Gets list of active user tasks  of a flow execution
     * @param flowExecution
     * @return
     */
    @Override
    public List<UserTask> getActiveUsertasksForExecution(FlowExecution flowExecution) {
        return flowAutomationQueries.getActiveUsertasksForExecution(flowExecution);
    }

    /**
     * Gets list of all user tasks for a flow execution
     * @param flowExecution
     * @return
     */
    @Override
    public List<UserTask> getAllUsertasksForExecution(FlowExecution flowExecution) {
        return flowAutomationQueries.getAllUsertasksForExecution(flowExecution);
    }

    /**
     * Gets list of user tasks of a flow execution for a given user task name
     * @param flowExecution
     * @param usertaskName
     * @return
     */
    @Override
    public List<UserTask> getUserTasks(FlowExecution flowExecution, String usertaskName) {
        return flowAutomationQueries.getUserTasks(flowExecution, usertaskName);
    }

    /**
     * Get execution state of a flow execution
     * @param flowExecution
     * @return
     */
    @Override
    public String getExecutionState(FlowExecution flowExecution) {
        return flowAutomationQueries.getExecutionState(flowExecution);
    }

    /**
     * Gets Input data of a flow execution
     * @param flowExecution
     * @return
     */
    @Override
    public String getExecutionInput(FlowExecution flowExecution) {
        return flowAutomationQueries.getExecutionInput(flowExecution);
    }


    /**
     * Checks if Process Details of a given flow has valid values
     *
     * @param flowDefinition
     * @param flowVersion
     * @param expectedVersionProcessDetails
     */
    public void checkProcessDetailsForFlowVersion(FlowDefinition flowDefinition, String flowVersion, FlowVersionProcessDetails expectedVersionProcessDetails) {
        flowAutomationCheck.checkProcessDetailsForFlowVersion(flowDefinition, flowVersion, expectedVersionProcessDetails);
    }

    /**
     * Checks if flow definition of a given flow has valid values
     *
     * @param flowId
     * @param expectedDefinition
     */
    public void checkFlowDefinition(String flowId, FlowDefinition expectedDefinition) {
        flowAutomationCheck.checkFlowDefinition(flowId, expectedDefinition);
    }

    /**
     * Checks if the flow input schema of a given flow has valid values
     *
     * @param flowDefinition
     * @param flowVersion
     * @param expectedSchema
     */
    public void checkFlowInputSchemaForFlowVersion(FlowDefinition flowDefinition, String flowVersion, String expectedSchema) {
        flowAutomationCheck.checkFlowInputSchemaForFlowVersion(flowDefinition, flowVersion, expectedSchema);
    }

    /**
     * Checks if the flow version of a given flow is active
     *
     * @param flowId
     * @param flowVersion
     * @param expectedIsActive
     */
    public void checkFlowVersionIsActive(String flowId, String flowVersion, boolean expectedIsActive) {
        flowAutomationCheck.checkFlowVersionIsActive(flowId, flowVersion, expectedIsActive);
    }

    public static boolean isValidDateTime(String dateTime)
    {
        /** sample dateTime format - 2023-03-08T14:49:37.903+0000 */
        String regex = "^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2}(?:\\.\\d{3})?)[+-](\\d{4})$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dateTime);
        return matcher.matches();
    }
}
