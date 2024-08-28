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

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.After;
import org.junit.Before;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.script.ScriptRunner;

public abstract class FlowAutomationScriptBaseTest extends FlowAutomationBaseTest {

    private String dummyFlowPackage = "scripttaskTestDummyFlow";
    private String dummyFlowId = "com.ericsson.oss.fa.flows.scripttaskTestDummyFlow";

    protected DelegateExecution delegateExecution;
    protected FlowExecution flowExecution;

    /**
     * Initial setup for running a flow automation script  by importing a flow and starting its execution
     */
    @Before
    public void before() {
        delegateExecution = new DelegateExecutionFake();
        final FlowDefinition dummyFlowDefinition = importFlow(dummyFlowId, getFlowPackageBytes(dummyFlowPackage));
        flowExecution = startFlowExecution(dummyFlowDefinition, "my-execution-name");
        checkExecutionState(flowExecution, "EXECUTING");
        ((DelegateExecutionFake)delegateExecution).withProcessBusinessKey(flowExecution.getProcessInstanceBusinessKey());
        ((DelegateExecutionFake)delegateExecution).withBusinessKey(flowExecution.getProcessInstanceBusinessKey());
    }

    /**
     * Cleanup the data
     */
    @After
    public void after() {
        removeFlow(dummyFlowId);
    }


    /**
     * Runs a groovy script at the given path during flow execution
     * @param execution
     * @param scriptPath
     * @return
     */
    protected Object runFlowScript(final DelegateExecution execution, final String scriptPath) {
        return new ScriptRunner().runFlowScript(execution, scriptPath);
    }
}
