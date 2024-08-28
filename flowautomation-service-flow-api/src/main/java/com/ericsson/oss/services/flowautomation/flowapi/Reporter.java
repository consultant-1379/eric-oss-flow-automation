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

package com.ericsson.oss.services.flowautomation.flowapi;

import static com.ericsson.oss.services.flowautomation.entities.ReportVariableEntity.FA_INTERNAL_VARIABLE_SUMMARY_REPORT;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Map;

import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.utils.ServiceLoaderUtil;
import com.ericsson.oss.services.flowautomation.internal.report.ReporterInternal;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Responsible for providing report capabilities to Flow designers.
 */
public class Reporter {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(Reporter.class);

    private Reporter() {
        //
    }

    /**
     * Update report summary.
     *
     * @param execution the execution
     * @param summary   the summary
     */
    public static void updateReportSummary(final DelegateExecution execution, final String summary) {
        updateReportVariable(execution, FA_INTERNAL_VARIABLE_SUMMARY_REPORT, summary);
    }

    /**
     * Update report variable.
     *
     * @param execution the execution
     * @param name      the name
     * @param value     the value
     */
    public static void updateReportVariable(final DelegateExecution execution, final String name, final String value) {
        final String businessKey = execution.getProcessBusinessKey();
        LOGGER.debug("Reporter: updateReportVariable -> businessKey={}, name={}, value={}", businessKey, name, value);
        final ReporterInternal reporterInternal = ServiceLoaderUtil.loadExactlyOneInstance(ReporterInternal.class);
        reporterInternal.updateReportVariable(businessKey, name, value);
    }

    /**
     * Update report variable.
     *
     * @param execution the execution
     * @param name      the name
     * @param value     the value
     */
    public static void updateReportVariable(final DelegateExecution execution, final String name, final Object value) {
        if (value instanceof Map) {
            try {
                updateReportVariable(execution, name, objectMapper.writeValueAsString(value));
            } catch (final IOException e) {
                LOGGER.error("Error on converting map report variable to json string:"  + e.getMessage(), e);
                throw new FlowAutomationException("Error on converting map report variable to json string: " + e.getMessage(), e);
            }
        } else {
            updateReportVariable(execution, name, String.valueOf(value));
        }
    }

    public static void updateReportVariable(final String businessKey, final String name, final String value) {
        LOGGER.debug("Reporter: updateReportVariable -> businessKey={}, name={}, value={}", businessKey, name, value);
        final ReporterInternal reporterInternal = ServiceLoaderUtil.loadExactlyOneInstance(ReporterInternal.class);
        reporterInternal.updateReportVariable(businessKey, name, value);
    }

}
