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

package com.ericsson.oss.services.flowautomation.test.fwk.jse;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.api.FlowService;

public class JseTestContext {
    private static JseTestContext testContext;

    private Weld weld;
    private WeldContainer weldContainer;
    private FlowService flowServiceClient;
    private FlowExecutionService flowExecutionServiceClient;
    private ProcessEngineContainer processEngineContainer;

    private JseTestContext() {
        weld = new Weld();
        weldContainer = weld.initialize();
        flowServiceClient = weldContainer.select(FlowService.class, new TestServiceTypeJarQualifier()).get();
        flowExecutionServiceClient = weldContainer.select(FlowExecutionService.class, new TestServiceTypeJarQualifier()).get();
        processEngineContainer = weldContainer.select(ProcessEngineContainer.class).get();
    }

    public static JseTestContext getInstance() {
        if (testContext == null) {
            testContext = new JseTestContext();
        }

        return testContext;
    }

    public WeldContainer getWeldContainer() {
        return weldContainer;
    }

    public FlowService getFlowServiceClient() {
        return flowServiceClient;
    }

    public FlowExecutionService getFlowExecutionServiceClient() {
        return flowExecutionServiceClient;
    }

    public ProcessEngineContainer getProcessEngineContainer() {
        return processEngineContainer;
    }


}
