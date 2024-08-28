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

package com.ericsson.oss.services.utils;

import org.jboss.security.SecurityContextAssociation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * IDUN-2154 : Replacing Context Service
 * From com.ericsson.oss.itpf.sdk.context.classic.ContextDataFactory
 */
abstract class SimpleContextDataFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleContextDataFactory.class);
    private static final String HTTP_HEADER_USERNAME_KEY = "UserID";
    private static final String SDK_CONTEXT_KEY = "com.ericsson.oss.itpf.sdk.context.key";
    private static final String HTTP_SERVLET_REQUEST_CONTEXT_NAME = "javax.servlet.http.HttpServletRequest";

    private static final ThreadLocal<Map<String, Serializable>> CONTEXT_SERVICE_DATA = ThreadLocal.withInitial(LinkedHashMap::new);

    private SimpleContextDataFactory() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * This method returns Context service instance specific to calling thread. The context service is populated with the context data generated
     * during the work-flow by various distributed components as it flows through different remote EJB invocations.
     *
     * @return the Context service instance specific to calling thread
     */
    public static Map<String, Serializable> getContextData() {
        /* context data is already present, erase stale context data if found */
        eraseStaleContextData();
        final Map<String, Serializable> contextData = CONTEXT_SERVICE_DATA.get();
        if (!contextData.containsKey(HTTP_HEADER_USERNAME_KEY)) {
            LOGGER.debug("{} not found in the context service, trying to locate in the Policy Context", HTTP_HEADER_USERNAME_KEY);
            final String userName = getUserFromContext();
            contextData.put(HTTP_HEADER_USERNAME_KEY, userName);
        }

        return contextData;
    }

    /**
     * Cleanup the Context data specific to the calling thread.
     */
    public static void cleanupThreadData() {
        CONTEXT_SERVICE_DATA.get().clear();
    }

    /**
     * Remove the Context service data current thread value.
     */
    public static void removeThreadData() {
        CONTEXT_SERVICE_DATA.remove();
    }

    /**
     * Special handling for User ID, initially it will not be found in the context service, it needs to be fetched from {@link PolicyContext}
     *
     * @return the User ID from policy context
     */
    private static String getUserFromContext() {
        LOGGER.debug("Trying to find user id from policy context");
        final Principal principal = SecurityContextAssociation.getPrincipal();
        String principalName = principal == null ? null : principal.getName();

        if (principal == null || isEmpty(principalName)) {
            LOGGER.debug("no principal name available from security context, principal: {}", principal);

            final HttpServletRequest httpRequest = getHttpRequest();

            if (httpRequest == null || (principalName = httpRequest.getHeader(HTTP_HEADER_USERNAME_KEY)) == null || principalName.trim().isEmpty()) {
                LOGGER.debug("Unable to find user id in HttpRequest-{} , principalName-{}", httpRequest, principalName);
            }
        }
        LOGGER.debug("principal name fetched from context: {}", principalName);
        return principalName;
    }

    /**
     * Method uses HTTPRequest object to identify if there is any stale context data found in server environment threads are re-used from threadpool
     * and they may still contain the context data from old request, hence that needs to be cleared
     *
     * @return
     */
    private static void eraseStaleContextData() {
        final HttpServletRequest httpRequest = getHttpRequest();

        if (httpRequest != null) {
            final String contextKey = (String) httpRequest.getAttribute(SDK_CONTEXT_KEY);
            if (contextKey == null) {
                /* new request, delete old context data if found */
                CONTEXT_SERVICE_DATA.get().clear();
                httpRequest.setAttribute(SDK_CONTEXT_KEY, "Context service initialized for this HTTP Request");
            }
        }
    }

    /**
     * This method fetches HttpRequest object from policy context if the flow is started by http request
     *
     * @return the HttpRequest object from policy context
     */
    private static HttpServletRequest getHttpRequest() {
        HttpServletRequest httpRequest = null;
        try {
            httpRequest = (HttpServletRequest) PolicyContext.getContext(HTTP_SERVLET_REQUEST_CONTEXT_NAME);
        } catch (final PolicyContextException | IllegalArgumentException | SecurityException e) {
            LOGGER.warn("policy context exception caught getting context for {}, Exception- {}", HTTP_SERVLET_REQUEST_CONTEXT_NAME, String.valueOf(e));
        }
        return httpRequest;
    }

    private static boolean isEmpty(final String val) {
        return val == null || val.trim().length() == 0;
    }

}

