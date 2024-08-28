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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import com.ericsson.oss.services.utils.JNDIUtil;

/**
 * Responsible for providing report capabilities to Flow designers.
 */
public class JeeReporterInternal implements ReporterInternal {

    /**
     * The constant FA_INTERNAL_VARIABLE_SUMMARY_REPORT.
     */
    public static final String FA_INTERNAL_VARIABLE_SUMMARY_REPORT = "FA_INTERNAL_VARIABLE_SUMMARY_REPORT";

    private static final Logger LOGGER = LoggerFactory.getLogger(JeeReporterInternal.class);

    /**
     * Empty constructor necessary for framework to instantiate class.
     */
    public JeeReporterInternal() {
        //
    }

    @Override
    public void updateReportVariable(final String businessKey, final String name, final String value) {
        LOGGER.debug("JeeReporterInternal: updateReportVariable, businessKey={}, name={}, value={}", businessKey, name, value);

        final ReporterLocal reporterBean =
            (ReporterLocal) JNDIUtil.findLocalServiceImplementationForInterface(ReporterLocal.class.getName());
        if (reporterBean != null) {
            reporterBean.updateReportVariable(businessKey, name, value);
        } else {
            LOGGER.error("Failed to find ReporterBean");
        }
    }

    @Override
    public FlowExecutionResource generateReport(final String businessKey) {
        LOGGER.debug("JeeReporterInternal: generateReport, businessKey={}", businessKey);

        final ReporterLocal reporterBean =
            (ReporterLocal) JNDIUtil.findLocalServiceImplementationForInterface(ReporterLocal.class.getName());

        if (reporterBean != null) {
            try {
                return reporterBean.generateReport(businessKey);
            } catch (FlowAutomationException e) {
                LOGGER.error("generateReport() Failed to process: {}", e.getMessage());
            }
        } else {
            LOGGER.error("Failed to find ReporterBean");
        }

        return null;
    }
}
