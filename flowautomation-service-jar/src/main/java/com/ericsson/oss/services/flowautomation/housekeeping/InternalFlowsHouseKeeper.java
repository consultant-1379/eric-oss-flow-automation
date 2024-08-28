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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.repo.FlowExecutionRepository;
import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;

/**
 * The type Internal flows house keeper.
 */
public class InternalFlowsHouseKeeper {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalFlowsHouseKeeper.class);

    private static final String HOUSE_KEEPING_FLOW_ID_PATTERN = "com.ericsson.oss.fa.internal.flows.houseKeeping%";
    private static final String P_PROCESS_DEFINITION_KEY = "pProcessDefinitionKey";
    private static final int ACT_INST_TABLE_MAX_ROWS = 1000;
    private static final int PROC_INST_TABLE_MAX_ROWS = 1000;
    private static final String SQL_CLEAN_CAMUNDA_HISTORY_ACT_INST_TABLE = "DELETE FROM act_hi_actinst c1 WHERE c1.ctid IN (SELECT ctid FROM act_hi_actinst c WHERE c.proc_def_key_ LIKE :pProcessDefinitionKey AND c.end_time_ is NOT NULL LIMIT " + ACT_INST_TABLE_MAX_ROWS + ")";
    private static final String SQL_CLEAN_CAMUNDA_HISTORY_PROC_INST_TABLE = "DELETE FROM act_hi_procinst c1 WHERE c1.ctid IN (SELECT ctid FROM act_hi_procinst c WHERE c.proc_def_key_ LIKE :pProcessDefinitionKey AND c.end_time_ is NOT NULL LIMIT " + PROC_INST_TABLE_MAX_ROWS + ")";


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
     * Clean up internal Flow historic job logs.
     *
     * @param processDefinitionKey is the internal flow Id
     */
    public void cleanUpInternalFlowHistoricJobLogs(final String processDefinitionKey) {
        LOGGER.debug("Cleaning up HistoricJobLogs for internal flow: [{}]", processDefinitionKey);
        final List<String> internalFlowHistoricProcessInstanceIds = getHistoricProcessInstanceIds(processDefinitionKey);
        if (internalFlowHistoricProcessInstanceIds.isEmpty()) {
            return;
        }
        final Set<String> internalFlowRuntimeJobIds = new HashSet<>();
        final List<Job> totalInternalFlowRuntimeJobs = new ArrayList<>();
        internalFlowHistoricProcessInstanceIds.stream().forEach(internalFlowHistoricProcessInstanceId -> {
            final List<Job> internalFlowJobs = processEngine.getManagementService().createJobQuery()
                    .processInstanceId(internalFlowHistoricProcessInstanceId).list();
            totalInternalFlowRuntimeJobs.addAll(internalFlowJobs);
        });
        totalInternalFlowRuntimeJobs.stream().forEach(internalFlowJob -> internalFlowRuntimeJobIds.add(internalFlowJob.getId()));

        final Set<String> internalFlowHistoricJobIds = new HashSet<>();
        final List<HistoricJobLog> totalHistoricJobLog = new ArrayList<>();
        internalFlowHistoricProcessInstanceIds.stream().forEach(internalFlowHistoricProcessInstanceId -> totalHistoricJobLog.addAll(
                processEngine.getHistoryService().createHistoricJobLogQuery().processInstanceId(internalFlowHistoricProcessInstanceId).list()));
        totalHistoricJobLog.stream().forEach(historicJobLog -> internalFlowHistoricJobIds.add(historicJobLog.getJobId()));
        internalFlowHistoricJobIds.removeAll(internalFlowRuntimeJobIds);

        if (!internalFlowHistoricJobIds.isEmpty()) {
            final AtomicInteger counter = new AtomicInteger(-1);
            final Collection<List<String>> result = internalFlowHistoricJobIds.stream()
                    .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / 1000))
                    .values();
            LOGGER.debug("Number of Batches in historicFlowJobIds: [{}]", result.size());
            for (List<String> innerList : result) {
                deleteHistoricJobLogs(innerList, processDefinitionKey);
            }
        }
    }


    /**
     * Clean up internal flow historic task instances.
     *
     * @param processDefinitionKey the process definition key
     */
    public void cleanUpinternalFlowHistoricTaskInstances(final String processDefinitionKey) {
        LOGGER.debug("Cleaning up HistoricTaskInstances for internal flow: [{}]",processDefinitionKey);
        if (!processDefinitionKey.equals(HOUSE_KEEPING_FLOW_ID_PATTERN)) {
            return;
        }
        final List<String> internalFlowHistoricProcessInstanceIds = getHistoricProcessInstanceIds(processDefinitionKey);
        if (internalFlowHistoricProcessInstanceIds.isEmpty()) {
            return;
        }
        final List<HistoricTaskInstance> totalHistoricTaskInstances = new ArrayList<>();
        internalFlowHistoricProcessInstanceIds.stream().forEach(internalFlowHistoricProcessInstanceId -> {
            final List<HistoricTaskInstance> historicTaskInstances = processEngine.getHistoryService().createHistoricTaskInstanceQuery()
                    .processInstanceId(internalFlowHistoricProcessInstanceId).finished().list();
            if (!historicTaskInstances.isEmpty()) {
                totalHistoricTaskInstances.addAll(historicTaskInstances);
            }

        });

        totalHistoricTaskInstances.stream().forEach(
                totalHistoricTaskInstance -> processEngine.getHistoryService().deleteHistoricTaskInstance(totalHistoricTaskInstance.getId()));
        LOGGER.debug("Cleaned up HistoricTaskInstances for internal flow: [{}], result: [{}]", processDefinitionKey, totalHistoricTaskInstances.size());
    }

    /**
     * Clean up internal flow historic variable instances.
     *
     * @param processDefinitionKey is the internal flow Id
     */
    public void cleanUpInternalFlowHistoricVariableInstances(String processDefinitionKey) {
        LOGGER.debug("Cleaning up HistoricVariableInstances for internal flow: [{}]", processDefinitionKey);
        final List<String> internalFlowHistoricProcessInstanceIds = getHistoricProcessInstanceIds(processDefinitionKey);
        if (internalFlowHistoricProcessInstanceIds.isEmpty()) {
            return;
        }
        final List<Execution> internalFlowRunTimeExecutions = new ArrayList<>();
        final List<String> internalFlowRunTimeExecutionIds = new ArrayList<>();
        internalFlowHistoricProcessInstanceIds.forEach(internalFlowHistoricProcessInstanceId -> internalFlowRunTimeExecutions
                .addAll(processEngine.getRuntimeService().createExecutionQuery().processInstanceId(internalFlowHistoricProcessInstanceId).list()));
        internalFlowRunTimeExecutions
                .forEach(internalFlowRunTimeExecution -> internalFlowRunTimeExecutionIds.add(internalFlowRunTimeExecution.getId()));

        final List<String> internalFlowHistoricExecutionIds = new ArrayList<>();
        final List<HistoricVariableInstance> internalFlowHistoricVariableInstances = processEngine.getHistoryService()
                .createHistoricVariableInstanceQuery().processInstanceIdIn(internalFlowHistoricProcessInstanceIds.toArray(new String[0])).list();
        internalFlowHistoricVariableInstances.forEach(
                internalFlowHistoricVariableInstance -> internalFlowHistoricExecutionIds.add(internalFlowHistoricVariableInstance.getExecutionId()));
        internalFlowHistoricExecutionIds.removeAll(internalFlowRunTimeExecutionIds);

        if (!internalFlowHistoricExecutionIds.isEmpty()) {
            final AtomicInteger counter = new AtomicInteger(-1);
            final Collection<List<String>> result = internalFlowHistoricExecutionIds.stream()
                    .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / 1000))
                    .values();
            LOGGER.debug("Number of batches of HistoricVariableInstances for internal flow: [{}], batchSize: [{}]", processDefinitionKey, result.size());
            for (List<String> innerList : result) {
                deleteHistoricVariableInstances(innerList,processDefinitionKey);
            }
        }
    }

    /**
     * Clean up internal flow historic activity instances.
     *
     * @param processDefinitionKey the process definition key
     */
    public void cleanUpInternalFlowHistoricActivityInstances(final String processDefinitionKey) {
        LOGGER.debug("Cleaning up  HistoricActivityInstances for internal flow: [{}]",processDefinitionKey);
        final Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(P_PROCESS_DEFINITION_KEY, processDefinitionKey);
        int result = 0;
        int partialResult;
        do {
            partialResult = flowExecutionRepository.executeNativeQuery(SQL_CLEAN_CAMUNDA_HISTORY_PROC_INST_TABLE, queryParams);
            result += partialResult;
        } while (partialResult == ACT_INST_TABLE_MAX_ROWS);
        LOGGER.debug("Cleaned up HistoricActivityInstances for internal flow: [{}], result: [{}]", processDefinitionKey, result);
    }

    /**
     * Clean up internal flow historic process instances.
     *
     * @param processDefinitionKey the process definition key
     */
    public void cleanUpInternalFlowHistoricProcessInstances(final String processDefinitionKey) {
        LOGGER.debug("Cleaning up HistoricProcessInstances for internal flow: [{}]", processDefinitionKey);
        final Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(P_PROCESS_DEFINITION_KEY, processDefinitionKey);
        int result = 0;
        int partialResult;
        do {
            partialResult = flowExecutionRepository.executeNativeQuery(SQL_CLEAN_CAMUNDA_HISTORY_PROC_INST_TABLE, queryParams);
            result += partialResult;
        } while (partialResult == PROC_INST_TABLE_MAX_ROWS);
        LOGGER.debug("Cleaned up HistoricProcessInstances for internal flow: [{}], result: [{}]", processDefinitionKey, result);
    }

    /**
     * Get flow byte array variables collection.
     *
     * @return the collection
     */
    public Collection<List<String>> getFlowByteArrayVariables(){
        LOGGER.debug("Getting ids from byte array table for deletion");
        List<String> flowInstanceVariableIds = new ArrayList<>();
        flowInstanceVariableIds.add("parentProcessInstanceIdsMarkedAsStop");
        flowInstanceVariableIds.add("parentProcessInstanceIdsMarkedAsStopping");
        flowInstanceVariableIds.add("flowExecutionIdsMarkedAsStopping");
        flowInstanceVariableIds.add("processInstanceIdsToBeSuspended");
        flowInstanceVariableIds.add("suspendedFlowExecutionIds");
        flowInstanceVariableIds.add("incidents");
        Class clazz = org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity.class;

        String queryParam = getQueryParam(flowInstanceVariableIds);
        final String sqlQuery = "SELECT id_ FROM act_ge_bytearray c WHERE name_ IN (" + queryParam + ")";
        LOGGER.debug("Query params size: [{}], query: [{}]", flowInstanceVariableIds.size(),sqlQuery);

        final List<String> result = flowExecutionRepository.executeNativeSelectQuery(sqlQuery);
        LOGGER.debug("Number of byte array variables :[{}]", (result != null)? result.size():0);

        if(result != null && !result.isEmpty()){
            String[] ids = new String[flowInstanceVariableIds.size()];
            ids = flowInstanceVariableIds.toArray(ids);
            List<VariableInstance> variableInstances = processEngine.getRuntimeService().createVariableInstanceQuery().variableNameIn(ids).list();
            final List<String> runtimeByteArrayIds = new ArrayList<>();
            for(VariableInstance  variableInstance: variableInstances){
                HasDbReferences hasDbReferences = (HasDbReferences)variableInstance;
                Map<String,Class> referencedEntitiesIdAndClass =  hasDbReferences.getReferencedEntitiesIdAndClass();
                referencedEntitiesIdAndClass.entrySet().stream().filter(idClassEntry -> idClassEntry.getValue().equals(clazz)).forEach(idClassEntry -> runtimeByteArrayIds.add(idClassEntry.getKey()));
            }

            LOGGER.debug("Run time byte array ids :[{}]", runtimeByteArrayIds);
            result.removeAll(runtimeByteArrayIds);

            final AtomicInteger counter = new AtomicInteger(-1);
            final Collection<List<String>>  byteArrayBatch = result.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / 10)).values();
            LOGGER.debug("Number of byte array batches :[{}]", byteArrayBatch.size());
            return byteArrayBatch;
        }
        return Collections.emptyList();
    }

    /**
     * Clean up internal flow byte array variables.
     *
     * @param byteArrayIds the byte array ids
     */
    public void cleanUpInternalFlowByteArrayVariables(List<String> byteArrayIds){
        LOGGER.debug("Cleaning up byte array table for internal flow: {}", byteArrayIds);
        String queryParam = getQueryParam(byteArrayIds);
        final String sqlQuery = "DELETE FROM act_ge_bytearray c WHERE c.id_ in (" + queryParam + ")";
        LOGGER.debug("Size of Query params: {}", byteArrayIds.size());
        LOGGER.debug("SQL Query: {}", sqlQuery);
        final int result = flowExecutionRepository.executeNativeQuery(sqlQuery);
        LOGGER.debug("Cleaned up byte array table for internal flow :[{}]", result);
    }

    private List<String> getHistoricProcessInstanceIds(String processDefinitionKey) {
        final List<HistoricProcessInstance> historicProcessInstances = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceBusinessKeyLike(processDefinitionKey).list();

        if (historicProcessInstances.isEmpty()) {
            LOGGER.warn("No HistoricProcessInstances found for {}", processDefinitionKey);
            return Collections.emptyList();
        }
        final String flowName = processDefinitionKey.substring(processDefinitionKey.lastIndexOf('.')+1,processDefinitionKey.length()-1);
        LOGGER.debug("Cleaning up {} data..", flowName);
        final List<String> historicProcessInstanceIds = new ArrayList<>();
        historicProcessInstances.stream().forEach(
                houseKeepingHistoricProcessInstance -> historicProcessInstanceIds.add(houseKeepingHistoricProcessInstance.getId()));

        return historicProcessInstanceIds;
    }

    private void deleteHistoricVariableInstances(List<String> internalFlowHistoricExecutionIds,String processDefinitionKey) {
        String queryParam = getQueryParam(internalFlowHistoricExecutionIds);
        LOGGER.debug("Query params size: {}", internalFlowHistoricExecutionIds.size());
        final String sqlQuery = "DELETE FROM act_hi_varinst c WHERE c.execution_id_ IN (" + queryParam + ")";
        final int result = flowExecutionRepository.executeNativeQuery(sqlQuery);
        LOGGER.debug("Cleaned up HistoricVariableInstances for internal flow: [{}], result: [{}]", processDefinitionKey, result);
    }

    private String getQueryParam(List<String> parameterIds) {
        final StringBuilder queryParamBuilder = new StringBuilder();
        parameterIds.forEach(parameterId -> {
            queryParamBuilder.append("'");
            queryParamBuilder.append(parameterId);
            queryParamBuilder.append("'");
            queryParamBuilder.append(",");
        });

        String queryparam = queryParamBuilder.toString();
        queryparam = queryparam.substring(0, queryparam.length() - 1);
        return queryparam;
    }

    private void deleteHistoricJobLogs(List<String> internalFlowHistoricJobIds, String processDefinitionKey) {
        String queryParam = getQueryParam(internalFlowHistoricJobIds);

        //jpa 2.0 does not support setting collection for named binding in IN clause (:houseKeepingHistoricJobIds). jpa 2.1 supports this functionality.
        final String baseQuery = "DELETE FROM act_hi_job_log c WHERE c.process_def_key_ LIKE ";
        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(baseQuery);
        queryBuilder.append("'");
        queryBuilder.append(processDefinitionKey);
        queryBuilder.append("'");
        queryBuilder.append(" AND c.job_id_ IN ");
        queryBuilder.append("(");
        queryBuilder.append(queryParam);
        queryBuilder.append(")");

        final String sqlQuery = queryBuilder.toString();
        LOGGER.debug("Query params size: {}", internalFlowHistoricJobIds.size());
        LOGGER.debug("SQL Query: {}", sqlQuery);
        final int result = flowExecutionRepository.executeNativeQuery(sqlQuery);
        LOGGER.debug("Cleaned up HistoricJobLogs for internal flow :[{}], result :[{}]", processDefinitionKey, result);

    }
}
