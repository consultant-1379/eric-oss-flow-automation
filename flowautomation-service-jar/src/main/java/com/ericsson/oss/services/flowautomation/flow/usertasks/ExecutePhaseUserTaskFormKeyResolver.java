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

import static com.ericsson.oss.services.flowautomation.flow.utils.FlowPackageUtil.FORWARD_SLASH;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskFormKeyResolverType.Value.EXECUTE_PHASE_USER_TASK_FORM_KEY_RESOLVER;

import org.camunda.bpm.engine.task.Task;

import com.ericsson.oss.services.flowautomation.resources.UserTaskFormKeyResolverType;

/**
 * The Class ExecutePhaseUserTaskFormKeyResolver.
 */
@UserTaskFormKeyResolverType(EXECUTE_PHASE_USER_TASK_FORM_KEY_RESOLVER)
public class ExecutePhaseUserTaskFormKeyResolver implements UserTaskFormKeyResolver {

    private static final String COLON = ":";

    @Override
    public String resolveFormKey(final Task task) {
        final String formKey = task.getFormKey();
        return formKey.replace(COLON, FORWARD_SLASH);
    }
}
