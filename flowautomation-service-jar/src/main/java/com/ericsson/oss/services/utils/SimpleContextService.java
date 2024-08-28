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

import java.io.Serializable;
import java.util.Map;

/**
 * IDUN-2154 : Replacing Context Service
 * From com.ericsson.oss.itpf.sdk.context.ContextService
 */
public interface SimpleContextService {

    /**
     * Assigns value to context parameter or overwrites previous value.
     *
     * @param contextParameterName
     *            case sensitive name of context parameter. Key used to search for value in context. Must not be null or empty. Must not begin with
     *            underscore.
     * @param contextData
     *            data assigned to particular context parameter name. Must be serializable. Can be null.
     */
    void setContextValue(String contextParameterName, Serializable contextData);

    /**
     * Retrieves value from context assigned to particular context parameter name.
     *
     * @param contextParameterName
     *            case sensitive name of context parameter. Must not be null or empty. Must not begin with underscore.
     * @param <T>
     *            the type of the context value
     * @return the value found in context data, assigned to context parameter or null in case context parameter could not be found.
     */
    <T> T getContextValue(String contextParameterName);

    /**
     * Retrieves Map containing all the Context data from the current context.
     * @return the unmodifiable context data map for the current context.
     */
    Map<String, Serializable> getContextData();

}
