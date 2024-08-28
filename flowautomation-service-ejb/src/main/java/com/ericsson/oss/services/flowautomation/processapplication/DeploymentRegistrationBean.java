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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;

@Singleton
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class DeploymentRegistrationBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentRegistrationBean.class);

    @Resource
    private TimerService timerService;

    /**
     * The process engine.
     */
    @Inject
    @DefaultProcessEngine
    protected ProcessEngine processEngine;

    private ProcessApplicationReference processApplicationReference;

    /**
     * Observe import events
     */
    public void observeImportEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) FlowDefinition flowDefinition) {
        if(Objects.nonNull(processApplicationReference)) {
            registerForAllDeployments();
        } else {
            LOGGER.debug("Deployments not registered as processApplicationReference is null");
        }
    }

    /**
     * Set process application reference
     *
     * @param reference Process application reference
     */
    public void setProcessApplicationRef(ProcessApplicationReference reference) {
        LOGGER.info("Setting process ref: {}", reference.getName());
        processApplicationReference = reference;
    }

    /**
     * Start deployment registration timer
     */
    public void startDeploymentRegistrationTimer(){
        TimerConfig timerConfig = new TimerConfig("Deployment registration Scheduler", false);
        ScheduleExpression schedule = new ScheduleExpression();
        schedule.hour("*").minute("*").second("*/15");
        timerService.createCalendarTimer(schedule, timerConfig);
    }

    /**
     * Register for all deployments
     */
    @Timeout
    public void registerForAllDeployments() {
        final List<Deployment> deployments = processEngine.getRepositoryService().createDeploymentQuery().list();
        LOGGER.debug("{} deployments found", deployments.size());
        for (final Deployment deployment : deployments) {
            final String deploymentId = deployment.getId();
            LOGGER.debug("Checking registration for deployment id: {}, name: {}", deploymentId, deployment.getName());
            final ManagementService processEngineManagementService = processEngine.getManagementService();
            if (!isAlreadyRegisteredForJobExecutor(deploymentId, processEngineManagementService)) {
                LOGGER.info("Registering for deployment id: {}, name: {}", deploymentId, deployment.getName());
                processEngineManagementService.registerProcessApplication(deploymentId, processApplicationReference);
                processEngineManagementService.registerDeploymentForJobExecutor(deploymentId);
            }
        }
    }

    /**
     * Checks if deployment is already registered for a job executor
     *
     * @param deploymentId Deployment ID
     * @param processEngineManagementService Process engine management service
     */
    private boolean isAlreadyRegisteredForJobExecutor(final String deploymentId, final ManagementService processEngineManagementService) {
        boolean registered = false;
        final Set<String> jobExecutorRegisteredDeployments = processEngineManagementService.getRegisteredDeployments();
        for (final Iterator<String> iter = jobExecutorRegisteredDeployments.iterator(); iter.hasNext(); ) {
            final String xdeploymentId = iter.next();
            if (xdeploymentId.equals(deploymentId)) {
                final String processApplicationName = processEngineManagementService.getProcessApplicationForDeployment(xdeploymentId);
                if (processApplicationName != null) {
                    LOGGER.debug("Process application registered for id: {} is {}", deploymentId, processApplicationName);
                    registered = true;
                }
                break;
            }
        }
        return registered;
    }
}
