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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The type Generic filter, can be extended by Entity/ Model specific filters.
 * It gives the pagination and sort features to any filter extending this class.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public abstract class GenericFilter {

    private PaginationData paginationData;
    private SortData sortData;

}