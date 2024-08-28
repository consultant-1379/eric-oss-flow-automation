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

import java.util.List;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEventEntity;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventFilter;

/**
 * The interface Flow execution event repository.
 */
public interface FlowExecutionEventRepository extends Repository<FlowExecutionEventEntity, Long> {

    /**
     * Returns the collection of {@code FlowExecutionEventEntity} that matches the specified filter.
     *
     * @param filter the filter
     * @return the list
     */
    List<FlowExecutionEventEntity> findByFilter(FlowExecutionEventFilter filter);

    /**
     * Gets the events count that matches the specified filter.
     *
     * @param filter the filter
     * @return the events count
     */
    long getEventsCount(FlowExecutionEventFilter filter);

}
