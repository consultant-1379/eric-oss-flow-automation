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

package com.ericsson.oss.services.flowautomation.flowapi.impl;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.internal.report.RealReporter;
import com.ericsson.oss.services.flowautomation.internal.report.ReporterLocal;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;

/**
 * The {@code ReporterBean} is stateless session bean and implements interface {@code ReporterLocal}}
 */
@Stateless
public class ReporterBean implements ReporterLocal {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReporterBean.class);

    @Inject
    private RealReporter realReporter;

    @Inject
    @ServiceType(JAR_TYPE)
    private FlowExecutionService executionService;

    @Override
    public void updateReportVariable(final String businessKey, final String name, final String value) {
        LOGGER.debug("ReporterBean: updateReportVariable, businessKey={}, name={}, vaue={}", businessKey, name, value);
        realReporter.updateReportVariable(businessKey, name, value);
    }

    @Override
    public FlowExecutionResource generateReport(final String businessKey) {
        final FlowBusinessKey flowBusinessKey = new FlowBusinessKey(businessKey);
        return executionService.getExecutionResource(flowBusinessKey.getFlowId(), flowBusinessKey.getFlowExecutionName(), "all");
    }
}