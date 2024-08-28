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

package com.ericsson.oss.services.flowautomation.rest.functest;

import com.ericsson.oss.services.flowautomation.flows.test.DslHelloWorldHighVersionTest;
import com.ericsson.oss.services.flowautomation.flows.test.DslHelloWorldTest;
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;

/**
 * Test cases for World flow version with three digits in the patch number using Flow Automation Service API via REST using a simple DSL.
 */
@RunAsClient
@RunWith(Arquillian.class)
public class RestDslHelloWorldHighVersionTest extends DslHelloWorldHighVersionTest {

    @Override
    protected TestClientType selectClientType() {
        return TestClientType.REST;
    }

}
