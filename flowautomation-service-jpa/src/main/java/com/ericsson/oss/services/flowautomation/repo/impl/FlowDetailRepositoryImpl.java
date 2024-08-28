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

import static com.ericsson.oss.services.flowautomation.entities.FlowDetailEntity.FIND_BY_FLOWID_AND_VERSION;
import static com.ericsson.oss.services.flowautomation.entities.FlowDetailEntity.GET_PROCESS_DEFINITION_KEY;
import static com.ericsson.oss.services.flowautomation.entities.FlowDetailEntity.IS_FLOW_VERSION_ACTIVE;
import static com.ericsson.oss.services.flowautomation.entities.FlowDetailEntity.IS_FLOW_VERSION_EXISTS;
import static com.ericsson.oss.services.flowautomation.entities.FlowDetailEntity.NAMED_PARAM_FLOW_ID;
import static com.ericsson.oss.services.flowautomation.entities.FlowDetailEntity.NAMED_PARAM_FLOW_VERSION_STATUS;
import static com.ericsson.oss.services.flowautomation.entities.FlowDetailEntity.NAMED_PARAM_VERSION;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntity.NAMED_PARAM_FLOW_STATUS;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntityStatus.ENABLED;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.ACTIVE_FLOW_VERSION_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.ACTIVE_FLOW_VERSION_DOESNT_EXIST;

import java.util.Map;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.slf4j.Logger;

import com.ericsson.oss.services.flowautomation.entities.FlowDetailEntity;
import com.ericsson.oss.services.flowautomation.exception.EntityNotFoundException;
import com.ericsson.oss.services.flowautomation.jpa.QueryParameter;
import com.ericsson.oss.services.flowautomation.repo.FlowDetailRepository;

/**
 * DAO Implementation for Entity {@link FlowDetailEntity}
 */
public class FlowDetailRepositoryImpl extends AbstractRepository<FlowDetailEntity, Long> implements FlowDetailRepository {

    /** The Logger */
    @Inject
    private Logger logger;

    @Override
    protected Class<FlowDetailEntity> getEntityType() {
        return FlowDetailEntity.class;
    }

    @Override
    public FlowDetailEntity getActiveFlowVersion(final String flowId) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_PARAM_FLOW_ID, flowId).and(NAMED_PARAM_FLOW_VERSION_STATUS, true)
                .and(NAMED_PARAM_FLOW_STATUS, ENABLED.getStatus()).parameters();
        try {
            return this.findOne(GET_PROCESS_DEFINITION_KEY, queryParameters);
        } catch (final NoResultException ex) {
            logger.debug("Error encountered while fetching process definition key: {}", ex.getMessage());
            throw new EntityNotFoundException(ACTIVE_FLOW_VERSION_DOESNT_EXIST, format(ACTIVE_FLOW_VERSION_NOT_FOUND, flowId), ex);
        }
    }

    @Override
    public boolean isFlowVersionActive(final String flowId, final String flowVersion) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_PARAM_FLOW_ID, flowId).and(NAMED_PARAM_VERSION, flowVersion)
                .parameters();
        try {
            findOne(IS_FLOW_VERSION_ACTIVE, queryParameters);
            return true;
        } catch (final NoResultException e) {
            logger.debug("isFlowVersionActive() Failed to process: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Flow version exists boolean.
     *
     * @param flowId      the flow id
     * @param flowVersion the flow version
     * @return the boolean
     */
    @Override
    public boolean flowVersionExists(final String flowId, final String flowVersion) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_PARAM_FLOW_ID, flowId).and(NAMED_PARAM_VERSION, flowVersion)
                .parameters();
        try {
            return count(IS_FLOW_VERSION_EXISTS, queryParameters) > 0;
        } catch (final NoResultException ex) {
            logger.debug("flowVersionExists() Failed to process: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public FlowDetailEntity getFlowVersion(final String flowId, final String flowVersion) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_PARAM_FLOW_ID, flowId).and(NAMED_PARAM_VERSION, flowVersion)
                .parameters();
        try {
            return findOne(FIND_BY_FLOWID_AND_VERSION, queryParameters);
        } catch (final NoResultException ex) {
            logger.debug("getFlowVersion() Failed to process: {}", ex.getMessage());
            return null;
        }
    }
}
