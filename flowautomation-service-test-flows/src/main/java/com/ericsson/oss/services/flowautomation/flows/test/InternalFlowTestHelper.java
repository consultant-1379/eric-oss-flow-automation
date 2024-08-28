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

package com.ericsson.oss.services.flowautomation.flows.test;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.realSleepMs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.jse.JseTestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Internal flow test helper.
 */
public class InternalFlowTestHelper {

    protected static ProcessEngine processEngine = JseTestContext.getInstance().getProcessEngineContainer().getProcessEngine();

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalFlowTestHelper.class);

    private InternalFlowTestHelper(){
        //
    }


    /**
     * Test internal flow history.
     *
     * @param flowExecution the flow execution
     */
    public static void  testInternalFlowHistory(FlowExecution flowExecution) {
        String parentProcessInstanceId = flowExecution.getProcessInstanceId();
        HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(parentProcessInstanceId).singleResult();
        assertNotNull(historicProcessInstance);

        String businessKey = historicProcessInstance.getBusinessKey();
        final List<String> processInstanceIds = new ArrayList<>();
        getProcessInstanceIds(parentProcessInstanceId, processInstanceIds);
        processInstanceIds.add(parentProcessInstanceId);

        assertEquals(processEngine.getHistoryService().createHistoricTaskInstanceQuery().processInstanceBusinessKey(businessKey).count(), 0);
        checkInternalHistoricVariablePresent(processInstanceIds);

        processInstanceIds.stream().forEach(processInstanceId -> {
            assertEquals(processEngine.getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).count(), 0);
            assertEquals(processEngine.getHistoryService().createHistoricJobLogQuery().processInstanceId(processInstanceId).count(), 0);
            assertEquals(processEngine.getHistoryService().createHistoricIncidentQuery().processInstanceId(processInstanceId).count(), 0);
        });

    }

    /**
     * Gets the process instance ids.
     *
     * @param processParentInstanceId the process parent instance id
     * @param processInstanceIds      the process instance ids
     * @return the process instance ids
     */
    @SuppressWarnings("squid:S1612")
    private static List<String> getProcessInstanceIds(final String processParentInstanceId, final List<String> processInstanceIds) {
        final List<ProcessInstance> processInstances = processEngine.getRuntimeService().createProcessInstanceQuery()
                                                               .superProcessInstanceId(processParentInstanceId).list();

        processInstanceIds.addAll(processInstances.stream().map(Execution::getId).collect(Collectors.toList()));
        for (final ProcessInstance processInstance : processInstances) {
            getProcessInstanceIds(processInstance.getId(), processInstanceIds);
        }
        return processInstanceIds;
    }

    /**
     * Checks if the only internal flow historic variable is present, indicating internal flow has started
     *
     */
    private static void checkInternalHistoricVariablePresent(List<String> processInstanceIds) {
        final int MAX_RETRIES = 10;
        int retries = 0;

        while (retries < MAX_RETRIES) {
            try {
                // Internal flows allow only one variable, rest are being dropped. Due to an async execution it's possible for the variable to not be present at the time of check, retries added.
                assertEquals(processEngine.getHistoryService().createHistoricVariableInstanceQuery().processInstanceIdIn(processInstanceIds.toArray(new String[0])).count(), 1);
                return;
            } catch (AssertionError e) {
                retries++;
                realSleepMs(500);
            }
        }
        LOGGER.error("Historic variable was not present at runtime, internal flow is not deployed.");
        throw new RuntimeException();
    }
}
