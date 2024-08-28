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

package com.ericsson.oss.services.flowautomation.processapplication;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.impl.EjbProcessApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The FlowEjbProcessApplication class registers flow automation as a process application enabling context switching in camunda.
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@ProcessApplication("FlowAutomation")
@Local(ProcessApplicationInterface.class)
public class FlowEjbProcessApplication extends EjbProcessApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowEjbProcessApplication.class);

    @Inject
    DeploymentRegistrationBean deploymentRegistration;

    /**
     * Registers deployments with Camunda at the time of flowautomation application startup.
     * The same registration happens periodically every 15 seconds from {@code DeploymentRegistrationBean}.
     * <p>
     * So, In case of any exceptions while deploying a particular deployment, the error shouldn't be thrown out from this method,
     * as it will trigger the flowautomation ear deployment to fail.
     */
    @PostDeploy
    public void registerDeployments() {
        try {
            LOGGER.info("Setting process application ref: {}", this.getReference().getName());
            deploymentRegistration.setProcessApplicationRef(this.getReference());
            deploymentRegistration.registerForAllDeployments();
            deploymentRegistration.startDeploymentRegistrationTimer();
        } catch (final Exception e) {
            LOGGER.warn("Error while processing deployment registration, cause:", e);
        }
    }
}
