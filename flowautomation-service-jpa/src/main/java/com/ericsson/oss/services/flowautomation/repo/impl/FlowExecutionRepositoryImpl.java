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

import static java.lang.String.format;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import static com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity.FIND_BY_PROCESS_INSTANCE_ID;
import static com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity.GET_ALL_PROCESS_INSTANCE_IDS;
import static com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity.GET_FLOW_EXECUTION;
import static com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity.GET_PROCESS_INSTANCE_ID;
import static com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity.IS_FLOW_EXECUTION_NAME_EXISTS;
import static com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity.NAMED_PARAM_FLOW_EXECUTION_NAME;
import static com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity.NAMED_PARAM_FLOW_ID;
import static com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity.NAMED_PARAM_PROCESS_INSTANCE_ID;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_EXECUTION_IS_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_NOT_FOUND;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.exception.EntityNotFoundException;
import com.ericsson.oss.services.flowautomation.jpa.QueryParameter;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionFilter;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionRepository;

/**
 * DAO Implementation for Entity {@link FlowExecutionEntity}
 */
public class FlowExecutionRepositoryImpl extends AbstractRepository<FlowExecutionEntity, Long> implements FlowExecutionRepository {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowExecutionRepositoryImpl.class);

    // Dynamic query criteria conditions
    private static final String BASE_QUERY = "SELECT f FROM FlowExecutionEntity f";
    private static final String QC_FLOW_ID = "f.flowDetailEntity.flowEntity.flowId IN";
    private static final String QC_EXECUTION_NAME = "f.flowExecutionName";
    private static final String QC_VERSION = "f.flowDetailEntity.version";
    private static final String QC_USER = "f.executedByUser";

    // Query key parameters
    private static final String P_FLOW_ID = "pflowId";
    private static final String P_EXECUTION_NAME = "pExecutionName";
    private static final String P_VERSION = "pVersion";
    private static final String P_USER = "pUser";

    @Override
    protected Class<FlowExecutionEntity> getEntityType() {
        return FlowExecutionEntity.class;
    }

    @Override
    public boolean flowExecutionNameExists(final String flowId, final String flowExecutionName) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_PARAM_FLOW_ID, flowId)
                .and(NAMED_PARAM_FLOW_EXECUTION_NAME, flowExecutionName).parameters();
        return count(IS_FLOW_EXECUTION_NAME_EXISTS, queryParameters) > 0;
    }

    @Override
    public List<FlowExecutionEntity> getFlowExecutions(final FlowExecutionFilter filter) {
        final Map<String, Object> filters = new HashMap<>();
        final Map<String, Object> parameters = new HashMap<>();

        if (isNotEmpty(filter.getFlowId())) {
            final List<String> flowIdList = Arrays.asList(filter.getFlowId().split(","));
            if (!flowIdList.isEmpty()) {
                filters.put(QC_FLOW_ID, P_FLOW_ID);
                parameters.put(P_FLOW_ID, flowIdList);
            }
        }

        if (isNotEmpty(filter.getFlowExecutionName())) {
            filters.put(QC_EXECUTION_NAME, P_EXECUTION_NAME);
            parameters.put(P_EXECUTION_NAME, filter.getFlowExecutionName());
        }

        if (isNotEmpty(filter.getFlowVersion())) {
            filters.put(QC_VERSION, P_VERSION);
            parameters.put(P_VERSION, filter.getFlowVersion());
        }

        if (filter.isFilterByUser()) {
            filters.put(QC_USER, P_USER);
            parameters.put(P_USER, filter.getUser());
        }

        return findAll(BASE_QUERY, filters, parameters);
    }

    @Override
    public String getProcessInstanceId(final String flowId, final String flowExecutionName) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_PARAM_FLOW_ID, flowId)
                .and(NAMED_PARAM_FLOW_EXECUTION_NAME, flowExecutionName).parameters();
        return this.singleValue(GET_PROCESS_INSTANCE_ID, queryParameters);
    }

    @Override
    public FlowExecutionEntity getFlowExecution(final String flowId, final String flowExecutionName) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_PARAM_FLOW_ID, flowId)
                .and(NAMED_PARAM_FLOW_EXECUTION_NAME, flowExecutionName).parameters();
        try {
            return this.findOne(GET_FLOW_EXECUTION, queryParameters);
        } catch (final NoResultException e) {
            LOGGER.debug("Flow: {} with execution: {} does not exist. Exception: {}", flowId, flowExecutionName, e.getMessage());
            throw new EntityNotFoundException(FLOW_EXECUTION_NOT_FOUND, format(FLOW_EXECUTION_IS_NOT_FOUND, flowId, flowExecutionName));
        }
    }

    /**
     * Get all process instance ids for currently executing flows
     */

    @Override
    public List getAllProcessInstanceIds() {
        return findAllResults(GET_ALL_PROCESS_INSTANCE_IDS, null);
    }

    @Override
    public FlowExecutionEntity getFlowExecution(final String processInstanceId) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_PARAM_PROCESS_INSTANCE_ID, processInstanceId).parameters();
        try {
            return findOne(FIND_BY_PROCESS_INSTANCE_ID, queryParameters);
        } catch (final NoResultException ex) {
            LOGGER.debug("Couldn't find the flow execution for process instance: {}. Exception: {}", processInstanceId, ex.getMessage());
            throw new EntityNotFoundException("Couldn't find the flow execution for process instance:" + processInstanceId);
        }
    }

    @Override
    public int deleteFlowExecution(final String processInstanceId) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_PARAM_PROCESS_INSTANCE_ID, processInstanceId).parameters();
        return delete(FlowExecutionEntity.DELETE_FLOW_EXECUTION, queryParameters);
    }
}
