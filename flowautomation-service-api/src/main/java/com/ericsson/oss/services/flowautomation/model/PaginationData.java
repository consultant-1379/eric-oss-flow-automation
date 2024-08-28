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
 * The type Pagination data class to hold the first and max results.
 */
@Getter
@ToString
public class PaginationData {

    private final int firstResult;
    private final int maxResults;

    /**
     * Instantiates a new Pagination data.
     *
     * @param firstResult the first result
     * @param maxResults  the max results
     */
    public PaginationData(final int firstResult, final int maxResults) {
        this.firstResult = firstResult;
        this.maxResults = maxResults;
    }
}