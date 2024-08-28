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

package com.ericsson.oss.services.flowautomation.model;

import lombok.Getter;

/**
 * The type Flow execution resource.
 */
@Getter
public class FlowExecutionResource {

    private final String name;

    private final String category;

    private final byte[] data;

    /**
     * Instantiates a new Flow execution resource.
     *
     * @param name     the name
     * @param category the category
     * @param data     the data
     */
    public FlowExecutionResource(final String name, final String category, final byte[] data) {
        this.name = name;
        this.category = category;
        this.data = data.clone();
    }

    /**
     * Instantiates a new Flow execution resource.
     *
     * @param name the name
     * @param data the data
     */
    public FlowExecutionResource(final String name, final byte[] data) {
        this(name, null, data);
    }
}
