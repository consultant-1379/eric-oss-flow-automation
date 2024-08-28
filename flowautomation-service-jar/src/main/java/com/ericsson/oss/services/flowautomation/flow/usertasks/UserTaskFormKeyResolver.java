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

import org.camunda.bpm.engine.task.Task;

/**
 * The Class UserTaskFormKeyResolver.
 */
public interface UserTaskFormKeyResolver {

    /**
     * Resolve form key.
     *
     * @param task the task
     * @return the string
     */
    String resolveFormKey(final Task task);

    /**
     * Checks if the formKey ends with an json or ftl extension
     *
     * @param formKey the form key of usertask
     * @return boolean result
     */
    default boolean isFormKeyExternalFile(final String formKey) {
        return formKey.endsWith(".json") || formKey.endsWith(".ftl");
    }

    /**
     * @param task the task
     */
    default String getFormKeyName(final Task task) {
        return "";
    }
}
