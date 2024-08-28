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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Producer class that injects appropriate SLF4J log to injection points.
 */
@ApplicationScoped
public class LoggerProducer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Producer method that injects appropriate SLF4J log to injection points.
     *
     * @param injectionPoint
     *            InjectionPoint instance provided
     * @return appropriate logger instance
     */
    @Produces
    public Logger createLoggerSLF4J(final InjectionPoint injectionPoint) {
        logger.trace("Producing logger for {}", injectionPoint);
        final Class<?> defaultLoggerName = injectionPoint.getMember().getDeclaringClass();
        logger.trace("creating default logger for {}", defaultLoggerName);
        return LoggerFactory.getLogger(defaultLoggerName);
    }

}
