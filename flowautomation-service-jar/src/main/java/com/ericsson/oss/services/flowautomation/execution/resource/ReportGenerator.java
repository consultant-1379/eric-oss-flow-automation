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

package com.ericsson.oss.services.flowautomation.execution.resource;

import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SETUP_PHASE_IN_PROGRESS;

import javax.inject.Inject;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.flow.FlowExecutionPhase;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder.FlowExecutionExcelReportGenerator;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;

/**
 * This class generates the report in the Xlsx format for the flow execution.
 */
@FlowExecutionResourceType("report")
public class ReportGenerator implements FlowExecutionResourceGenerator {

    @Inject
    private FlowExecutionExcelReportGenerator flowExecutionExcelReportBuilder;

    @Inject
    private FlowExecutionPhase flowExecutionPhase;

    @Override
    public FlowExecutionResource generate(final FlowExecutionEntity flowExecutionEntity, final FlowExecution flowExecution) {
        if (flowExecutionPhase.isExecutionInSetupPhase(flowExecutionEntity.getProcessInstanceId())) {
            throw new FlowResourceNotFoundException(SETUP_PHASE_IN_PROGRESS, "The report is not available in the setup phase of flow execution");
        }

        return flowExecutionExcelReportBuilder.generateReportExcel(flowExecutionEntity, flowExecution);
    }
}
