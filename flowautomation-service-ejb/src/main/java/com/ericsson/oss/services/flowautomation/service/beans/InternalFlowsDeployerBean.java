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

package com.ericsson.oss.services.flowautomation.service.beans;

import static java.util.concurrent.TimeUnit.SECONDS;
import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.EJB_TYPE;
import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;
import static com.ericsson.oss.services.flowautomation.flow.AbstractFlowAutomationService.USER_ID;
import static com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows.ERICSSON;
import static com.ericsson.oss.services.flowautomation.model.FlowSource.INTERNAL;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.api.FlowService;
import com.ericsson.oss.services.flowautomation.deployment.internalflows.InternalFlowsDeployer;
import com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode;
import com.ericsson.oss.services.flowautomation.exception.EntityExistsException;
import com.ericsson.oss.services.flowautomation.exception.EntityNotFoundException;
import com.ericsson.oss.services.flowautomation.flow.FlowExecutionPhase;
import com.ericsson.oss.services.flowautomation.flow.FlowServiceImpl;
import com.ericsson.oss.services.flowautomation.flow.definition.FlowConfig;
import com.ericsson.oss.services.flowautomation.flow.definition.FlowDefinitionParser;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowPackageUtil;
import com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows;
import com.ericsson.oss.services.flowautomation.model.FlowDefinition;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowVersion;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionFilter;
import com.ericsson.oss.services.flowautomation.service.exception.InternalFlowException;
import com.ericsson.oss.services.utils.SimpleContextService;

/**
 * Session Bean responsible for deploying the internal flows located in <code>flowautomation-service-internal-flows</code> project.
 */
@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InternalFlowsDeployerBean {

    /** The Constant LOGGER. */
    private static final Logger logger = LoggerFactory.getLogger(InternalFlowsDeployerBean.class);

    /**
     * The delay(in seconds) that will be used, initially to deploy the internal flows after the instance of this class is constructed. this is also
     * used along with {@code RETRY_EXPONENTIAL_BACKOFF} while trying to retry the deployment of failed flows.
     */
    private static final int DELAY = 10;

    private static final String INTERNAL_FLOWS_LOCATION = "internalFlows/";

    @Inject
    @ServiceType(EJB_TYPE)
    private FlowService flowService;

    @Inject
    @ServiceType(EJB_TYPE)
    private FlowExecutionService flowExecutionService;

    @Inject
    @ServiceType(JAR_TYPE)
    private FlowServiceImpl flowServiceImpl;

    @Inject
    private InternalFlowsDeployer internalFlowsDeployer;

    @Inject
    private SimpleContextService contextService;

    @Inject
    private TimerService timerService;

    @Inject
    private FlowExecutionPhase flowExecutionPhase;

    /**
     * Reads the internal flows from the location {@value #INTERNAL_FLOWS_LOCATION}. and creates the single action timer to start deploying the flows.
     */
    @PostConstruct
    private void readInternalFlows() {
        internalFlowsDeployer.loadFlowAutomationInternalFlows();
        if (internalFlowsDeployer.internalFlowsAvailable()) {
            timerService.createSingleActionTimer(SECONDS.toMillis(DELAY), new TimerConfig("internal-flows-deployer", false));
        } else {
            logger.info("There are no internal flow packages found in directory: {}", INTERNAL_FLOWS_LOCATION);
        }
    }

    /**
     * This is the ejb timeout callback method and is called on expiry of the timer {@value #DELAY}. This method uses the {@code RetryManager} to
     * deploy the internal flows with retries.
     */
    @Timeout
    public void deployInternalFlows() {
        internalFlowsDeployer.getInternalFlows().entrySet().removeIf(entry -> processFlow(entry.getKey(), entry.getValue()));
        if (internalFlowsDeployer.internalFlowsAvailable()) {
            logger.warn("All the internal flows are not processed, the failing flows: {}",
                    internalFlowsDeployer.getInternalFlows());
            throw new InternalFlowException();
        }
        logger.info("All the internal flows are processed successfully");

    }

    private void preDeploy(final String flowId, final String newFlowVersion) {
        final FlowDefinition flowDefinition = flowService.getFlowDefinition(flowId);
        final List<FlowVersion> flowVersions = flowDefinition.getFlowVersions();
        final List<FlowExecution> flowExecutions = new ArrayList<>();
        flowVersions.stream().filter(flowVersion -> !flowVersion.getVersion().equals(newFlowVersion)).forEach(flowVersion ->
                flowExecutions.addAll(flowExecutionService.getExecutions(new FlowExecutionFilter(flowDefinition.getId(), null, flowVersion.getVersion(), "", false)))

        );

        flowExecutions.stream().filter(flowExecution -> flowExecutionPhase.isExecutionSuspendable(flowExecution.getProcessInstanceId())).forEach(flowExecution ->
            flowExecutionService.stopExecution(flowDefinition.getId(), flowExecution.getName())
        );

    }


    @SuppressWarnings({"squid:S2629", "squid:S3776"})
    private boolean processFlow(final String name, final byte[] flowBytes) {
        contextService.setContextValue(USER_ID, ERICSSON);
        final boolean processed = true;
        final Optional<FlowPackageErrorCode> violation = Optional.ofNullable(FlowPackageUtil.validate(flowBytes));
        if (!violation.isPresent()) {
            boolean flowVersionExists = true;
            boolean flowExists = true;
            final FlowConfig flowConfig = FlowDefinitionParser.parseFlowDefinitionJson(flowBytes);
            final String flowId = flowConfig.getFlowId();
            final String flowVersion = flowConfig.getFlowVersion();

            try{
                // check if internal flow already deployed
                flowServiceImpl.validateFlowId(flowId);
            }catch(final EntityNotFoundException e){
                flowExists = false;
                logger.info("Internal Flow with id: [{}] does not exist, hence it will be deployed. Exception: {}", flowId, String.valueOf(e));
            }

            if(flowExists){
                try {
                    // check if internal flow version already deployed
                    flowServiceImpl.validateFlowVersion(flowId, flowVersion);
                } catch (final EntityNotFoundException e) {
                    flowVersionExists = false;
                    logger.info("Internal Flow with id: [{}] version: [{}] does not exist, hence it will be deployed. Exception: {}", flowId, flowVersion, e.getMessage());
                } catch (final Exception e) {
                    logger.error("Error while checking for the flow: [{}] version: [{}]. Exception: {}", name, flowVersion, String.valueOf(e));
                    return false;
                }

            }

            if (!flowExists || !flowVersionExists) {

                if(flowExists) {
                    logger.info("Performing pre deploy procedure for the flow  with id: [{}] version: [{}]", flowId, flowVersion);
                    preDeploy(flowId, flowVersion);
                }

                //import the flow
                try {
                    flowService.importFlow(flowBytes, INTERNAL);
                    logger.info("The internal flow package: [{}] version: [{}] successfully deployed.", name, flowVersion);
                } catch (final EntityExistsException e) {
                    logger.warn("The internal flow package: [{}] version: [{}] is already deployed. Exception: {}", name, flowVersion, String.valueOf(e));
                } catch (final Exception e) {
                    logger.error("Error while processing the flow:[{}] version:[{}]. Exception: {}", name, flowVersion, String.valueOf(e));
                    return false;
                }

                //execute the flow
                try {
                    flowService.executeFlow(flowId, FlowAutomationInternalFlows.getFlowExecutionName(flowId, flowVersion));
                } catch (final EntityExistsException e) {
                    logger.warn("The flow:[{}] version:[{}] is already in execution. Exception: {}", name, flowVersion, String.valueOf(e));
                } catch (final Exception e) {
                    logger.error("Error while executing the flow:[{}] version:[{}]. Exception: {}", name, flowVersion, String.valueOf(e));
                    return false;
                }
            } else {
                logger.info("Internal Flow with id: [{}] version: [{}] exists already", flowId, flowVersion);
            }

        } else {
            logger.error("The internal flow package: [{}] validation failed and deployment won't be retried. details: {}", name,
                    violation.get().reason());
        }
        return processed;
    }
}
