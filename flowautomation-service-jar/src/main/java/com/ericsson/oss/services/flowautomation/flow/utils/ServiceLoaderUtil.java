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

package com.ericsson.oss.services.flowautomation.flow.utils;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used by APIs which need to be able to load implementations using Java {@link ServiceLoader} feature.
 */
public abstract class ServiceLoaderUtil {


    private ServiceLoaderUtil() {
        throw new IllegalStateException("Utility class.");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLoaderUtil.class);

    /**
     * Loads exactly one instance of provided interface. Implementation must follow {@link ServiceLoader} conventions and must be available in the
     * classpath. If no implementation can be found an exception is thrown. If multiple implementations are found then one of them is used (last one
     * detected).
     *
     * @param clazz
     *            the interface for which to find implementation
     * @param <T>
     *            the type of the interface
     *
     * @return implementation of provided interface exposed following {@link ServiceLoader} conventions.
     */
    public static <T> T loadExactlyOneInstance(final Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Unable to find implementation for null class");
        }

        LOGGER.debug("Trying to find single implementation for {}", clazz);

        final ServiceLoader<T> loader = ServiceLoader.load(clazz);
        final Iterator<T> iter = loader.iterator();
        if (iter == null) {
            LOGGER.debug("Was not able to find any implementation for [{}].", clazz);
            return null;
        }
        int count = 0;
        T impl = null;
        while (iter.hasNext()) {
            impl = iter.next();
            count++;
        }
        if (impl == null) {
            LOGGER.debug("Was not able to find any implementation for [{}].", clazz);
            return null;
        }
        if (count > 1) {
            LOGGER.warn("Found {} implementations of {}. Will use the latest one found {}.", count, clazz, impl);
        } else {
            LOGGER.debug("Found single implementation for {}", clazz);
        }
        LOGGER.trace("Successfully found {} implementation of {}", impl, clazz);
        return impl;
    }

}
