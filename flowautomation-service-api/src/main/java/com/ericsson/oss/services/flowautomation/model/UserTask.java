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

package com.ericsson.oss.services.flowautomation.model;

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * The Class UserTask.
 */
@Getter
@EqualsAndHashCode
public class UserTask {

    public static final String FA_NAME_EXTRA = "FA_NAME_EXTRA";

    private final String id;

    private final String name;

    private final String nameExtra;

    private final String processDefinitionId;

    private final String taskDefinitionId;

    private final UserTaskStatus status;

    @JsonIgnore
    private final Date endTime;

    /**
     * Instantiates a new user task.
     *
     * @param id               the id
     * @param taskDefinitionId the task definition id
     * @param status           the status
     */
    public UserTask(final String id, final String name, final String processDefinitionId, final String taskDefinitionId, final UserTaskStatus status) {
        this(id, name, null, processDefinitionId, taskDefinitionId, status);
    }

    public UserTask(final String id, final String name, final String nameExtra, final String processDefinitionId, final String taskDefinitionId, final UserTaskStatus status) {
        this(id, name, nameExtra, processDefinitionId, taskDefinitionId, status, null);
    }

    /**
     * Instantiates a new user task.
     *
     * @param id               the id
     * @param nameExtra        the nameExtra
     * @param taskDefinitionId the task definition id
     * @param status           the status
     */
    public UserTask(final String id, final String name, final String nameExtra, final String processDefinitionId, final String taskDefinitionId, final UserTaskStatus status, final Date endTime) {
        this.id = id;
        this.name = name;
        this.nameExtra = nameExtra;
        this.processDefinitionId = processDefinitionId;
        this.taskDefinitionId = taskDefinitionId;
        this.status = status;
        this.endTime = Objects.nonNull(endTime) ? new Date(endTime.getTime()) : null;
    }
   /**
    * Gets the status.
    *
    * @return the status
    */
   public String getStatus() {
       return status.getValue();
    }
}
