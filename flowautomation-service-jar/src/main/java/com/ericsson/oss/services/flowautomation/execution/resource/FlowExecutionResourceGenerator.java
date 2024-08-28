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

import java.util.Collection;
import java.util.Collections;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;

/**
 * The interface Flow execution resource generator, generates the requested resource.
 */
public interface FlowExecutionResourceGenerator {

    /**
     * Generates flow execution resource.
     *
     * @param flowExecutionEntity the flow execution entity
     * @param flowExecution       the flow execution
     * @return the flow execution resource
     */
    default FlowExecutionResource generate(final FlowExecutionEntity flowExecutionEntity, final FlowExecution flowExecution) {
        throw new AbstractMethodError();
    }

    /**
     * Generates the collection of resources that are zipped later by {@code ZipBundleGenerator}.
     *
     * @param flowExecutionEntity the flow execution entity
     * @param flowExecution       the flow execution
     * @return the collection of resources
     * @see ZipBundleGenerator
     */
    default Collection<FlowExecutionResource> generateForZipBundle(final FlowExecutionEntity flowExecutionEntity, final FlowExecution flowExecution) {
        return Collections.singletonList(generate(flowExecutionEntity, flowExecution));
    }
}
