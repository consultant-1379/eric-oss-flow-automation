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

package com.ericsson.oss.services.flowautomation.bragent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.ericsson.adp.mgmt.bro.api.exception.InvalidRegistrationInformationException;
import com.ericsson.adp.mgmt.control.AgentControl;
import com.ericsson.adp.mgmt.control.ControlInterfaceGrpc;
import com.ericsson.adp.mgmt.control.OrchestratorControl;
import com.ericsson.oss.services.flowautomation.bragent.test.BroGrpcStub;
import io.grpc.BindableService;
import io.grpc.stub.StreamObserver;
import org.awaitility.Awaitility;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class BackupAndRestoreAgentIntegrationTest extends BroGrpcStub {

    private ControlInterface controlInterface;

    @Test
    public void main_startsTestAgent_testAgentIsAlive() {
        startTestAgent();
        waitUntil(this.controlInterface::receivedMessage);
        assertNotNull(BackupAndRestoreAgentFA.getAgent());
    }

    @Test
    public void main_startsTestAgent_noProperties() {
        startTestAgent();

        waitUntil(this.controlInterface::receivedMessage);
        assertNotNull(BackupAndRestoreAgentFA.getAgent());
    }

    @Test
    public void main_startsFAAgentAndChannelToOrchestratorCloses_agentRetries() throws Exception {
        startTestAgent();

        waitUntil(this.controlInterface::receivedMessage);

        this.controlInterface.reset();

        restartGrpcServer();

        waitUntil(this.controlInterface::receivedMessage);
    }

    @Test
    public void main_orchestratorUnavailableWhenAgentStarts_agentRetries() throws Exception {
        stopGrpcServer();

        startTestAgent();

        assertFalse(this.controlInterface.receivedMessage);

        startGrpcServer();

        waitUntil(this.controlInterface::receivedMessage);
    }

    @Test
    public void main_startsTestAgentWithNonExistingBehavior_usesDefaultBehaviorAndTriesToRegister() throws Exception {
        startTestAgent("src/test/resources/invalidBehavior.properties");

        waitUntil(this.controlInterface::receivedMessage);
        assertNotNull(BackupAndRestoreAgentFA.getAgent());
    }

    @Test(expected = InvalidRegistrationInformationException.class)
    public void main_startsTestAgentWithInvalidAgentId_throwsException() throws Exception {
        startTestAgent("src/test/resources/invalidAgentId.properties");
    }

    @Override
    protected List<BindableService> getServices() {
        this.controlInterface = new ControlInterface();
        return Arrays.asList(this.controlInterface);
    }

    private void startTestAgent() {
        startTestAgent(new String[] { "src/test/resources/application.properties" });
    }

    private void startTestAgent(final String propertiesFile) {
        startTestAgent(new String[] { propertiesFile });
    }

    private void startTestAgent(final String[] args) {
        BackupAndRestoreAgentFA.startFaagent(args);
        waitUntil(() -> BackupAndRestoreAgentFA.getAgent() != null);
    }

    private void waitUntil(final Callable<Boolean> condition) {
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(condition);
    }

    //This interface is used to control and coordinate messages / events between the Orchestrator and Agents
    private class ControlInterface extends ControlInterfaceGrpc.ControlInterfaceImplBase {

        private boolean receivedMessage;

        //AgentControl represents the messages that can be sent from the Agent to the Orchestrator.
        @Override
        public StreamObserver<AgentControl> establishControlChannel(final StreamObserver<OrchestratorControl> orchestratorControlStream) {
            return new StreamObserver<AgentControl>() {

                @Override
                public void onNext(final AgentControl arg0) {
                    ControlInterface.this.receivedMessage = true;
                }

                @Override
                public void onError(final Throwable arg0) {
                    //Not needed
                }

                @Override
                public void onCompleted() {
                    //Not needed
                }
            };
        }

        public boolean receivedMessage() {
            return this.receivedMessage;
        }

        public void reset() {
            this.receivedMessage = false;
        }

    }

}
