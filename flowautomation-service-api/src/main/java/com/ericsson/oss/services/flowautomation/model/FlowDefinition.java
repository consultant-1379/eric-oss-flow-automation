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

import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * The POJO class FlowDefinition.
 */
@Getter
@EqualsAndHashCode
@ToString
public class FlowDefinition {

    /** The id. */
    private final String id;

    /** The name. */
    private final String name;

    /** The Flow status. */
    private final String status;

    private final String source;

    /** The Flow Versions */
    private final List<FlowVersion> flowVersions;

    /**
     * Instantiates a new flow description.
     *
     * @param id
     *         the id
     * @param name
     *         the name
     * @param status
     *         the status
     * @param source
     *         the source
     * @param flowVersions
     *         the flow versions
     */
    public FlowDefinition(final String id, final String name, final String status, final String source, final List<FlowVersion> flowVersions) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.source = source;
        this.flowVersions = Collections.unmodifiableList(flowVersions);
    }

}
