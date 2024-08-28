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

package com.ericsson.oss.services.flowautomation.interfacetest.fwk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.ericsson.oss.services.flowautomation.error.ErrorCode;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.test.fwk.FlowAutomationBaseTest;

public abstract class FlowAutomationNegativeTest extends FlowAutomationBaseTest {

    protected void checkFlowFunctionFails(final FlowAutomationNegativeTestInterface f, ErrorCode errorCode) {
        try {
            f.execute();
            fail("Expected to fail with an exception here.");
        } catch (final FlowAutomationException e) {
            assertEquals(errorCode.code(), e.getErrorReasons().get(0).getCode());
        }
    }

}
