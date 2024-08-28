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
package com.ericsson.oss.services.flowautomation.test.fwk.check;

import static com.ericsson.oss.services.flowautomation.test.fwk.BasicUtils.niceJson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.api.FlowService;
import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEvent;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventFilter;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventSeverity;
import com.ericsson.oss.services.flowautomation.model.FlowVersion;
import com.ericsson.oss.services.flowautomation.model.FlowVersionProcessDetails;
import com.ericsson.oss.services.flowautomation.model.PaginatedData;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import com.ericsson.oss.services.flowautomation.model.UserTaskSchema;
import com.ericsson.oss.services.flowautomation.test.fwk.AbstractFlowInputCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskRenderer;
import com.ericsson.oss.services.flowautomation.test.fwk.query.FlowAutomationQueries;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class FlowAutomationCheckImpl implements FlowAutomationCheck {

    private static final String SEPARATOR = "----------------------------------------------------------------------------";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    FlowService flowService;
    protected FlowExecutionService flowExecutionService;
    protected FlowAutomationQueries flowAutomationQueries;

    public FlowAutomationCheckImpl(FlowService flowService, FlowExecutionService flowExecutionService, FlowAutomationQueries flowAutomationQueries) {
        this.flowService = flowService;
        this.flowExecutionService = flowExecutionService;
        this.flowAutomationQueries = flowAutomationQueries;
    }

    @Override
    public void checkNumberOfFlowsImported(final int number) {
        logger.info("Checking if number of imported flows = {}", number);
        assertEquals(number, flowAutomationQueries.getNumberFlowsImported());
    }

    @Override
    public void checkNumberOfExecutions(final int number) {
        logger.info("Checking if number of executions = {}", number);
        final List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().build());
        assertEquals(number, flowExecutions.size());
    }

    @Override
    public void checkNumberOfExecutionsForFlow(final String flowId, final int number) {
        logger.info("Checking if number of executions for flow {} = {}", flowId, number);
        assertEquals(number, flowAutomationQueries.getExecutionsForFlow(flowId).size());
    }

    @Override
    public void checkNumberOfActiveExecutionsForFlow(final String flowId, final int number) {
        logger.info("Checking if number of active executions for flow {} = {}", flowId, number);
        assertEquals(number, flowAutomationQueries.getActiveExecutionsForFlow(flowId).size());
    }

    @Override
    public void checkExecutionIsDeleted(final String flowId, final String executionName) {
        logger.info("Checking If the execution of the flow is deleted {},{}", flowId, executionName);
        final List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowId).flowExecutionName(executionName).build());
        assertEquals(0, flowExecutions.size());
    }

    @Override
    public void checkNumberOfActiveExecutions(final int number) {
        logger.info("Checking if number of active executions = {}", number);
        assertEquals(number, flowAutomationQueries.getActiveExecutions().size());
    }

    @Override
    public void checkExecutionIsActive(final String flowId, final String executionName) {
        logger.info("Checking If the execution of the flow is active {},{}", flowId, executionName);
        final List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowId).flowExecutionName(executionName).build());
        assertEquals(1, flowExecutions.size());
        assertNull(flowExecutions.get(0).getEndTime());
    }

    @Override
    public void checkExecutionIsInactive(final String flowId, final String executionName) {
        logger.info("Checking If the execution of the flow is Inactive {},{}", flowId, executionName);
        final List<FlowExecution> flowExecutions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowId).flowExecutionName(executionName).build());
        assertEquals(1, flowExecutions.size());
        assertNotNull(flowExecutions.get(0).getEndTime());
    }

    @Override
    public void checkExecutionState(final FlowExecution flowExecution, final String expectedState) {
        assertEquals(expectedState, flowAutomationQueries.getExecutionState(flowExecution));
    }

    @Override
    @SuppressWarnings("squid:S106")
    public UserTask checkUsertaskActive(final FlowExecution flowExecution, final String usertaskName, final boolean outputToConsole) {
        final String flowId = flowExecution.getFlowId();
        final String executionName = flowExecution.getName();

        logger.info("Checking if usertask with name {} is active for flowId {} executionName {}", usertaskName, flowId, executionName);

        final List<UserTask> utList = flowAutomationQueries.getUserTasks(flowExecution, usertaskName);
        assertFalse(utList.isEmpty());
        final UserTask usertask = utList.get(0);
        final UserTaskSchema schema = flowExecutionService.getUserTaskSchema(usertask.getId());
        assertNotNull(schema);

        if (outputToConsole) {
            try {
                final String niceSchemaString = niceJson(schema.getSchema());

                logger.info("---------------------------------- Usertask Schema ---------------------------");
                logger.info(niceSchemaString);
                logger.info(SEPARATOR);

                logger.info("---------------------------------- Usertask ----------------------------------");
                UsertaskRenderer.renderUsertask(usertask, schema);
                logger.info(SEPARATOR);
            } catch (final IOException e) {
                fail("checkUsertaskActive() Unable to show usertask schema: " + e.getMessage());
            }
        }
        return usertask;
    }

    @Override
    public void checkExecutionSummaryReport(final FlowExecution flowExecution, final String summaryReport, final boolean outputToConsole) {

        final List<FlowExecution> executions = flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().flowId(flowExecution.getFlowId()).flowExecutionName(flowExecution.getName()).build());
        assertNotNull(executions);
        assertEquals(1, executions.size());
        final FlowExecution execution = executions.get(0);
        if (outputToConsole) {
            logger.info("------------------------- Execution Summary Report --------------------------");
            logger.info(execution.getSummaryReport());
            logger.info(SEPARATOR);
        }
        assertEquals(summaryReport, execution.getSummaryReport());
    }

    @Override
    public void checkExecutionEventIsRecorded(FlowExecution execution, FlowExecutionEventSeverity severity, String message, String target) {

        final FlowExecutionEventFilter.Builder eventsFilter = new FlowExecutionEventFilter.Builder(execution.getFlowId(), execution.getName())
                .eventsSeverity(Collections.singletonList(severity))
                .message(message);

        if(target != null) {
            eventsFilter.target(target);
        }

        final PaginatedData<FlowExecutionEvent> executionEvents = flowExecutionService.getExecutionEvents(eventsFilter.build());
        final long eventCount = executionEvents.getRecords().stream()
                .filter(event -> Objects.equals(target, event.getTarget()))
                .count();

        assertTrue("Recorded event is not found in the response", eventCount > 0);
    }

    @Override
    public void checkExecutionInput(final FlowExecution flowExecution, final AbstractFlowInputCheckBuilder checkBuilder) {
        final String flowInputSchemaAndData = flowAutomationQueries.getExecutionInput(flowExecution);
        try {
            checkBuilder.performCheck(flowInputSchemaAndData);
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    @Override
    public void checkProcessDetailsForFlowVersion(FlowDefinition flowDefinition, String flowVersion, FlowVersionProcessDetails expectedVersionProcessDetails) {

        try {
            FlowVersionProcessDetails flowVersionProcessDetails = flowService.getProcessDetailsForFlowVersion(flowDefinition.getId(), flowVersion);
            assertNotNull(flowVersion);

            assertEquals("Checking Get process-details for flow Version: getProcessId",
                    flowVersionProcessDetails.getProcessId(), expectedVersionProcessDetails.getProcessId());
            assertEquals("Checking Get process-details for flow Version: getSetupProcessId",
                    flowVersionProcessDetails.getSetupProcessId(), expectedVersionProcessDetails.getSetupProcessId());
            assertEquals("Checking Get process-details for flow Version: getExecuteProcessId",
                    flowVersionProcessDetails.getExecuteProcessId(), expectedVersionProcessDetails.getExecuteProcessId());

        } catch (FlowResourceNotFoundException e) {
            fail("No expected exception while as running the test.");
        }
    }

    @Override
    public void checkFlowDefinition(String flowId, FlowDefinition expectedDefinition) {

        try {
            final FlowDefinition flowDefinition = flowService.getFlowDefinition(flowId);
            assertNotNull(flowDefinition);

            assertEquals("Checking flow definition: getId", flowDefinition.getId(), expectedDefinition.getId());
            assertEquals("Checking flow definition: getName", flowDefinition.getName(), expectedDefinition.getName());
            assertEquals("Checking flow definition: getStatus", flowDefinition.getStatus(), expectedDefinition.getStatus());
            assertEquals("Checking flow definition: getSource", flowDefinition.getSource(), expectedDefinition.getSource());

            List<FlowVersion> flowDefinitions = flowDefinition.getFlowVersions();
            assertNotNull(flowDefinitions);
            List<FlowVersion> definitionsTesting = expectedDefinition.getFlowVersions();
            assertNotNull(definitionsTesting);

            assertEquals("Checking flow definition Version: same size?", flowDefinitions.size(), definitionsTesting.size());

            for(int i = 0; i < flowDefinitions.size(); i++) {
                FlowVersion objDefinition = flowDefinitions.get(i);
                FlowVersion expectedObjDefinition = definitionsTesting.get(i);
                assertEquals("Checking flow definition Version: getVersion", objDefinition.getVersion(), expectedObjDefinition.getVersion());
                assertEquals("Checking flow definition Version: getDescription", objDefinition.getDescription(), expectedObjDefinition.getDescription());
                assertEquals("Checking flow definition Version: isActive", objDefinition.isActive(), expectedObjDefinition.isActive());
                assertEquals("Checking flow definition Version: getCreatedBy", objDefinition.getCreatedBy(), expectedObjDefinition.getCreatedBy());
                assertNotNull("Checking flow definition Version: getCreatedDate", objDefinition.getCreatedDate());
                assertEquals("Checking flow definition Version: isSetupPhaseRequired", objDefinition.isSetupPhaseRequired(), expectedObjDefinition.isSetupPhaseRequired());
            }
        } catch (FlowResourceNotFoundException e) {
            fail("No expected exception while as running the test.");
        }
    }

    @Override
    public void checkFlowInputSchemaForFlowVersion(FlowDefinition flowDefinition, String flowVersion, String expectedSchema) {

        try {
            String schema = flowService.getFlowInputSchema(flowDefinition.getId(), flowVersion);
            assertNotNull(schema);

            ObjectMapper om = new ObjectMapper();
            Map<String, Object> mapJsonSchema = (Map<String, Object>) (om.readValue(schema, Map.class));
            Map<String, Object> mapJsonExpectedSchema = (Map<String, Object>) (om.readValue(expectedSchema, Map.class));

            assertEquals("Checking Flow Input Schema:", mapJsonSchema, mapJsonExpectedSchema);
        } catch (Exception e) {
            fail("No expected exception while as running the test.");
        }
    }

    @Override
    public void checkFlowVersionIsActive(String flowId, String flowVersion, boolean expectedIsActive) {

        try {
            List<FlowVersion> flowVersions = flowService.getFlowDefinition(flowId).getFlowVersions();
            boolean found = false;

            for (FlowVersion flowVersionObject : flowVersions ) {
                if (Objects.equals(flowVersionObject.getVersion(), flowVersion)) {
                    assertEquals(flowVersionObject.isActive(), expectedIsActive);
                    found = true;
                }
            }

            if(!found) fail("Failed to find expected active status");
        } catch (Exception e) {
            fail("No expected exception while as running the test.");
        }
    }

}
