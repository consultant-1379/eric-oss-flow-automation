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

package com.ericsson.oss.services.flowautomation.execution.resource;

import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionRepository;

/**
 * This class loads the resources by getting the deployment id of the flow package.
 */
public class FlowPackageResourceLoader {

    @Inject
    private FlowExecutionRepository flowExecutionRepository;

    @Inject
    private FlowResourceLoader resourceLoader;



    /**
     * @param businessKey
     * @param location
     * @return List<String>
     */
    public List<String> list(final String businessKey, final String location) {
        return resourceLoader.listResources(getDeploymentId(businessKey), location);
    }


    /**
     * @param businessKey
     * @param resourcePath
     * @return String
     */
    public String get(final String businessKey, final String resourcePath) {
        return resourceLoader.getUTF8FlowResourceByDeploymentId(getDeploymentId(businessKey), resourcePath).orElse(null);
    }

    /**
     * Gets the deployment Id of the flow using the flowexecution business key.
     *
     * @param businessKey the business key of the flowexecution
     * @return the deployment id of the flow
     */
    private String getDeploymentId(final String businessKey) {
        final long businessKeyId = Long.parseLong(new FlowBusinessKey(businessKey).getBusinessKeyId());
        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.findOne(businessKeyId);
        return flowExecutionEntity.getFlowDetailEntity().getDeploymentId();
    }
}
