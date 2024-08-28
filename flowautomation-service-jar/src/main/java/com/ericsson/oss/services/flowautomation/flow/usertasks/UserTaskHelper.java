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

package com.ericsson.oss.services.flowautomation.flow.usertasks;

import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.EMPTY;
import static com.ericsson.oss.services.flowautomation.model.UserTask.FA_NAME_EXTRA;
import static com.ericsson.oss.services.flowautomation.model.UserTaskStatus.COMPLETED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.flow.listeners.UserTaskExecutionStartListener;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowAutomationBpmnEngineUtil;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionRepository;
import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;

/**
 * The Class UserTaskHelper.
 */
public class UserTaskHelper {

    public static final String FAINTERNAL_TASK_KEY = "FAInternal_TASK_KEY";

    /** The process engine. */
    @Inject
    @DefaultProcessEngine
    protected ProcessEngine processEngine;

    /** The flow execution repository dao */
    @Inject
    protected FlowExecutionRepository flowExecutionRepository;

    @Inject
    protected FlowAutomationBpmnEngineUtil flowAutomationBpmnEngineUtil;

    /**
     * Gets the key for a completed task
     *
     * @param historicTaskInstance the historicTaskInstance
     * @return the key
     */
    private String getKeyForCompletedTask(final HistoricTaskInstance historicTaskInstance) {
        final HistoricVariableInstance historicVariableInstance = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery().taskIdIn(historicTaskInstance.getId())
                .variableName(FAINTERNAL_TASK_KEY).singleResult();
        return (historicVariableInstance != null) ? (String) historicVariableInstance.getValue() : EMPTY;
    }

    /**
     * Gets the nameExtra for a completed task
     *
     * @param historicTaskInstance the historicTaskInstance
     * @return the nameExtra
     */
    public String getNameExtraForCompletedTask(final HistoricTaskInstance historicTaskInstance) {
        final HistoricVariableInstance historicVariableInstance = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery().taskIdIn(historicTaskInstance.getId())
                .variableName(FA_NAME_EXTRA).singleResult();
        return (historicVariableInstance != null) ? (String) historicVariableInstance.getValue() : EMPTY;
    }

    /**
     * Gets the nameExtra map
     *
     * @param flowExecutionEntity the flowExecutionEntity
     * @return the map containing nameExtra items
     */
    public Map<String, String> getNameExtraMap(final FlowExecutionEntity flowExecutionEntity) {
        return processEngine.getHistoryService().createHistoricTaskInstanceQuery()
                .processInstanceBusinessKey(flowExecutionEntity.getProcessInstanceBusinessKey()).finished()
                .orderByHistoricTaskInstanceEndTime().asc().list().stream()
                .filter(completedTask -> !getNameExtraForCompletedTask(completedTask).isEmpty())
                .collect(Collectors.toMap(this::getKeyForCompletedTask, this::getNameExtraForCompletedTask, (entry1, entry2) -> entry1));
    }

    /**
     * Returns the usertask back target by,
     * 1) checking if the userttask is in wrapper flow and then the backtarget is none.
     * 2) checks if the usertask has the faBackTarget configured and use that value as the back target
     * 3) querying the historic completed usertask and getting the most recent completed usertask.
     *
     * @param processInstanceId the process instance id
     * @param task              the task
     * @return the user task back target
     */
    public Optional<String> getUserTaskBackTarget(final String processInstanceId, final Task task) {
        if (Objects.equals(processInstanceId, task.getProcessInstanceId())) { // FAInternal Usertasks in wrapper flow
            return Optional.empty();
        }
        final VariableInstance variableInstance = processEngine.getRuntimeService().createVariableInstanceQuery()
                .processInstanceIdIn(task.getProcessInstanceId()).executionIdIn(task.getExecutionId()).variableName(UserTaskExecutionStartListener.BACK_TARGET).singleResult();
        if (variableInstance != null) { //Back target is configured in the flow using the extension property
            return Optional.ofNullable(String.valueOf(variableInstance.getValue()));
        }

        return getPreviousCompletedUserTask(task);
    }

    /**
     * Returns the previous completed usertask for a given usertask.
     *
     * @param task the current active usertask
     * @return the taskDefinitionKey if there is a previous usertask
     */
    public Optional<String> getPreviousCompletedUserTask(Task task) {
        //Return all the completed user tasks ordered by the endTime
        final List<HistoricTaskInstance> completedTasks = processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery().processInstanceId(task.getProcessInstanceId()).finished()
                .taskDeleteReason("completed").orderByHistoricTaskInstanceEndTime().asc().list();

        // Every time we execute a user task and click on continue, a new entry will be persisted in the database.
        // If we just add the entries to a LinkedHashSet, only the first entry for the given user task will be added
        // and we are not going to keep the latest tasks execution order. To keep the latest order, every time we go
        // back and complete a previous task we need to clean up the items starting from the current task so that we
        // can create the new execution order.
        List<String> taskDefinitionKeys = new ArrayList<>();
        completedTasks.forEach(completedTask -> {
            int index = taskDefinitionKeys.indexOf(completedTask.getTaskDefinitionKey());
            if (index >= 0) {
                taskDefinitionKeys.subList(index, taskDefinitionKeys.size()).clear();
            }
            taskDefinitionKeys.add(completedTask.getTaskDefinitionKey());
        });

        String previousTaskKey = null;
        for (final String key : taskDefinitionKeys) {
            if (Objects.equals(key, task.getTaskDefinitionKey())) {
                break;
            }
            previousTaskKey = key;
        }
        return Optional.ofNullable(previousTaskKey);
    }

    /**
     * Gets previously submitted user task data if available otherwise return an empty map.
     *
     * @param task the task
     * @return the previously submitted user task data
     */
    public Map<String, Object> getPreviouslySubmittedUserTaskData(final Task task) {
        final VariableInstance variableInstance = processEngine.getRuntimeService().createVariableInstanceQuery()
                .variableName(task.getTaskDefinitionKey()).processInstanceIdIn(task.getProcessInstanceId()).singleResult();

        return variableInstance == null ? Collections.emptyMap() : (Map<String, Object>) variableInstance.getValue();
    }

    /**
     * Checks if the flow execution has any completed usertask matching the predicate.
     * @param flowExecutionEntity the flow execution to check
     * @return true if there is any completed user task for the flow execution
     */
    public boolean isExecutionWithOtherCompletedUserTask(FlowExecutionEntity flowExecutionEntity, final Predicate<HistoricTaskInstance> taskPredicate) {
        final FlowBusinessKey flowBusinessKey = new FlowBusinessKey(flowExecutionEntity.getFlowId(), flowExecutionEntity.getFlowExecutionName(), flowExecutionEntity.getId().toString());
        final String businessKey = flowBusinessKey.getCamundaBusinessKey();
        return processEngine.getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .finished()
                .taskDeleteReason(COMPLETED.getValue())
                .list().stream()
                .anyMatch(taskPredicate);
    }
}
