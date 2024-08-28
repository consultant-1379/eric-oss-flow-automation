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

package com.ericsson.oss.services.flowautomation.housekeeping;

import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowTaskExecutionListener.FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME;
import static com.ericsson.oss.services.flowautomation.housekeeping.HouseKeepingRetentionPeriodTimeUnit.DAY;
import static com.ericsson.oss.services.flowautomation.housekeeping.HouseKeepingRetentionPeriodTimeUnit.SECOND;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionRepository;
import com.ericsson.oss.services.flowautomation.repo.FlowRepository;
import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;

/**
 * The Class HouseKeeper.
 */
public class HouseKeeper {


    private static final Logger LOGGER = LoggerFactory.getLogger(HouseKeeper.class);

    /**
     * The process engine.
     */
    @Inject
    @DefaultProcessEngine
    protected ProcessEngine processEngine;

    /**
     * The flow execution repository dao
     */
    @Inject
    protected FlowExecutionRepository flowExecutionRepository;

    /**
     * The Flow repository.
     */
    @Inject
    protected FlowRepository flowRepository;

    /**
     * Perform house keeping.
     *
     * @param retentionPeriod         the retention period
     * @param retentionPeriodTimeUnit the retention period time unit
     */
    public List<HistoricProcessInstance>  getStoppedParentProcessInstances(final String retentionPeriod, final String retentionPeriodTimeUnit) {
        List<HistoricProcessInstance> stoppedParentProcessInstances = null;
        final List<String> processInstanceIds = flowExecutionRepository.getAllProcessInstanceIds();
        LOGGER.info("Number of Flow Executions available for House Keeping:[{}]", processInstanceIds.size());
        if (!processInstanceIds.isEmpty()) {
            final Set<String> parentProcessInstanceIds = new HashSet<>();
            parentProcessInstanceIds.addAll(processInstanceIds);
            final Date retentionPeriodDate = getRetentionPeriodDate(retentionPeriod, retentionPeriodTimeUnit);
            stoppedParentProcessInstances = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceIds(parentProcessInstanceIds)
                    .variableValueEquals(FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME, ExecutionStatus.State.STOPPED.toString()).startedBefore(retentionPeriodDate).list();
            LOGGER.info("Number of runtime Stopped Flow Executions available for House Keeping:[{}] which are Started before:[{}]",
                    stoppedParentProcessInstances.size(), retentionPeriodDate);
        }
        return stoppedParentProcessInstances;
    }

    /**
     * Gets the completed parent process instances.
     *
     * @param retentionPeriod         the retention period
     * @param retentionPeriodTimeUnit the retention period time unit
     */
    public List<HistoricProcessInstance> getCompletedParentProcessInstances(final String retentionPeriod, final String retentionPeriodTimeUnit) {
        final List<String> processInstanceIds = flowExecutionRepository.getAllProcessInstanceIds();
        List<HistoricProcessInstance> completedParentProcessInstances = null;
        LOGGER.info("Number Flow Executions available for House Keeping:[{}]", processInstanceIds.size());
        if (!processInstanceIds.isEmpty()) {
            final Set<String> parentProcessInstanceIds = new HashSet<>();
            parentProcessInstanceIds.addAll(processInstanceIds);
            final Date retentionPeriodDate = getRetentionPeriodDate(retentionPeriod, retentionPeriodTimeUnit);

            completedParentProcessInstances = processEngine.getHistoryService()
                    .createHistoricProcessInstanceQuery().processInstanceIds(parentProcessInstanceIds)
                    .finishedBefore(retentionPeriodDate).list();
            LOGGER.info("Number of Completed Flow Executions available for House Keeping:[{}] which are completed before:[{}]",
                    completedParentProcessInstances.size(), retentionPeriodDate);
        }
        return completedParentProcessInstances;
    }

    /**
     * Gets the historic process instances with a business key.
     *
     * @param businessKey   the businessKey
     */
    public List<HistoricProcessInstance>  getHistoricProcessInstancesPerBusinessKey(final String businessKey) {
        return processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).list();
    }

    /**
     * Gets the root process instances with a business key.
     *
     * @param businessKey   the businessKey
     */
    public List<ProcessInstance> getRootProcessInstancesPerBusinessKey(final String businessKey) {
        return processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceBusinessKey(businessKey)
                .rootProcessInstances().list();
    }

    /**
     * Clean up the process instances
     *
     * @param processInstanceIds the list of process instances
     */
    public void cleanUpRuntimeStoppedProcessInstances(List<String> processInstanceIds) {
        processInstanceIds.forEach(processInstanceId ->
                processEngine.getRuntimeService().deleteProcessInstance(processInstanceId, "Delete User Action", false, true, true));
    }

    /**
     * Clean up historic process instances
     *
     * @param processInstanceIds the list of process instance id
     */
    public void cleanUpHistoricFlowInstances(List<String> businessKeyIds, List<String> processInstanceIds) {
        businessKeyIds.forEach(businessKeyId ->
                flowExecutionRepository.delete(Long.valueOf(businessKeyId)));
        processEngine.getHistoryService().deleteHistoricProcessInstances(processInstanceIds);
    }

    /**
     * Sets retries for delete historic process.
     *
     * @return the retries for delete historic process
     */
    public Void setRetriesForDeleteHistoricProcess() {
        if (!processEngine.getManagementService().createJobQuery().noRetriesLeft().list().isEmpty()) {
            processEngine.getManagementService().setJobRetriesAsync(processEngine.getManagementService().createJobQuery().noRetriesLeft(), 3);
        }
        return null;
    }

    /**
     * Clean up unknown incidents void.
     *
     * @return the void
     */
    public Void cleanUpUnknownIncidents() {
        LOGGER.debug("Cleaning up unknown incidents");
        final String sqlQuery = "DELETE FROM act_ru_incident c WHERE c.proc_inst_id_ is NULL";
        LOGGER.debug("SQL Query: {}", sqlQuery);
        final int result = flowExecutionRepository.executeNativeQuery(sqlQuery);
        LOGGER.debug("Cleaned up unknown incidents :[{}]", result);
        return null;
    }

    private Date getRetentionPeriodDate(final String retentionPeriod, final String retentionPeriodTimeUnit) {
        final Calendar currentDateTime = Calendar.getInstance();
        final Date retentionPeriodDate;
        if (DAY.getValue().equals(retentionPeriodTimeUnit)) {
            retentionPeriodDate = defaultRetentionPeriod(retentionPeriod, currentDateTime);
        } else if (SECOND.getValue().equals(retentionPeriodTimeUnit)) {
            currentDateTime.add(Calendar.SECOND, -Integer.parseInt(retentionPeriod));
            retentionPeriodDate = currentDateTime.getTime();
        } else {
            retentionPeriodDate = defaultRetentionPeriod(retentionPeriod, currentDateTime);
        }
        return retentionPeriodDate;
    }

    private Date defaultRetentionPeriod(final String retentionPeriod, final Calendar currentDateTime) {
        final Date retentionPeriodDate;
        currentDateTime.add(Calendar.DATE, -Integer.parseInt(retentionPeriod));
        retentionPeriodDate = currentDateTime.getTime();
        return retentionPeriodDate;


    }
}
