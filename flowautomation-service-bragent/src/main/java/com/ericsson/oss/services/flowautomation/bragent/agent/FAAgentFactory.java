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

import com.ericsson.adp.mgmt.bro.api.agent.Agent;
import com.ericsson.adp.mgmt.bro.api.agent.AgentBehavior;
import com.ericsson.adp.mgmt.bro.api.agent.AgentFactory;
import com.ericsson.adp.mgmt.bro.api.agent.OrchestratorConnectionInformation;
import com.ericsson.oss.services.flowautomation.bragent.agent.behavior.FAAgentBehavior;
import com.ericsson.oss.services.flowautomation.bragent.util.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FAAgentFactory {

    private static final Logger log = LoggerFactory.getLogger(FAAgentFactory.class);

    private static final String ORCHESTRATOR_PORT_PROPERTY = "orchestrator.port";
    private static final String ORCHESTRATOR_HOST_PROPERTY = "orchestrator.host";
    private static final String DEFAULT_ORCHESTRATOR_HOST = "eric-ctrl-bro";
    private static final String DEFAULT_ORCHESTRATOR_PORT = "3000";
    private static final String AGENT_BEHAVIOR_CLASS_PROPERTY = "fa.agent.agentBehavior";
    private static final String DEFAULT_AGENT_BEHAVIOR_CLASS = "com.ericsson.oss.services.flowautomation.bragent.agent.behavior.FAAgentBehavior";
    private static final String MAX_INBOUND_MESSAGE_SIZE_PROPERTY = "fa.agent.maxInboundMessageSize";
    private static final String DEFAULT_MAX_INBOUND_MESSAGE_SIZE = "4194304";


    private static final String CERTIFICATE_AUTHORITY_NAME_PROPERTY = "siptls.ca.name";
    private static final String CERTIFICATE_AUTHORITY_PATH_PROPERTY = "siptls.ca.path";
    private static final String CLIENT_CERTIFICATE_CHAIN_FILE_PATH_PROPERTY = "siptls.client.certificateChainFilePath";
    private static final String CLIENT_PRIVATE_KEY_FILE_PATH_PROPERTY = "siptls.client.privateKeyFilePath";
    private static final String FLAG_GLOBAL_SECURITY_PROPERTY = "flag.global.security";
    private static final String DEFAULT_CERTIFICATE_AUTHORITY_NAME = "";
    private static final String DEFAULT_CERTIFICATE_AUTHORITY_PATH = "";
    private static final String DEFAULT_CLIENT_CERTIFICATE_CHAIN_FILE_PATH = null;
    private static final String DEFAULT_PRIVATE_KEY_FILE_PATH = null;
    private static final String DEFAULT_FLAG_GLOBAL_SECURITY = "false";


    /**
     * Prevents external instantiation.
     */
    private FAAgentFactory(){
    }

    /**
     * Creates FA agent.
     *
     * @return FA agent.
     */

    public static Agent createAgent() {
        OrchestratorConnectionInformation orchestratorConnectionInformation;
        if (getGlobalSecurityEnabled().equalsIgnoreCase("true")) {
            orchestratorConnectionInformation = new OrchestratorConnectionInformation(
                getOrchestratorHost(),
                getOrchestratorPort(),
                getCertificateAuthorityName(),
                getCertificateAuthorityPath(),
                getCertificateChainFilePath(),
                getPrivateKeyFilePath(),
                getMaxInboundMessageSize());
        } else {
            orchestratorConnectionInformation = new OrchestratorConnectionInformation(
                getOrchestratorHost(),
                getOrchestratorPort(),
                getMaxInboundMessageSize());
        }
        return AgentFactory.createAgent(orchestratorConnectionInformation, getAgentBehavior());
    }

    private static AgentBehavior getAgentBehavior() {
        final String agentBehavior = PropertiesHelper.getProperty(AGENT_BEHAVIOR_CLASS_PROPERTY, DEFAULT_AGENT_BEHAVIOR_CLASS);
        if (!agentBehavior.isEmpty()) {
            try {
                return (AgentBehavior) Class.forName(agentBehavior).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                log.info("AgentBehavior class <{}> not found - switching to DEFAULT", agentBehavior, e);
            }
        }
        return new FAAgentBehavior();
    }

    private static String getOrchestratorHost() {
        return PropertiesHelper.getProperty(ORCHESTRATOR_HOST_PROPERTY, DEFAULT_ORCHESTRATOR_HOST);
    }

    private static Integer getOrchestratorPort() {
        return Integer.valueOf(PropertiesHelper.getProperty(ORCHESTRATOR_PORT_PROPERTY, DEFAULT_ORCHESTRATOR_PORT));
    }

    private static String getCertificateAuthorityName() {
        return PropertiesHelper.getProperty(CERTIFICATE_AUTHORITY_NAME_PROPERTY, DEFAULT_CERTIFICATE_AUTHORITY_NAME);
    }

    private static String getCertificateAuthorityPath() {
        return PropertiesHelper.getProperty(CERTIFICATE_AUTHORITY_PATH_PROPERTY, DEFAULT_CERTIFICATE_AUTHORITY_PATH);
    }

    private static String getGlobalSecurityEnabled() {
        return PropertiesHelper.getProperty(FLAG_GLOBAL_SECURITY_PROPERTY, DEFAULT_FLAG_GLOBAL_SECURITY);
    }

    private static String getCertificateChainFilePath() {
        return PropertiesHelper.getProperty(CLIENT_CERTIFICATE_CHAIN_FILE_PATH_PROPERTY, DEFAULT_CLIENT_CERTIFICATE_CHAIN_FILE_PATH);
    }

    private static String getPrivateKeyFilePath() {
        return PropertiesHelper.getProperty(CLIENT_PRIVATE_KEY_FILE_PATH_PROPERTY, DEFAULT_PRIVATE_KEY_FILE_PATH);
    }

    private static int getMaxInboundMessageSize() {
        return Integer.parseInt(PropertiesHelper.getProperty(MAX_INBOUND_MESSAGE_SIZE_PROPERTY, DEFAULT_MAX_INBOUND_MESSAGE_SIZE));
    }

}
