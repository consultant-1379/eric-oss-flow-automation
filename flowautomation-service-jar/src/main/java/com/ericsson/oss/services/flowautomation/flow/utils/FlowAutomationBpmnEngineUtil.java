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

package com.ericsson.oss.services.flowautomation.flow.utils;

import java.util.List;

import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;

/**
 * Flow Automation Bpmn Engine Utilities.
 */
public class FlowAutomationBpmnEngineUtil {

    /** The process engine. */
    @Inject
    @DefaultProcessEngine
    protected ProcessEngine processEngine;

    /**
     * Gets the parent process instance id.
     *
     * @param processInstanceId the process instance id
     * @return the parent process instance id
     */
    public final String getParentProcessInstanceId(String processInstanceId) {
        final List<ProcessInstance> processInstances = processEngine.getRuntimeService().createProcessInstanceQuery()
                .subProcessInstanceId(processInstanceId).list();
        if (processInstances.isEmpty()) {
            return processInstanceId;
        } else {
            processInstanceId = processInstances.get(0).getId();
            return getParentProcessInstanceId(processInstanceId);
        }
    }
}
