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

package com.ericsson.oss.services.flowautomation.service.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;

/**
 * This scheduling bean pre materializes the executions every 10 seconds.
 */
@Singleton
@Startup
@AccessTimeout(value = 10, unit = TimeUnit.SECONDS)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class FlowExecutionsPreMaterializer {

    private final List<FlowExecution> cachedExecutions = new ArrayList<>();

    @Inject
    @ServiceType(ServiceType.Value.JAR_TYPE)
    private FlowExecutionService flowExecutionService;

    /**
     * Update the cached executions cache every 10 seconds.
     */
    @Lock(LockType.WRITE)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Schedule(hour = "*", minute = "*", second = "*/10", persistent = false)
    public void updateCache() {
        seedExecutions();
    }

    /**
     * Clear the cache if is not empty
     */
    @Lock(LockType.WRITE)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void clearCache() {
        if (!isCacheEmpty()) {
            cachedExecutions.clear();
        }
    }

    /**
     * Initializes the cache by seeding the executions from the db.
     */
    @Lock(LockType.WRITE)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void initializeCache() {
        if (isCacheEmpty()) {
            seedExecutions();
        }
    }

    /**
     * Returns unmodifiable collection of cached executions.
     *
     * @return executions
     */
    @Lock(LockType.READ)
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<FlowExecution> getExecutions() {
        return Collections.unmodifiableList(cachedExecutions);
    }

    /**
     * Returns true if the cache is empty.
     *
     * @return boolean true or false
     */
    @Lock(LockType.READ)
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean isCacheEmpty() {
        return cachedExecutions.isEmpty();
    }

    /**
     * Seeds the executions in the cache.
     */
    private void seedExecutions() {
        cachedExecutions.clear();
        cachedExecutions.addAll(flowExecutionService.getExecutions(new FlowExecutionFilterBuilder().build()));
    }

    /**
     * Removes the deleted execution from the cache.
     *
     * @param event containing the deleted execution
     */
    @Lock(LockType.WRITE)
    public void remove(@Observes(during = TransactionPhase.AFTER_SUCCESS) final FlowExecutionDeleteEvent event) {
        cachedExecutions.removeIf(execution -> execution.getFlowId().equals(event.getFlowId()) && execution.getName().equals(event.getFlowExecutionName()));
    }
}
