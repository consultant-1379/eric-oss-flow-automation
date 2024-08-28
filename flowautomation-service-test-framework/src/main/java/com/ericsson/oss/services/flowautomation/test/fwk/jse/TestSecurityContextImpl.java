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

package com.ericsson.oss.services.flowautomation.test.fwk.jse;

import com.ericsson.oss.services.flowautomation.flowapi.internal.SecurityContext;

public class TestSecurityContextImpl implements SecurityContext {

    @Override
    public String getPrincipal(final long executionId) {
        return "administrator";
    }
}
