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

package com.ericsson.oss.services.flowautomation.bragent.agent.behavior;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.ericsson.adp.mgmt.action.Action;
import com.ericsson.adp.mgmt.bro.api.agent.ActionInformation;
import com.ericsson.adp.mgmt.bro.api.agent.Agent;
import com.ericsson.adp.mgmt.bro.api.agent.BackupExecutionActions;
import com.ericsson.adp.mgmt.bro.api.agent.BackupPreparationActions;
import com.ericsson.adp.mgmt.bro.api.agent.CancelActions;
import com.ericsson.adp.mgmt.bro.api.agent.PostBackupActions;
import com.ericsson.adp.mgmt.bro.api.registration.RegistrationInformation;
import com.ericsson.oss.services.flowautomation.bragent.agent.FAAgentFactory;
import com.ericsson.oss.services.flowautomation.bragent.test.RestoreExecutionActionsStub;
import com.ericsson.oss.services.flowautomation.bragent.test.RestorePostActionsStub;
import com.ericsson.oss.services.flowautomation.bragent.test.RestorePreparationActionsStub;
import com.ericsson.oss.services.flowautomation.bragent.util.BackupType;
import com.ericsson.oss.services.flowautomation.bragent.util.PropertiesHelper;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class FAAgentBehaviorTest {

    private static final Logger log = LoggerFactory.getLogger(FAAgentBehaviorTest.class);
    WireMockServer wireMockServer;

    private FAAgentBehavior agentBehavior;
    private Agent agent;
    private ActionInformation actionInformation;
    private String backupName = "testingBackupName";
    private String backupType = BackupType.DEFAULT.getDescription();

    private static final String HOST_URL = "localhost";
    private static final int PORT = 8043;

    private static final String PRE_RESTORE_ACTIONS = "/flow-automation/internal/v1/pre-restore-actions";
    private static final String POST_RESTORE_ACTIONS = "/flow-automation/internal/v1/post-restore-actions";

    @Before
    public void setUp() {
        PropertiesHelper.loadProperties("src/test/resources/application.properties");
        agent = FAAgentFactory.createAgent();
        agentBehavior = new FAAgentBehavior();
        RestAssured.port = PORT;
        actionInformation = new ActionInformation(backupName, backupType);

        log.info("Flow Automation wiremock server starting ...");
        wireMockServer = new WireMockServer(PORT);
        wireMockServer.start();
        configureFor(HOST_URL, PORT);
        log.info("Flow Automation wiremock server: started on {}:{}",HOST_URL, PORT);
    }

    @Test
    public void getRegistrationInformation_valuesSetAsProperties_getRegistrationInformationFilledFromProperties() {
        PropertiesHelper.loadProperties("src/test/resources/application.properties");

        final RegistrationInformation registrationInformation = agentBehavior.getRegistrationInformation();

        assertEquals("a", registrationInformation.getAgentId());
        assertEquals("b", registrationInformation.getApiVersion());
        assertEquals("c", registrationInformation.getScope());
        assertEquals("d", registrationInformation.getSoftwareVersion().getDescription());
        assertEquals("2019-09-13", registrationInformation.getSoftwareVersion().getProductionDate());
        assertEquals("f", registrationInformation.getSoftwareVersion().getProductName());
        assertEquals("g", registrationInformation.getSoftwareVersion().getProductNumber());
        assertEquals("h", registrationInformation.getSoftwareVersion().getType());
        assertEquals("i", registrationInformation.getSoftwareVersion().getRevision());
    }

    @Test
    public void testExecuteRestore_whenTheServiceReturns204() {
        stubFor(post(urlEqualTo(PRE_RESTORE_ACTIONS)).willReturn(aResponse()
            .withStatus(204)
        ));
        final String expectedMessagePreResote = "eric-oss-flow-automation-brAgent has completed preRestore";
        final String expectedMessageResote = "oss-flow-automation-brAgent has completed executeRestore";
        final String expectedMessagePostResote = "eric-oss-flow-automation-brAgent has completed postRestore";

        //prepare Restore
        RestorePreparationActionsStub restorePreparationActions = new RestorePreparationActionsStub();
        agentBehavior.prepareForRestore(restorePreparationActions);
        assertTrue(restorePreparationActions.isSuccessful());
        assertEquals(expectedMessagePreResote, restorePreparationActions.getMessage());

        //execute restore
        RestoreExecutionActionsStub restoreExecutionActions = new RestoreExecutionActionsStub();
        agentBehavior.executeRestore(restoreExecutionActions);
        assertTrue(restoreExecutionActions.isSuccessful());
        assertEquals(expectedMessageResote, restoreExecutionActions.getMessage());

        stubFor(post(urlEqualTo(POST_RESTORE_ACTIONS)).willReturn(aResponse()
            .withStatus(204)
        ));
        //Post Restore
        RestorePostActionsStub restorePostActionsStub = new RestorePostActionsStub();
        agentBehavior.postRestore(restorePostActionsStub);
        assertTrue(restorePostActionsStub.isSuccessful());
        assertEquals(expectedMessagePostResote, restorePostActionsStub.getMessage());
    }

    @Test
    public void testExecutePreRestore_whenTheServerReturns500() {
        stubFor(post(urlEqualTo(PRE_RESTORE_ACTIONS)).willReturn(aResponse()
            .withStatus(500)
        ));
        final String expectedMessagePreResote = "eric-oss-flow-automation-brAgent unexpected status code returned";

        //prepare Restore
        RestorePreparationActionsStub restorePreparationActions = new RestorePreparationActionsStub();
        agentBehavior.prepareForRestore(restorePreparationActions);
        assertFalse(restorePreparationActions.isSuccessful());
        assertEquals(expectedMessagePreResote, restorePreparationActions.getMessage());
    }

    @Test
    public void testExecutePostRestore_whenTheServerReturns500_thePrepareForRestore_whenTheServerReturns204() {
        stubFor(post(urlEqualTo(POST_RESTORE_ACTIONS)).willReturn(aResponse()
            .withStatus(500)
        ));

        final String expectedMessagePostResote = "eric-oss-flow-automation-brAgent has completed postRestore";

        //Post Restore
        RestorePostActionsStub restorePostActionsStub = new RestorePostActionsStub();
        agentBehavior.postRestore(restorePostActionsStub);
        assertFalse(restorePostActionsStub.isSuccessful());
        assertNotEquals(expectedMessagePostResote, restorePostActionsStub.getMessage());

        stubFor(post(urlEqualTo(PRE_RESTORE_ACTIONS)).willReturn(aResponse()
            .withStatus(204)
        ));
        //prepare Restore
        RestorePreparationActionsStub restorePreparationActions = new RestorePreparationActionsStub();
        agentBehavior.prepareForRestore(restorePreparationActions);
        assertTrue(restorePreparationActions.isSuccessful());
        assertEquals("eric-oss-flow-automation-brAgent has completed preRestore", restorePreparationActions.getMessage());
    }

    @Test (expected = Exception.class)
    public void testExecutePostRestore_whenTheServiceNotAvailable() {
        log.info("stopping Flow Automation wiremock server");
        wireMockServer.stop();
        //Post Restore
        RestorePostActionsStub restorePostActionsStub = new RestorePostActionsStub();
        agentBehavior.postRestore(restorePostActionsStub);
    }

    @Test
    public void testExecuteBackup() {
        //prepare Backup
        BackupPreparationActions backupPreparationActions = new BackupPreparationActions(agent, actionInformation);
        agentBehavior.prepareForBackup(backupPreparationActions);

        //execute Backup
        BackupExecutionActions backupExecutionActions = new BackupExecutionActions(agent, actionInformation);
        agentBehavior.executeBackup(backupExecutionActions);

        //Post Backup
        PostBackupActions postBackupActions = new PostBackupActions(agent, actionInformation);
        agentBehavior.postBackup(postBackupActions);
    }


    @Test
    public void testCancelAction() {
        CancelActions cancelActions = new CancelActions(agent, backupName, Action.CANCEL);
        agentBehavior.cancelAction(cancelActions);
    }

    @After
    public void after(){
        log.info("stopping Flow Automation wiremock server");
        wireMockServer.stop();
    }

}