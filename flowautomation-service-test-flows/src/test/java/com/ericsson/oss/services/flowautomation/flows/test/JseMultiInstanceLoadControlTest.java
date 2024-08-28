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

package com.ericsson.oss.services.flowautomation.flows.test;

import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;

/**
 * File handling flow test cases
 */
public class JseMultiInstanceLoadControlTest extends MultiInstanceLoadControlTest {

    @Override
    protected TestClientType selectClientType() {
        return TestClientType.JSE;
    }

}
