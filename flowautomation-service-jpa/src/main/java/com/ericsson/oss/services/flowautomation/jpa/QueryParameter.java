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

package com.ericsson.oss.services.flowautomation.jpa;

import java.util.HashMap;
import java.util.Map;

public class QueryParameter {

    private Map<String, Object> parameters = null;

    private QueryParameter(final String name, final Object value) {
        this.parameters = new HashMap<>();
        this.parameters.put(name, value);
    }

    public static QueryParameter with(final String name, final Object value) {
        return new QueryParameter(name, value);
    }

    public QueryParameter and(final String name, final Object value) {
        this.parameters.put(name, value);
        return this;
    }

    public Map<String, Object> parameters() {
        return this.parameters;
    }
}