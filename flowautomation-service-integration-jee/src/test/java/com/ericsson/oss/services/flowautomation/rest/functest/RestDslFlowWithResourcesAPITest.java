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

package com.ericsson.oss.services.flowautomation.rest.functest;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;

import com.ericsson.oss.services.flowautomation.flows.test.DslFlowWithResourcesAPITest;
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;

/**
 * REST Test cases for flow using the flow resource API.
 */
@RunAsClient
@RunWith(Arquillian.class)
public class RestDslFlowWithResourcesAPITest extends DslFlowWithResourcesAPITest {

    @Override
    protected TestClientType selectClientType() {
        return TestClientType.REST;
    }

}

