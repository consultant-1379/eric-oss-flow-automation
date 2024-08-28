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

package com.ericsson.oss.services.flowautomation.bragent.agent;

import static org.junit.Assert.assertNotNull;

import com.ericsson.adp.mgmt.bro.api.agent.Agent;
import com.ericsson.oss.services.flowautomation.bragent.test.BroGrpcStub;
import com.ericsson.oss.services.flowautomation.bragent.util.PropertiesHelper;
import io.grpc.BindableService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestAgentFactoryIntegrationTest extends BroGrpcStub {

    @Before
    public void setUp() throws Exception {
        stopGrpcServer();
    }

    @Test
    public void createTestAgent_orchestratorInformationAsProperties_createsTestAgent() throws Exception {
        PropertiesHelper.loadProperties("src/test/resources/application.properties");
        final Agent agent = FAAgentFactory.createAgent();
        assertNotNull(agent);
    }

    @Override
    protected List<BindableService> getServices() {
        return new ArrayList<>();
    }

    @After
    public void after() throws Exception {
        stopGrpcServer();
    }

}
