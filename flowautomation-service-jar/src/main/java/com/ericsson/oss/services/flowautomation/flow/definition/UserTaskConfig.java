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

package com.ericsson.oss.services.flowautomation.flow.definition;

import static java.lang.Boolean.TRUE;

/**
 * The type User task config.
 */
public class UserTaskConfig {

    private Boolean backEnabled;

    /**
     * Is back enabled boolean.
     *
     * @return the boolean
     */
    public boolean isBackEnabled() {
        return TRUE.equals(backEnabled);
    }

    /**
     * Sets back enabled.
     *
     * @param backEnabled the back enabled
     */
    public void setBackEnabled(final Boolean backEnabled) {
        this.backEnabled = backEnabled;
    }
}
