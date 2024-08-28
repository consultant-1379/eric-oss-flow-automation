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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for JNDI Lookup
 */
public class JNDIUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JNDIUtil.class);

    private JNDIUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Looking for implementation of EJB Service
     * @param serviceInterface Service name
     * @return EJB Service implementation
     */
    public static Object findLocalServiceImplementationForInterface(final String serviceInterface) {
        LOGGER.info("Looking up local service impl for interface {}", serviceInterface);

        final Properties lookupProperties = new Properties();
        lookupProperties.putAll(System.getProperties());
        lookupProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        lookupProperties.put("jboss.naming.client.ejb.context", "true");

        final InitialContext ctx = JNDIUtil.createInitialContext(lookupProperties);

        try {
            final String requiredSuffix = "!" + serviceInterface;

            final Set<String> fullPaths =  buildAllDataPaths(ctx, "java:");
            final Set<String> javaGlobalPaths = buildAllDataPaths(ctx, "java:global");
            fullPaths.addAll(javaGlobalPaths);

            Object serviceImplementation = null;
            for (final String localJndi: fullPaths) {
                if (localJndi.endsWith(requiredSuffix)) {
                    if (serviceImplementation != null) {
                        LOGGER.warn("More than 1 local EJBs implement interface. Selecting first only. Interface = {}", serviceInterface);
                    } else {
                        serviceImplementation = ctx.lookup(localJndi);
                        LOGGER.info("Found local service impl {}", localJndi);
                    }
                }
            }
            return serviceImplementation;

        } catch (final Exception exc) {
            LOGGER.error("Exception while listing all local JNDI names. Will not be able to optimize access to local EServices", exc);
        } finally {
            try {
                ctx.close();
            } catch (final NamingException namingException) {
                LOGGER.warn("NamingException while closing context: {}", namingException.getMessage());
            }
        }
        return new Object();
    }

    private static Set<String> buildAllDataPaths(final InitialContext ctx, final String name) {
        final Set<String> data = new HashSet<>();
        try {
            final NamingEnumeration<NameClassPair> nameClassList = ctx.list(name);
            while (nameClassList.hasMore()) {
                final NameClassPair next = nameClassList.next();
                final String fullChildName = name + "/" + next.getName();
                data.addAll(buildAllDataPaths(ctx, fullChildName));
            }
        } catch (final Exception exc) {
            LOGGER.trace(String.valueOf(exc));
            data.add(name);
        }
        return data;
    }

    private static InitialContext createInitialContext(final Properties lookupProperties) {
        try {
            return new InitialContext(lookupProperties);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }
}