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

package com.ericsson.oss.services.flowautomation.entities;

import com.ericsson.oss.services.flowautomation.model.FlowSource;

/**
 * The enum Flow entity source.
 */
public enum FlowEntitySource {

    /**
     * Internal flow entity source.
     */
    INTERNAL,
    /**
     * External flow entity source.
     */
    EXTERNAL;

    /**
     * From flow entity source.
     *
     * @param source
     *         the source
     * @return the flow entity source
     */
    public static FlowEntitySource from(final FlowSource source) {
        return FlowEntitySource.valueOf(source.name());
    }
}
