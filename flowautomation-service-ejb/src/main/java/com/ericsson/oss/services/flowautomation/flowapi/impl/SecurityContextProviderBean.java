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

package com.ericsson.oss.services.flowautomation.flowapi.impl;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionRepository;

/**
 * The type Security context provider bean.
 */
@Stateless
public class SecurityContextProviderBean implements SecurityContextProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityContextProviderBean.class);

    @Inject
    private FlowExecutionRepository flowExecutionRepository;

    @Override
    public String getPrincipal(final long executionId) {

        LOGGER.debug("SecurityContextProviderBean: getPrincipal, executionId={}", executionId);

        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.findOne(executionId);
        if (flowExecutionEntity != null) {
            return flowExecutionEntity.getExecutedByUser();
        }

        LOGGER.error("Failed to inject FlowExecutionEntity");
        return null;
    }
}
