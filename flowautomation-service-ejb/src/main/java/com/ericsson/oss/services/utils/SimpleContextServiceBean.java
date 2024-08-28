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
 * From com.ericsson.oss.itpf.sdk.context.classic.ClassicContextServiceImpl
 */
public class SimpleContextServiceBean implements SimpleContextService {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getContextValue(final String contextKey) {
        return (T) SimpleContextDataFactory.getContextData().get(contextKey);
    }

    @Override
    public void setContextValue(final String contextKey, final Serializable contextValue) {
        SimpleContextDataFactory.getContextData().put(contextKey, contextValue);
    }

    @Override
    public Map<String, Serializable> getContextData() {
        return SimpleContextDataFactory.getContextData();
    }
}
