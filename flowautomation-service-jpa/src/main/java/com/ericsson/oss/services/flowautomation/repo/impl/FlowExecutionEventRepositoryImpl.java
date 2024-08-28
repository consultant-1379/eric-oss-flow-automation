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

package com.ericsson.oss.services.flowautomation.repo.impl;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEventEntity;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventFilter;
import com.ericsson.oss.services.flowautomation.model.PaginationData;
import com.ericsson.oss.services.flowautomation.model.SortData;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionEventRepository;

/**
 * The type Flow execution event repository.
 */
public class FlowExecutionEventRepositoryImpl extends AbstractRepository<FlowExecutionEventEntity, Long> implements FlowExecutionEventRepository {

    @Override
    public List<FlowExecutionEventEntity> findByFilter(final FlowExecutionEventFilter filter) {

        final Map<String, Object> qFilters = new HashMap<>();
        final Map<String, Object> qParameters = new HashMap<>();
        populateQueryFiltersAndParameters(filter, qFilters, qParameters);

        final PaginationData paginationData = filter.getPaginationData();
        final SortData sortData = filter.getSortData();
        return findAllByFilter("SELECT e FROM FlowExecutionEventEntity e",
                qFilters,
                qParameters,
                sortData != null ? "e." + sortData.getSortBy() : null,
                sortData != null ? sortData.getSortOrder().name() : null,
                paginationData != null ? paginationData.getFirstResult() : null,
                paginationData != null ? paginationData.getMaxResults() : null
        );
    }

    @Override
    public long getEventsCount(final FlowExecutionEventFilter filter) {
        final Map<String, Object> filters = new HashMap<>();
        final Map<String, Object> parameters = new HashMap<>();
        populateQueryFiltersAndParameters(filter, filters, parameters);

        return count("SELECT COUNT(e) FROM FlowExecutionEventEntity e", filters, parameters);
    }

    private void populateQueryFiltersAndParameters(final FlowExecutionEventFilter filter, final Map<String, Object> qFilters, final Map<String, Object> qParameters) {
        qFilters.put("e.flowExecutionEntity.flowDetailEntity.flowEntity.flowId", "pFlowId");
        qParameters.put("pFlowId", filter.getFlowId());

        qFilters.put("e.flowExecutionEntity.flowExecutionName", "pFlowExecutionName");
        qParameters.put("pFlowExecutionName", filter.getFlowExecutionName());

        if (Objects.nonNull(filter.getStartDate())) {
            qFilters.put("e.eventTime >=", "pStartDate");
            qParameters.put("pStartDate", new Timestamp(filter.getStartDate().getTime()));
        }

        if (Objects.nonNull(filter.getEndDate())) {
            qFilters.put("e.eventTime <=", "pEndDate");
            qParameters.put("pEndDate", new Timestamp(filter.getEndDate().getTime()));
        }

        if (isNotEmpty(filter.getMessage())) {
            qFilters.put("e.message LIKE", "pMessage");
            qParameters.put("pMessage", filter.getMessage());
        }

        if (isNotEmpty(filter.getTarget())) {
            qFilters.put("e.target LIKE", "pTarget");
            qParameters.put("pTarget", filter.getTarget());
        }

        if (Objects.nonNull(filter.getEventsSeverity()) && !filter.getEventsSeverity().isEmpty()) {
            qFilters.put("e.severity IN", "pSeverity");
            qParameters.put("pSeverity", filter.getEventsSeverity());
        }
    }


    @Override
    protected Class<FlowExecutionEventEntity> getEntityType() {
        return FlowExecutionEventEntity.class;
    }
}
