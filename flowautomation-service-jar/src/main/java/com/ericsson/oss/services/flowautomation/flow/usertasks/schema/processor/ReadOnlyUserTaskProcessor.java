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

package com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor;

import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FA_READ_ONLY_USER_TASK_MAP;

import java.util.Map;

import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;

/**
 * The type Read only user task processor.
 */
public class ReadOnlyUserTaskProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadOnlyUserTaskProcessor.class);

    /**
     * The process engine.
     */
    @Inject
    @DefaultProcessEngine
    private ProcessEngine processEngine;

    /**
     * Instantiates a new Read only user task processor.
     */
    public ReadOnlyUserTaskProcessor() {
        //
    }

    /**
     * Process read only user task map.
     *
     * @param userTaskVariables the user task variables
     * @param task              the task
     * @return the map
     */
    public Map<String, Object> processReadOnlyUserTask(final Map<String, Object> userTaskVariables, final Task task) {

        final Map<String, Object> readOnlyUserTaskVariables = (Map<String, Object>) processEngine.getTaskService().getVariableLocal(task.getId(), FA_READ_ONLY_USER_TASK_MAP);
        if (readOnlyUserTaskVariables != null) {
            LOGGER.debug("Processing read only user task. UserTaskVariables: {}, ReadOnlyUserTaskVariables: {}",userTaskVariables,readOnlyUserTaskVariables);
            processReadOnlyUserTask(userTaskVariables, readOnlyUserTaskVariables);
            LOGGER.debug("Processed user task variables: {}", userTaskVariables);
        }
        return userTaskVariables;
    }

    private void processReadOnlyUserTask(final Map<String, Object> userTaskVariables, final Map<String, Object> readOnlyUserTaskVariables) {
        readOnlyUserTaskVariables.forEach(
                (key, value) ->
                    userTaskVariables.merge(key, value, (userTaskVariableValue, readOnlyUserTaskVariableValue) -> {
                        if (userTaskVariableValue instanceof Map && readOnlyUserTaskVariableValue instanceof Map) {
                            processReadOnlyUserTask((Map<String, Object>) userTaskVariableValue, (Map<String, Object>) readOnlyUserTaskVariableValue);
                                return userTaskVariableValue;
                        }
                        return readOnlyUserTaskVariableValue;
                    })
        );
    }
}
