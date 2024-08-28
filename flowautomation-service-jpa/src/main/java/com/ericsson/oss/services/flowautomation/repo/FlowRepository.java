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

import com.ericsson.oss.services.flowautomation.entities.FlowEntity;

public interface FlowRepository extends Repository<FlowEntity, Long> {

    /**
     * Update flow status.
     *
     * @param flowId the flow id
     * @param enable the enable
     */
    void updateFlowStatus(final String flowId, final boolean enable);

    /**
     * Gets the flow entity.
     *
     * @param flowId the flow id
     * @return the flow entity
     */
    FlowEntity getFlowEntity(final String flowId);

    /**
     * Flow exists.
     *
     * @param flowId the flow id
     * @return true, if successful
     */
    boolean flowExists(final String flowId);

    /**
     * Is flow name in use by another flow.
     *
     * @param flowId the flow id
     * @param name   the name
     * @return the boolean
     */
    boolean isFlowNameInUseByAnotherFlow(String flowId, String name);
}
