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

package com.ericsson.oss.services.flowautomation.execution.resource;

import java.util.List;

/**
 * The interface FlowPackageResourceSPI allows to switch context from camunda to flowautomation EJB.
 */
public interface FlowPackageResourceSPI {

    /**
     * Lists the resources in the specified {@code location}.
     *
     * @param businessKey the businessKey
     * @param location    the location
     * @return the list of resource names
     */
    List<String> list(final String businessKey, String location);

    /**
     * Reads and returns the specified resource represented by {@code resourcePath}.
     *
     * @param businessKey  the businessKey
     * @param resourcePath the resource path
     * @return the resource as string
     */
    String get(final String businessKey, final String resourcePath);
}
