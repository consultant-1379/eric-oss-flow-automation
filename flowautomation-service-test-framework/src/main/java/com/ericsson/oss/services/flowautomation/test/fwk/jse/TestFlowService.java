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

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;
import static com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType.Value.TEST_JAR_TYPE;

import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.api.FlowService;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionInput;
import com.ericsson.oss.services.flowautomation.model.FlowSource;
import com.ericsson.oss.services.flowautomation.model.FlowVersionProcessDetails;
import com.ericsson.oss.services.flowautomation.test.fwk.TestServiceType;

@TestServiceType(TEST_JAR_TYPE)
public class TestFlowService implements FlowService {

    @Inject
    @ServiceType(JAR_TYPE)
    private FlowService flowService;

    @Inject
    private TransactionalOperationExecutor transactionalOperationExecutor;

    @Override
    public List<FlowDefinition> getFlows() {
        return transactionalOperationExecutor.execute(() -> flowService.getFlows());
    }

    @Override
    public FlowDefinition importFlow(final byte[] flowPackage) {
        return transactionalOperationExecutor.execute(() -> flowService.importFlow(flowPackage));
    }

    @Override
    public FlowDefinition importFlow(final byte[] flowPackage, final FlowSource flowSource) {
        return transactionalOperationExecutor.execute(() -> flowService.importFlow(flowPackage, flowSource));
    }

    @Override
    public void executeFlow(final String flowId, final String flowExecutionName) {
        transactionalOperationExecutor.execute(() -> {
            flowService.executeFlow(flowId, flowExecutionName);
            return null;
        });
    }

    @Override
    public void executeFlow(final String flowId, final FlowExecutionInput flowExecutionInput) {
        transactionalOperationExecutor.execute(() -> {
            flowService.executeFlow(flowId, flowExecutionInput);
            return null;
        });
    }

    @Override
    public void activateFlowVersion(final String flowId, final String flowVersion, final boolean activate) {
        transactionalOperationExecutor.execute(() -> {
            flowService.activateFlowVersion(flowId, flowVersion, activate);
            return null;
        });
    }

    @Override
    public void enableFlow(final String flowId, final boolean enable) {
        transactionalOperationExecutor.execute(() -> {
            flowService.enableFlow(flowId, enable);
            return null;
        });
    }

    @Override
    public FlowDefinition getFlowDefinition(final String flowId) {
        return transactionalOperationExecutor.execute(() -> flowService.getFlowDefinition(flowId));
    }

    @Override
    public void deleteFlow(final String flowId, final boolean force) {
        transactionalOperationExecutor.execute(() -> {
            flowService.deleteFlow(flowId, force);
            return null;
        });
    }

    @Override
    public FlowVersionProcessDetails getProcessDetailsForFlowVersion(final String flowId, final String flowVersion) {
        return transactionalOperationExecutor.execute(() -> flowService.getProcessDetailsForFlowVersion(flowId, flowVersion));
    }

    @Override
    public String getFlowInputSchema(String flowId, String flowVersion) {
        return transactionalOperationExecutor.execute(() -> flowService.getFlowInputSchema(flowId, flowVersion));
    }
}
