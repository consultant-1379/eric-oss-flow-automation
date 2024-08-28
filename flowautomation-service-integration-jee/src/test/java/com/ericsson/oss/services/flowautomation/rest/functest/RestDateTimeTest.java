/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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

package com.ericsson.oss.services.flowautomation.rest.functest;

import com.ericsson.oss.services.flowautomation.flows.test.DateTimeTest;
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;

public class RestDateTimeTest extends DateTimeTest {
    @Override
    protected TestClientType selectClientType() {
        return TestClientType.REST;
    }

}
