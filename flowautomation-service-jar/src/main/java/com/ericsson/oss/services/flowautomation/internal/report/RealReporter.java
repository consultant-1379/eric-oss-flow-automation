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

package com.ericsson.oss.services.flowautomation.internal.report;


import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.entities.ReportVariableEntity;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionRepository;
import com.ericsson.oss.services.flowautomation.repo.ReportVariableRepository;

public class RealReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RealReporter.class);

    /**
     * The report variable repository dao
     */
    @Inject
    private ReportVariableRepository reportVariableRepository;

    /** The flow execution repository dao */
    @Inject
    FlowExecutionRepository flowExecutionRepository;

    public void updateReportVariable(final String businessKey, final String variable, final String value) {
        LOGGER.debug("ReporterInternal: updateReportSummary -> flowExecutionName={}, summary={}", businessKey, value);
        final ReportVariableEntity reportVariableEntity = new ReportVariableEntity();
        final FlowBusinessKey flowBusinessKey = new FlowBusinessKey(businessKey);
        final String businessKeyId = flowBusinessKey.getBusinessKeyId();
        final Integer count = reportVariableRepository.deleteReportVariable(Long.valueOf(businessKeyId), variable);
        LOGGER.debug("Deleted {} report variable: [{}] record(s) for flowBusinessKey: {}", count, variable, flowBusinessKey);
        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.findReferenceOnly(Long.valueOf(businessKeyId));
        reportVariableEntity.setFlowExecutionEntity(flowExecutionEntity);
        reportVariableEntity.setName(variable);
        reportVariableEntity.setValue(value);
        reportVariableEntity.setSize(value == null? 0 : value.length());
        reportVariableRepository.save(reportVariableEntity);
        LOGGER.debug("Inserted ReportVariableEntity: {}", reportVariableEntity);
    }
}
