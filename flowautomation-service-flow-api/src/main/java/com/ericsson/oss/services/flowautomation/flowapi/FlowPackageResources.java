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

package com.ericsson.oss.services.flowautomation.flowapi;

import static java.util.Objects.requireNonNull;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import com.ericsson.oss.services.flowautomation.execution.resource.FlowPackageResourceSPI;
import com.ericsson.oss.services.flowautomation.flow.utils.ServiceLoaderUtil;

/**
 * The FlowPackageResources API allows the flows to get and list the resources in the specified location.
 */
public final class FlowPackageResources {

    /**
     * Lists the resources in the specified {@code location}.
     *
     * @param execution the execution
     * @param location  the location
     * @return the list of resource names
     */
    public static List<String> list(final DelegateExecution execution, final String location) {
        return loadFlowPackageResourcesInstance().list(execution.getProcessBusinessKey(), location);
    }

    /**
     * Reads and returns the specified resource represented by {@code resourcePath}.
     *
     * @param execution    the execution
     * @param resourcePath the resource path
     * @return the resource as string
     */
    public static String get(final DelegateExecution execution, final String resourcePath) {
        return loadFlowPackageResourcesInstance().get(execution.getProcessBusinessKey(), resourcePath);
    }

    /**
     * Loads and returns the SPI implementation for FlowPackageResources.
     *
     * @return FlowPackageResourceSPI instance
     */
    private static FlowPackageResourceSPI loadFlowPackageResourcesInstance() {
        return requireNonNull(ServiceLoaderUtil.loadExactlyOneInstance(FlowPackageResourceSPI.class));
    }

    /**
     * Avoids Instantiation
     */
    private FlowPackageResources() {

    }

}
