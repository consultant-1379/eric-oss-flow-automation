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

import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.COLON;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowPackageUtil.FORWARD_SLASH;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskFormKeyResolverType.Value.SETUP_PHASE_USER_TASK_FORM_KEY_RESOLVER;

import org.camunda.bpm.engine.task.Task;

import com.ericsson.oss.services.flowautomation.resources.UserTaskFormKeyResolverType;

/**
 * The Class SetupPhaseUserTaskFormKeyResolver.
 */
@UserTaskFormKeyResolverType(SETUP_PHASE_USER_TASK_FORM_KEY_RESOLVER)
public class SetupPhaseUserTaskFormKeyResolver implements UserTaskFormKeyResolver {

    public static final String SETUP_PHASE_FORM_KEY_PREFIX = "setup";

    @Override
    public String resolveFormKey(final Task task) {
        final String formKey = task.getFormKey();
        if (formKey == null) {
            return null;
        }
        if (isFormKeyExternalFile(formKey)) {
            return formKey.replace(COLON, FORWARD_SLASH);
        } else {
            return removeSetupPrefix(formKey);
        }
    }

    @Override
    public String getFormKeyName(final Task task) {
        final String formKey = task.getFormKey();
        if (formKey == null) {
            return null;
        }
        final String keyText = isFormKeyExternalFile(formKey) ? formKey.replaceFirst("[.][^.]+$", "") : formKey;
        return removeSetupPrefix(keyText);
    }

    /**
     * Removes the prefix "setup:" from the formKey
     *
     * @param formKey the form key of usertask
     * @return the extracted string
     */
    private String removeSetupPrefix(final String formKey) {
        return formKey.substring(SETUP_PHASE_FORM_KEY_PREFIX.length() + 1);
    }
}
