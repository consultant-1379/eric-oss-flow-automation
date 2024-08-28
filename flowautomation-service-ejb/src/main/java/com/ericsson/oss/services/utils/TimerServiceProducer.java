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

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produces default TimerService for injection. For TimerService to be injected it has to use @Inject instead of @Resource annotation.
 */
public class TimerServiceProducer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Produce the timer service
     *
     * @return a TimerServiceWrapper instance
     */
    @Produces
    @Alternative
    @Dependent
    public TimerServiceWrapper produceTimerService() {
        logger.trace("Producing TimerServiceWrapper instance for injection");
        return new TimerServiceWrapper();
    }

}
