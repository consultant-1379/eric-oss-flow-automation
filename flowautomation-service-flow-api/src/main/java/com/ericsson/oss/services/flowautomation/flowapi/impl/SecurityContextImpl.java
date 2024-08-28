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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flowapi.internal.SecurityContext;
import com.ericsson.oss.services.utils.JNDIUtil;

/**
 * The type Security context.
 */
public class SecurityContextImpl implements SecurityContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityContextImpl.class);

    @Override
    public String getPrincipal(final long executionId) {
        LOGGER.debug("SecurityContextImpl: getPrincipal, executionId={}", executionId);

        final SecurityContextProvider securityContext =
            (SecurityContextProvider) JNDIUtil.findLocalServiceImplementationForInterface(SecurityContextProvider.class.getName());

        if (securityContext != null) {
            return securityContext.getPrincipal(executionId);
        }

        LOGGER.error("Failed to find SecurityContextProvider");
        return null;
    }
}
