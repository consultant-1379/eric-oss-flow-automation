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

package com.ericsson.oss.services.flowautomation.deployment.processor;

import org.camunda.bpm.engine.repository.DeploymentBuilder;

/**
 * Interface to process the deployment entity.
 */
public interface FlowAutomationDeploymentProcessor {

    /**
     * Process deployment.
     *
     * @param processDefinitionKey
     *            the process definition key
     * @param deploymentBuilder
     *            the deployment builder
     */
    void processDeployment(final String processDefinitionKey, final DeploymentBuilder deploymentBuilder);

    /**
     * Generate process id.
     *
     * @param processDefinitionKey
     *            the process definition key
     * @param processId
     *            the process id
     * @return the string
     */
    String generateProcessId(final String processDefinitionKey, final String processId);

}
