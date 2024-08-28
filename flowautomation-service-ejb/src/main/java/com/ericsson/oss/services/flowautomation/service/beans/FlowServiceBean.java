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

package com.ericsson.oss.services.flowautomation.service.beans;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.EJB_TYPE;
import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;

import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.api.FlowService;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionInput;
import com.ericsson.oss.services.flowautomation.model.FlowSource;
import com.ericsson.oss.services.flowautomation.model.FlowVersionProcessDetails;

/**
 * The {@code FlowServiceBean} is stateless session bean and implements interface {@code Flow}}.
 */
@Stateless
@ServiceType(EJB_TYPE)
public class FlowServiceBean implements FlowService {

    /** The flow service */
    @Inject
    @ServiceType(JAR_TYPE)
    private FlowService flowService;

    @Inject
    private Event<FlowDefinition> importFlowEvent;

    /**
     * Default constructor.
     */
    public FlowServiceBean() {
        //
    }

    @Override
    public FlowDefinition importFlow(final byte[] flowPackage) {
        final FlowDefinition flowDefinition = flowService.importFlow(flowPackage);
        importFlowEvent.fire(flowDefinition);
        return flowDefinition;
    }

    @Override
    public FlowDefinition importFlow(final byte[] flowPackage, final FlowSource flowSource) {
        final FlowDefinition flowDefinition = flowService.importFlow(flowPackage, flowSource);
        importFlowEvent.fire(flowDefinition);
        return flowDefinition;
    }

    @Override
    public void activateFlowVersion(final String flowId, final String flowVersion, final boolean activate) {
        flowService.activateFlowVersion(flowId, flowVersion, activate);
    }

    @Override
    public void enableFlow(final String flowId, final boolean enable) {
        flowService.enableFlow(flowId, enable);
    }

    @Override
    public List<FlowDefinition> getFlows() {
        return flowService.getFlows();
    }

    @Override
    public void executeFlow(final String flowId, final String flowExecutionName) {
        flowService.executeFlow(flowId, flowExecutionName);
    }

    @Override
    public void executeFlow(final String flowId, final FlowExecutionInput flowExecutionInput) {
        flowService.executeFlow(flowId, flowExecutionInput);
    }

    @Override
    public FlowDefinition getFlowDefinition(final String flowId) {
        return flowService.getFlowDefinition(flowId);
    }

    @Override
    public void deleteFlow(final String flowId, final boolean force) {
        flowService.deleteFlow(flowId, force);
    }

    @Override
    public FlowVersionProcessDetails getProcessDetailsForFlowVersion(String flowId, String flowVersion) {
        return flowService.getProcessDetailsForFlowVersion(flowId, flowVersion);
    }

    @Override
    public String getFlowInputSchema(final String flowId, final String flowVersion) {
        return flowService.getFlowInputSchema(flowId, flowVersion);
    }
}