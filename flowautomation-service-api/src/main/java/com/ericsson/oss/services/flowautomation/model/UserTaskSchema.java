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

import lombok.Getter;
import lombok.ToString;

/**
 * The Class UserTaskSchema.
 */
@Getter
@ToString
public class UserTaskSchema extends UserTask {

    /** The user task schema. */
    private final String schema;

    /**
     * Instantiates a new user task schema.
     *
     * @param id
     *            the id
     * @param name
     *            the name
     * @param status
     *            the status
     * @param schema
     *            the schema
     */
    public UserTaskSchema(final String id, final String name, final String processDefinitionId, final String taskDefinitionId, final UserTaskStatus status, final String schema) {
        super(id, name, processDefinitionId, taskDefinitionId, status);
        this.schema = schema;
    }

    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
