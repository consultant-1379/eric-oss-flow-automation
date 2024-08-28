/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.services.flowautomation.brtest.fwk.jse;

import com.ericsson.oss.services.flowautomation.brapi.RestoreHandler;
import com.ericsson.oss.services.flowautomation.test.fwk.jse.JseTestContext;
import com.ericsson.oss.services.flowautomation.test.fwk.jse.TestServiceTypeJarQualifier;

public class InternalTestContext{
    private static InternalTestContext internalTestContext;
    private final RestoreHandler restoreHandlerClient;

    private InternalTestContext() {
        JseTestContext jseTestContext = JseTestContext.getInstance();
        restoreHandlerClient = jseTestContext.getWeldContainer().select(RestoreHandler.class, new TestServiceTypeJarQualifier()).get();
    }

    public static InternalTestContext getInstance() {
        if (internalTestContext == null) {
            internalTestContext = new InternalTestContext();
        }

        return internalTestContext;
    }

    public RestoreHandler getRestoreHandlerClient() {
        return restoreHandlerClient;
    }
}
