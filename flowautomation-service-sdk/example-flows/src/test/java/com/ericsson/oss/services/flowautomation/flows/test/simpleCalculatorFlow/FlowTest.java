 /*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

package com.ericsson.oss.services.flowautomation.flows.test.simpleCalculatorFlow;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.getFlowPackageBytes;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskCheckBuilder;
import com.ericsson.oss.services.flowautomation.test.fwk.UsertaskInputBuilder;

public class FlowTest extends FlowAutomationBaseTest {

    String flowPackage = "simpleCalculatorFlow";
    String flowId = "com.ericsson.oss.ae.fa.flows.example.simpleCalculatorFlow";
    FlowDefinition flowDefinition;

    @Before
    public void before() {
        setOutputToConsole(true);
        flowDefinition = importFlow(flowId, getFlowPackageBytes(flowPackage));
    }

    @After
    public void after() {
        removeFlow(flowId);
    }

    @Test
    public void testFlowExecution() {
        // Start Flow execution
        String executionName = createUniqueInstanceName(flowId);
        FlowExecution flowExecution = startFlowExecution(flowDefinition, executionName);

        checkExecutionIsActive(flowExecution);

        // ----------------- Test 'sum' -----------------
        // Enter values and operation
        completeUsertask(flowExecution, "Define numbers and math operation",
                new UsertaskInputBuilder()
                        .input("Value 1", 5)
                        .input("Value 2", 10)
                        .input("Choose Math operation > Arithmetical operations > +", true));

        // Check results
        checkUsertask(flowExecution, "Result",
                new UsertaskCheckBuilder()
                        .check("Result > Calculation result", 15));

        // Continue
        completeUsertaskNoInput(flowExecution, "Result");

        // Request another calculation
        completeUsertask(flowExecution, "Another Calculation ?",
                new UsertaskInputBuilder()
                        .input("Perform another calculation ?", true));

        // ----------------- Test 'multiply' -----------------
        completeUsertask(flowExecution, "Define numbers and math operation",
                new UsertaskInputBuilder()
                        .input("Value 1", 25)
                        .input("Value 2", 5)
                        .input("Choose Math operation > Arithmetical operations > *", true));

        checkUsertask(flowExecution, "Result",
                new UsertaskCheckBuilder()
                        .check("Result > Calculation result", 125));

        completeUsertaskNoInput(flowExecution, "Result");

        completeUsertask(flowExecution, "Another Calculation ?",
                new UsertaskInputBuilder()
                        .input("Perform another calculation ?", true));

        // ----------------- Test 'subtract' -----------------
        completeUsertask(flowExecution, "Define numbers and math operation",
                new UsertaskInputBuilder()
                        .input("Value 1", 13)
                        .input("Value 2", 10)
                        .input("Choose Math operation > Arithmetical operations > -", true));

        checkUsertask(flowExecution, "Result",
                new UsertaskCheckBuilder()
                        .check("Result > Calculation result", 3));

        completeUsertaskNoInput(flowExecution, "Result");

        // Request no more calculations
        completeUsertask(flowExecution, "Another Calculation ?",
                new UsertaskInputBuilder()
                        .input("Perform another calculation ?", false));

        // Wait for execution to finish
        checkExecutionExecuted(flowExecution);
    }

}
