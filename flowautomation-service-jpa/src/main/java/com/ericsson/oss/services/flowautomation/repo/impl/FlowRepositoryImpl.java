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

import static com.ericsson.oss.services.flowautomation.entities.FlowEntity.ENABLE_DISABLE_FLOW;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntity.FIND_BY_FLOW_ID;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntity.FLOW_NAME_EXIST_FOR_ANOTHER_FLOW;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntity.IS_FLOW_ENABLED;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntity.IS_FLOW_EXIST;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntity.NAMED_PARAM_FLOWID;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntity.NAMED_PARAM_FLOW_STATUS;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntity.NAMED_PARAM_NAME;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntityStatus.DISABLED;
import static com.ericsson.oss.services.flowautomation.entities.FlowEntityStatus.ENABLED;

import java.util.Map;

import javax.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.entities.FlowEntity;
import com.ericsson.oss.services.flowautomation.jpa.QueryParameter;
import com.ericsson.oss.services.flowautomation.repo.FlowRepository;

/**
 * DAO Implementation for Entity {@link FlowEntity}
 */
public class FlowRepositoryImpl extends AbstractRepository<FlowEntity, Long> implements FlowRepository {

    @Override
    protected Class<FlowEntity> getEntityType() {
        return FlowEntity.class;
    }

    private static final Logger logger = LoggerFactory.getLogger(FlowRepositoryImpl.class);

    @Override
    public void updateFlowStatus(final String flowId, final boolean enable) {
        String status = null;
        if (enable) {
            status = ENABLED.getStatus();
        } else {
            status = DISABLED.getStatus();
        }

        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_PARAM_FLOWID, flowId).and(NAMED_PARAM_FLOW_STATUS, status).parameters();
        if (count(IS_FLOW_ENABLED, queryParameters) > 0) {
            return;
        }
        this.update(ENABLE_DISABLE_FLOW, queryParameters);
    }

    @Override
    public FlowEntity getFlowEntity(final String flowId) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_PARAM_FLOWID, flowId).parameters();
        try {
            return findOne(FIND_BY_FLOW_ID, queryParameters);
        } catch (final NoResultException ex) {
            logger.warn("getFlowEntity() Failed to process: {}", ex.getMessage());
            return null;
        }
    }

    @Override
    public boolean flowExists(final String flowId) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_PARAM_FLOWID, flowId).parameters();
        try {
            return count(IS_FLOW_EXIST, queryParameters) > 0;
        } catch (final NoResultException ex) {
            logger.warn("flowExists() Failed to process: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean isFlowNameInUseByAnotherFlow(final String flowId, final String name) {
        final Map<String, Object> queryParameters = QueryParameter
                .with(NAMED_PARAM_FLOWID, flowId)
                .and(NAMED_PARAM_NAME, name)
                .parameters();
        try {
            return count(FLOW_NAME_EXIST_FOR_ANOTHER_FLOW, queryParameters) > 0;
        } catch (final NoResultException ex) {
            logger.warn("isFlowNameInUseByAnotherFlow() Failed to process: {}", ex.getMessage());
            return false;
        }
    }

}
