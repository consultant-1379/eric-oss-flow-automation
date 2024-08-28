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

package com.ericsson.oss.services.flowautomation.repo;

import com.ericsson.oss.services.flowautomation.entities.FlowDetailEntity;

/**
 * The interface Flow detail repository.
 */
public interface FlowDetailRepository extends Repository<FlowDetailEntity, Long> {

    /**
     * Gets the active flow version.
     *
     * @param flowId the flow id
     * @return the active flow version
     */
    FlowDetailEntity getActiveFlowVersion(String flowId);

    /**
     * Get flow version
     *
     * @param flowId      the flow id
     * @param flowVersion the flow version
     * @return the flow detail entity
     */
    FlowDetailEntity getFlowVersion(String flowId, String flowVersion);

    /**
     * Is flow version active boolean.
     *
     * @param flowId      the flow id
     * @param flowVersion the flow version
     * @return the boolean
     */
    boolean isFlowVersionActive(String flowId, String flowVersion);

    /**
     * Flow version exists boolean.
     *
     * @param flowId      the flow id
     * @param flowVersion the flow version
     * @return the boolean
     */
    boolean flowVersionExists(String flowId, String flowVersion);
}
