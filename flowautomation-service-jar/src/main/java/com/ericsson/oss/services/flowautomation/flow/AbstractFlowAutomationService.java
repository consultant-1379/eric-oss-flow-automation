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

package com.ericsson.oss.services.flowautomation.flow;

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_ID_DOES_NOT_EXIST;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_INPUT_SCHEMA_IS_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_VERSION_DOES_NOT_EXIST;
import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.FLOW_DOES_NOT_EXIST;
import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.FLOW_VERSION_NOT_EXIST;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_INPUT_SCHEMA_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowAutomationServiceUtil.isVersionSyntaxValid;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader.FLOW_INPUT_SCHEMA_FILE;
import static java.lang.String.format;

import com.ericsson.oss.services.flowautomation.exception.EntityNotFoundException;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowServiceException;
import com.ericsson.oss.services.flowautomation.flow.exception.InvalidPayloadException;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowAutomationBpmnEngineUtil;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader;
import com.ericsson.oss.services.flowautomation.flow.utils.PayloadTransformer;
import com.ericsson.oss.services.flowautomation.repo.FlowDetailRepository;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionRepository;
import com.ericsson.oss.services.flowautomation.repo.FlowRepository;
import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;
import com.ericsson.oss.services.utils.SimpleContextService;
import javax.inject.Inject;
import org.camunda.bpm.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * The type Abstract flow automation service.
 */
public abstract class AbstractFlowAutomationService {

    /** The Constant USER_ID. */
    public static final String USER_ID = "UserID";

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFlowAutomationService.class);

    /** The process engine. */
    @Inject
    @DefaultProcessEngine
    protected ProcessEngine processEngine;

    /** The flow repository dao */
    @Inject
    protected FlowRepository flowRepository;

    /** The flow detail repository dao */
    @Inject
    protected FlowDetailRepository flowDetailRepository;

    /** The flow execution repository dao */
    @Inject
    protected FlowExecutionRepository flowExecutionRepository;

    /** The context service. */
    @Inject
    protected SimpleContextService contextService;

    /**
     * The Flow resource loader.
     */
    @Inject
    protected FlowResourceLoader flowResourceLoader;

    @Inject
    protected FlowAutomationBpmnEngineUtil flowAutomationBpmnEngineUtil;

    /**
     * Check flowId does exist.
     *
     * @param flowId the flow id
     */
    public void validateFlowId(final String flowId) {
        if (!flowRepository.flowExists(flowId)) {
            LOGGER.debug("Flow: {} does not exist.", flowId);
            throw new EntityNotFoundException(FLOW_DOES_NOT_EXIST, format(FLOW_ID_DOES_NOT_EXIST, flowId));
        }
    }

    /**
     * Validates flow version
     *
     * @param flowId
     * @param flowVersion
     */
    public void validateFlowVersion(final String flowId, final String flowVersion) {
        if (isVersionSyntaxValid(flowVersion) && !flowDetailRepository.flowVersionExists(flowId, flowVersion)) {
            LOGGER.debug("Flow: {} with version: {} does not exist.", flowId, flowVersion);
            throw new EntityNotFoundException(FLOW_VERSION_NOT_EXIST,
                    format(FLOW_VERSION_DOES_NOT_EXIST, flowVersion, flowId));
        }
    }

    /**
     * Gets the flow input variables
     *
     * @param flowId        the flow id
     * @param version       the version
     * @param deploymentId  the deployment id
     * @param flowInputData the flow input data
     * @param fileInput     the file input
     * @return the flow input variables
     */
    protected Map<String, Object> getFlowInputVariables(final String flowId, final String version, final String deploymentId,
                                                        final String flowInputData, final Map<String, byte[]> fileInput) {
        final Optional<String> flowInputSchema = flowResourceLoader.getFlowResourceByDeploymentId(deploymentId, FLOW_INPUT_SCHEMA_FILE);
        if (!flowInputSchema.isPresent()) {
            LOGGER.error("Flow input schema not found and failed to execute the flow with id: {}, version: {} with flow input.", flowId, version);
            throw new EntityNotFoundException(FLOW_INPUT_SCHEMA_NOT_FOUND, format(FLOW_INPUT_SCHEMA_IS_NOT_FOUND, flowId, version));
        }
        try {
            return PayloadTransformer.transformJsonPayloadToMap(flowInputSchema.get(), flowInputData, fileInput);
        } catch (final InvalidPayloadException e) {
            /* Idun-2950 - This might, or might not be an error. Only the caller can know */
            LOGGER.warn("getFlowInputVariables() Failed to process: {}", e.getMessage());
            throw new FlowServiceException(e.getErrorCode(), e.getErrorDetail(), e.getCause());
        }
    }

    /**
     * Gets user in context.
     *
     * @return the user in context
     */
    protected String getUserInContext() {
        return contextService.getContextValue(USER_ID);
    }
}
