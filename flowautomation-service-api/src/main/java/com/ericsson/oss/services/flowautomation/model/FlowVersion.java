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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * The POJO class FlowVersion.
 */
@Getter
@EqualsAndHashCode
@ToString
public class FlowVersion {

    /** The version. */
    private final String version;

    /** The description. */
    private final String description;

    /** Flow version status */
    private final boolean active;

    /** User who created the flow. */
    private final String createdBy;

    /** Imported Date and time in UTC */
    private final String createdDate;

    /** Setup phase required */
    private final boolean setupPhaseRequired;

    /** Report supported */
    private final boolean reportSupported;

    /**
     * Instantiates a new flow version info.
     *
     * @param version            the version
     * @param description        the description
     * @param active             the active
     * @param createdBy          the created by
     * @param createdDate        the imported date
     * @param setupPhaseRequired the setupPhaseRequired
     * @param reportSupported    the reportSupported
     */
    public FlowVersion(final String version, final String description, final boolean active, final String createdBy, final String createdDate, final boolean setupPhaseRequired, final boolean reportSupported) {
        this.version = version;
        this.description = description;
        this.active = active;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.setupPhaseRequired = setupPhaseRequired;
        this.reportSupported = reportSupported;
    }

}
