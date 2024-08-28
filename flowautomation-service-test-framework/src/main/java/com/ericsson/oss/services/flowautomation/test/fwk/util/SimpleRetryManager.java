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

package com.ericsson.oss.services.flowautomation.test.fwk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.oss.services.flowautomation.test.fwk.TestUtils.realSleepMs;

public class SimpleRetryManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public <T> T executeWithRetries(SimpleRetriableCommand<T> command) {
        return executeWithRetries(SimpleRetryPolicy.defaultRetryPolicy(), command);
    }

    public <T> T executeWithRetries(SimpleRetryPolicy retryPolicy, SimpleRetriableCommand<T> command) {
        for (int i = 0; i <= retryPolicy.retries; i++) {
            try {
                return command.execute();
            } catch (Throwable e) {
                if (i == retryPolicy.retries || !retryPolicy.retryOn.isInstance(e)) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else if (e instanceof Error) {
                        throw (Error) e;
                    }
                    throw new RetryException(e);
                }
                logger.debug("Assertion failed, retrying");
                realSleepMs(retryPolicy.waitIntervalMs);
            }
        }
        throw new RuntimeException("Should not reach here");
    }

    public interface SimpleRetriableCommand<T> {
        T execute() throws Exception;
    }

    public static class RetryException extends RuntimeException {
        public RetryException(Throwable t) {
            super(t);
        }
    }

    public static class SimpleRetryPolicy<T> {
        Class<T> retryOn;
        int waitIntervalMs;
        int retries;

        public SimpleRetryPolicy(Class<T> retryOn, int retries, int waitIntervalMs) {
            this.retryOn = retryOn;
            this.waitIntervalMs = waitIntervalMs;
            this.retries = retries;
        }

        public static SimpleRetryPolicy defaultRetryPolicy() {
            return new SimpleRetryPolicy(Throwable.class, 150, 500);
        }
    }
}
