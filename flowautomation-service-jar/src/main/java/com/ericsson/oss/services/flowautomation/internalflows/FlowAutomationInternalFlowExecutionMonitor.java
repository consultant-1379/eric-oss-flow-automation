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

package com.ericsson.oss.services.flowautomation.internalflows;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STOP;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowTaskExecutionListener.FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows.ERICSSON;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.api.FlowService;
import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.flow.FlowExecutionPhase;
import com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionRepository;
import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;
import com.ericsson.oss.services.utils.SimpleContextService;

/**
 * The Class FlowAutomationInternalFlowExecutionMonitor.
 */
public class FlowAutomationInternalFlowExecutionMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowAutomationInternalFlowExecutionMonitor.class);

    @Inject
    protected SimpleContextService contextService;

    @Inject
    protected FlowExecutionRepository flowExecutionRepository;

    @Inject
    private FlowExecutionPhase flowExecutionPhase;

    @Inject
    @ServiceType(JAR_TYPE)
    private FlowService flowService;

    @Inject
    @DefaultProcessEngine
    protected ProcessEngine processEngine;

    public static final String USER_ID = "UserID";

    /**
     * Handle internal flow execution.
     *
     * @param flowId
     *         the flow id
     */
    public void handleInternalFlowExecution(final String flowId) {

        LOGGER.info("Monitoring internal Flow: [{}]", flowId);
        contextService.setContextValue(USER_ID, ERICSSON);

        flowService.getFlowDefinition(flowId).getFlowVersions().forEach(flowVersion -> {
            FlowExecutionFilterBuilder flowExecutionFilterBuilder =
                    new FlowExecutionFilterBuilder().flowId(flowId).flowVersion(flowVersion.getVersion());

            List<FlowExecutionEntity> flowExecutionEntities = flowExecutionRepository.getFlowExecutions(flowExecutionFilterBuilder.build());

            if (!flowExecutionEntities.isEmpty()) {
                if (flowVersion.isActive()) {
                    LOGGER.info("Monitoring executions of active version flow: [{}]", flowId);
                    handleActiveFlowVersionExecutions(flowId, flowVersion.getVersion(), flowExecutionEntities);
                } else {
                    LOGGER.info("Deleting executions of old version of flows: [{}] with Version: [{}]", flowId, flowVersion.getVersion());
                    deleteInactiveFlowVersionExecutions(flowExecutionEntities);
                }
            }
        });
    }


    private void deleteInactiveFlowVersionExecutions(List<FlowExecutionEntity> flowExecutionEntities) {
        flowExecutionEntities.forEach(flowExecutionEntitiy -> {
            LOGGER.debug("Deleting executions of inactive versions of flow: [{}], execution name: [{}]", flowExecutionEntitiy.getFlowId(), flowExecutionEntitiy.getFlowExecutionName());
            String parentProcessInstanceId = flowExecutionRepository.getProcessInstanceId(flowExecutionEntitiy.getFlowId(), flowExecutionEntitiy.getFlowExecutionName());
            final List<String> processInstanceIds = new ArrayList<>();
            getProcessInstanceIds(parentProcessInstanceId, processInstanceIds);
            processInstanceIds.add(parentProcessInstanceId);
            deleteExecutions(parentProcessInstanceId, processInstanceIds);
        });
    }


    private void handleActiveFlowVersionExecutions(String flowId, String flowVersion, List<FlowExecutionEntity> flowExecutionEntities) {

        final List<FlowExecutionEntity> validFlowExecutions = new ArrayList<>();

        flowExecutionEntities.forEach(flowExecutionEntity -> {

            final String parentProcessInstanceId = flowExecutionEntity.getProcessInstanceId();

            final List<String> processInstanceIds = new ArrayList<>();
            getProcessInstanceIds(parentProcessInstanceId, processInstanceIds);
            processInstanceIds.add(parentProcessInstanceId);

            final List<Incident> incidents = processInstanceIds.stream()
                    .flatMap(processInstanceId -> processEngine.getRuntimeService().createIncidentQuery().processInstanceId(processInstanceId).list().stream())
                    .collect(Collectors.toList());

            if (!incidents.isEmpty() && flowExecutionPhase.isExecutionStoppable(parentProcessInstanceId)) {
                LOGGER.debug("Incidents found on a executing flow instance: [{}] hence deleting.", flowExecutionEntity.getFlowExecutionName());
                deleteExecutions(parentProcessInstanceId, processInstanceIds);
            } else {
                if (flowExecutionEntity.getFlowExecutionName().contains(flowVersion)) {
                    validFlowExecutions.add(flowExecutionEntity);
                } else {
                    LOGGER.debug("Found a valid execution with old/invalid name: [{}] hence stopping graciously.", flowExecutionEntity.getFlowExecutionName());
                    stopExecution(flowExecutionEntity);
                }

            }
        });

        handleValidFlowExecutions(validFlowExecutions, flowId, flowVersion);
    }

    private void deleteExecutions(String parentProcessInstanceId, List<String> processInstanceIds) {
        //suspend the instance
        processInstanceIds
                .forEach(processInstanceId -> processEngine.getRuntimeService().suspendProcessInstanceById(processInstanceId));
        processEngine.getRuntimeService().deleteProcessInstance(parentProcessInstanceId, "Delete User Action", false, true, true);
        //delete the instance from history
        processInstanceIds
                .forEach(processInstanceId -> processEngine.getHistoryService().deleteHistoricProcessInstance(processInstanceId));
        //delete the instance from fa_execution_table
        flowExecutionRepository.deleteFlowExecution(parentProcessInstanceId);
    }

    private void handleValidFlowExecutions(List<FlowExecutionEntity> validFlowExecutions, String flowId, String flowVersion) {
        if (validFlowExecutions.isEmpty()) {
            final String executionName = FlowAutomationInternalFlows.getFlowExecutionName(flowId, flowVersion) + DateTimeUtil.getTimeStamp();
            LOGGER.debug("Starting new instance with name: [{}]", executionName);
            contextService.setContextValue(USER_ID, ERICSSON);
            flowService.executeFlow(flowId, executionName);
        } else if (validFlowExecutions.size() > 1) {
            Date latestDate = null;
            FlowExecutionEntity finalExecution = null;
            for (FlowExecutionEntity flowExecution : validFlowExecutions) {
                final HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(flowExecution.getProcessInstanceId()).singleResult();
                final Date instanceStartTime = historicProcessInstance.getStartTime();
                if (null == latestDate) {
                    latestDate = instanceStartTime;
                    finalExecution = flowExecution;
                } else if (instanceStartTime.compareTo(latestDate) > 0) {
                    latestDate = instanceStartTime;
                    stopExecution(finalExecution);
                    finalExecution = flowExecution;
                } else {
                    LOGGER.debug("Stopping older execution because another latest valid execution found: [{}]", flowExecution.getFlowExecutionName());
                    stopExecution(flowExecution);
                }
            }
        }
    }

    private void stopExecution(FlowExecutionEntity flowExecution) {
        if (flowExecutionPhase.isExecutionStoppable(flowExecution.getProcessInstanceId())) {
            try {
                processEngine.getRuntimeService().setVariable(flowExecution.getProcessInstanceId(), FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME, STOP.name());
                LOGGER.info("Requested to stop the flow id: {}, flow execution: {}", flowExecution.getFlowId(), flowExecution.getFlowExecutionName());
            } catch (final ProcessEngineException e) {
                throw new FlowAutomationException("Failed to stop the flow execution: " + flowExecution.getFlowId() + "." + flowExecution.getFlowExecutionName()+ ": " + e.getMessage(), e);
            }
        }
    }


    /**
     * Gets the process instance ids.
     *
     * @param processParentInstanceId
     *         the process parent instance id
     * @param processInstanceIds
     *         the process instance ids
     * @return the process instance ids
     */
    @SuppressWarnings("squid:S1612")
    private List<String> getProcessInstanceIds(final String processParentInstanceId, final List<String> processInstanceIds) {
        final List<ProcessInstance> processInstances = processEngine.getRuntimeService().createProcessInstanceQuery()
                .superProcessInstanceId(processParentInstanceId).list();

        processInstanceIds.addAll(processInstances.stream().map(Execution::getId).collect(Collectors.toList()));
        for (final ProcessInstance processInstance : processInstances) {
            getProcessInstanceIds(processInstance.getId(), processInstanceIds);
        }
        return processInstanceIds;
    }
}
