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

package com.ericsson.oss.services.flowautomation.test.fwk.rest;

/**
 * Utility class to return the flowautomation users.
 */
public class FlowAutomationUsers {

    public static final String USER_HEADER = "UserID";

    /**
     * Avoids instantiation.
     */
    private FlowAutomationUsers() {
    }

    /**
     * Returns the flow automation admin user.
     */
    @SuppressWarnings("squid:S3400")
    public static String admin() {
        return "faadmin";
    }
}
