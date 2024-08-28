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
import lombok.ToString;

/**
 * The type Sort data used for storing the sort field and order.
 */
@Getter
@ToString
public class SortData {

    private final String sortBy;
    private final SortOrder sortOrder;

    /**
     * Instantiates a new Sort data.
     *
     * @param sortBy    the sort by
     * @param sortOrder the sort order
     */
    public SortData(final String sortBy, final SortOrder sortOrder) {
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
    }
}
