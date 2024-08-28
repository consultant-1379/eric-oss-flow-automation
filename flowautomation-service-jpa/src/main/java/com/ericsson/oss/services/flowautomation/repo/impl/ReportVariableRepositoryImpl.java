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

import static com.ericsson.oss.services.flowautomation.entities.ReportVariableEntity.DELETE_REPORT_VARIABLE;
import static com.ericsson.oss.services.flowautomation.entities.ReportVariableEntity.FA_INTERNAL_VARIABLE_SUMMARY_REPORT;
import static com.ericsson.oss.services.flowautomation.entities.ReportVariableEntity.NAMED_FLOW_EXECUTION_ID;
import static com.ericsson.oss.services.flowautomation.entities.ReportVariableEntity.NAMED_REPORT_VARIABLE;
import static com.ericsson.oss.services.flowautomation.entities.ReportVariableEntity.NAMED_REPORT_VARIABLE_SIZE;

import java.util.*;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.entities.ReportVariableEntity;
import com.ericsson.oss.services.flowautomation.jpa.QueryParameter;
import com.ericsson.oss.services.flowautomation.repo.ReportVariableRepository;

/**
 * DAO Implementation for Entity {@link ReportVariableEntity}
 */
public class ReportVariableRepositoryImpl extends AbstractRepository<ReportVariableEntity, Long> implements ReportVariableRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportVariableRepositoryImpl.class);

    @Override
    protected Class<ReportVariableEntity> getEntityType() {
        return ReportVariableEntity.class;
    }

    @Override
    public int deleteReportVariable(final Long businessKeyId, final String variable) {
        LOGGER.debug("Removing report variable: {} for businessKeyId: {}", variable, businessKeyId);
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_FLOW_EXECUTION_ID, businessKeyId).and(NAMED_REPORT_VARIABLE, variable)
                .parameters();
        return delete(DELETE_REPORT_VARIABLE, queryParameters);
    }

    @Override
    public String getSummaryReport(final Long businessKeyId) {
        return getVariableValue(businessKeyId, FA_INTERNAL_VARIABLE_SUMMARY_REPORT);
    }

    @Override
    public String getReportVariableValue(final long businessKeyId, final String variable) {
        return getVariableValue(businessKeyId, variable);
    }

    @Override
    public List<ReportVariableEntity> getReportVariablesWithinSize(long flowExecutionId, int size) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_FLOW_EXECUTION_ID, flowExecutionId).and(NAMED_REPORT_VARIABLE_SIZE, size).parameters();
        try {
            return findAllResults(ReportVariableEntity.GET_REPORT_VARIABLES_WITHIN_SIZE, queryParameters);
        } catch (final NoResultException e) {
            LOGGER.debug("getReportVariablesWithinSize() Failed to process: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Set<String> getVariableNamesBeyondSize(long flowExecutionId, int size) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_FLOW_EXECUTION_ID, flowExecutionId).and(NAMED_REPORT_VARIABLE_SIZE, size).parameters();
        try {

            final Query query = emw.getEntityManager().createNamedQuery(ReportVariableEntity.GET_REPORT_VARAIABLE_NAMES_BEYOND_SIZE);
            if (queryParameters != null && !queryParameters.isEmpty()) {
                queryParameters.forEach(query::setParameter);
            }
            return new HashSet<>(query.getResultList());

        } catch (final NoResultException e) {
            LOGGER.debug("getVariableNamesBeyondSize() Failed to process: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    @Override
    public Set<String> getHiddenReportVariableNames(long flowExecutionId) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_FLOW_EXECUTION_ID, flowExecutionId).parameters();
        try {

            final Query query = emw.getEntityManager().createNamedQuery(ReportVariableEntity.GET_HIDDEN_REPORT_VARAIABLE_NAMES);
            if (queryParameters != null && !queryParameters.isEmpty()) {
                queryParameters.forEach(query::setParameter);
            }
            return new HashSet<>(query.getResultList());

        } catch (final NoResultException e) {
            LOGGER.debug("Failed to process: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    private String getVariableValue(final long flowExecutionId, final String variable) {
        final Map<String, Object> queryParameters = QueryParameter.with(NAMED_FLOW_EXECUTION_ID, flowExecutionId).and(NAMED_REPORT_VARIABLE, variable)
                .parameters();
        try {
            return singleValue(ReportVariableEntity.GET_REPORT_VARIABLE, queryParameters);
        } catch (final NoResultException e) {
            LOGGER.debug("getVariableValue() Failed to process: {}", e.getMessage());
            return null;
        }
    }
}
