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

package com.ericsson.oss.services.flowautomation.flow;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.entities.FlowDetailEntity;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEventEntity;
import com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode;
import com.ericsson.oss.services.flowautomation.exception.EntityNotFoundException;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.exception.UserTaskBackException;
import com.ericsson.oss.services.flowautomation.execution.event.api.FlowEventRecorderService;
import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.execution.resource.FlowExecutionResourceGenerator;
import com.ericsson.oss.services.flowautomation.execution.resource.FlowExecutionResourceTypeLiteral;
import com.ericsson.oss.services.flowautomation.execution.resource.ReportVariableGenerator;

import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowServiceException;
import com.ericsson.oss.services.flowautomation.flow.exception.InvalidPayloadException;
import com.ericsson.oss.services.flowautomation.flow.exception.OperationNotAllowedException;
import com.ericsson.oss.services.flowautomation.flow.setting.FlowInputSchemaAndDataBuilder;

import com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskFormKeyResolver;
import com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskHelper;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.FASchema;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder.FlowExecutionReportBuilder;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.factory.UserTaskSchemaFactory;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor.ReadOnlyUserTaskProcessor;
import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor.SchemaProcessorReport;
import com.ericsson.oss.services.flowautomation.flow.utils.*;
import com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows;
import com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil;
import com.ericsson.oss.services.flowautomation.model.*;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionEventRepository;
import com.ericsson.oss.services.flowautomation.repo.ReportVariableRepository;
import com.ericsson.oss.services.flowautomation.resources.UserTaskFormKeyResolverType;
import org.apache.commons.collections4.MapUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.*;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.DELETE_FLOW_EXECUTION_IS_NOT_ALLOWED;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_EXECUTION_SETUP_INPUT_FILE_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_EXECUTION_SETUP_MORE_THAN_ONE_FILE_UPLOADED;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_INPUT_FILE_NOT_UPLOADED;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_SETUP_PHASE_NOT_AVAILABLE;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.INVALID_INPUT_FILE_TYPE_UPLOADED;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.TASK_IS_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowErrorCode.FLOW_VERSION_IS_ACTIVE;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_DOES_NOT_SUPPORT_BACK;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_INPUT_FILE_PARSE_ERROR;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_INVALID_BACK_TARGET;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_MODIFICATION_ERROR;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_FLOW_EXECUTION_DELETE;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_FLOW_EXECUTION_PHASE_FOR_STOP;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_FLOW_EXECUTION_PHASE_FOR_SUSPEND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_FLOW_INPUT;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_RESOURCE_DOWNLOAD_REQUEST;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.TASK_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.SETUP_PHASE_NOT_AVAILABLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskHelper.FAINTERNAL_TASK_KEY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.EMPTY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.INTERACTIVE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.NAME_EXTRA;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROPERTIES;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SETUP_TYPE;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowAutomationServiceUtil.convertMilliSeconds;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.SETTING_UP;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.STOP;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.ExecutionStatus.State.SUSPENDED;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.CHOOSE_SETUP_USER_TASK;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.FA_INTERNAL_USER_SETUP;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.FLOW_INPUT;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement.REVIEW_AND_CONFIRM_EXECUTE_USER_TASK;
import static com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowTaskExecutionListener.FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME;
import static com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil.DATE_TIME_FORMATTER_SQL;
import static com.ericsson.oss.services.flowautomation.jpa.DateTimeUtil.DATE_TIME_FORMATTER_WITHOUT_TZ;
import static com.ericsson.oss.services.flowautomation.model.UserTask.FA_NAME_EXTRA;
import static com.ericsson.oss.services.flowautomation.model.UserTaskStatus.ACTIVE;
import static com.ericsson.oss.services.flowautomation.model.UserTaskStatus.COMPLETED;
import static com.ericsson.oss.services.flowautomation.resources.UserTaskFormKeyResolverType.Value.SETUP_PHASE_USER_TASK_FORM_KEY_RESOLVER;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;


/**
 * The type Flow execution service.
 */
@ServiceType(JAR_TYPE)
public class FlowExecutionServiceImpl extends AbstractFlowAutomationService implements FlowExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowExecutionServiceImpl.class);
    private static final String FLOW_INPUT_FILE = "flow-input.json";

    private static final String FA_INTERNAL_BACK_USERTASK_DEFINITION_KEYS = "FAInternalBackUsertaskDefinitionKeys";

    /**
     * The Report variable repository.
     */
    @Inject
    ReportVariableRepository reportVariableRepository;
    /**
     * The User task schema factory.
     */
    @Inject
    UserTaskSchemaFactory userTaskSchemaFactory;

    @Inject
    private FlowExecutionEventRepository flowExecutionEventRepository;
    @Inject
    private FlowExecutionReportBuilder flowExecutionReportBuilder;

    @Inject
    private FlowInputSchemaAndDataBuilder flowInputSchemaAndDataBuilder;

    @Any
    @Inject
    private Instance<FlowExecutionResourceGenerator> resourceGenerators;

    @Inject
    private FlowExecutionPhase flowExecutionPhase;

    @Inject
    ReadOnlyUserTaskProcessor readOnlyUserTaskProcessor;

    @Inject
    @ServiceType(ServiceType.Value.JAR_TYPE)
    FlowEventRecorderService flowEventRecorderService;

    /** The user task helper. */
    @Inject
    UserTaskHelper userTaskHelper;

    @Inject
    @UserTaskFormKeyResolverType(SETUP_PHASE_USER_TASK_FORM_KEY_RESOLVER)
    UserTaskFormKeyResolver userTaskFormKeyResolver;

    @Override
    public List<FlowExecution> getExecutions(final FlowExecutionFilter filter) {
        return flowExecutionRepository.getFlowExecutions(filter).stream().map(this::getFlowExecution).collect(Collectors.toList());
    }

    @Override
    public List<UserTask> getUserTasks(final String flowId, final String flowExecutionName) {
        validateFlowId(flowId);
        final FlowExecutionEntity flowExecutionEntity;
        flowExecutionEntity = flowExecutionRepository.getFlowExecution(flowId, flowExecutionName);

        final FlowBusinessKey flowBusinessKey = new FlowBusinessKey(flowId, flowExecutionName, flowExecutionEntity.getId().toString());
        final String businessKey = flowBusinessKey.getCamundaBusinessKey();
        final List<Task> activeTasks = processEngine.getTaskService().createTaskQuery().processInstanceBusinessKey(businessKey).active()
                .initializeFormKeys().list();

        final String executionPhase = flowExecutionPhase.getExecutionPhase(flowExecutionEntity.getProcessInstanceId());
        final List<UserTask> activeTasksMap = createActiveUserTasks(activeTasks, flowExecutionEntity, executionPhase);

        if (flowExecutionPhase.isSetupPhase(executionPhase)) { // Include the completed usertasks in response if in setup phase
            final List<HistoricTaskInstance> completedTasks = processEngine.getHistoryService().createHistoricTaskInstanceQuery()
                    .processInstanceBusinessKey(businessKey).finished().orderByHistoricTaskInstanceEndTime().asc().list(); //all camunda usertasks

            final List<UserTask> allUserTasks = mapAllTheHistoricTaskIntancesToUserTasks(completedTasks);
            final Set<UserTask> nonCompletedUserTasks = getAllNonCompletedUserTasks(allUserTasks);

            final Set<UserTask> filteredUserTasks = filterUserTasks(allUserTasks, activeTasksMap, nonCompletedUserTasks);
            return Stream.concat(filteredUserTasks.stream(), activeTasksMap.stream())
                    .collect(Collectors.toList());
        }
        return activeTasksMap;
    }

    /**
     * Maps all the camunda {@code HistoricTaskInstance} to {@code UserTask} instances.
     *
     * @param completedTasks the historic tasks
     * @return the collection of previously completed {@code UserTask} instances
     */
    private List<UserTask> mapAllTheHistoricTaskIntancesToUserTasks(final List<HistoricTaskInstance> completedTasks) {
        return completedTasks.stream() // All usertasks mapped to fa usertasks
                .map(task -> new UserTask(task.getId(),
                        task.getName(),
                        userTaskHelper.getNameExtraForCompletedTask(task),
                        task.getProcessDefinitionId(),
                        task.getTaskDefinitionKey(),
                        UserTaskStatus.valueFrom(task.getDeleteReason()),
                        task.getEndTime()))
                .collect(Collectors.toList());
    }

    /**
     * Returns all the usertasks that were there in the history but were not completed. e.g the ones which were canceled due to process instance modification.
     * It does it making sure to collect the last non completed ones. even if the same task is non completed multiple times.. back and forth execution.
     *
     * @param allUserTasks all the usertasks from history
     * @return the non completed usertasks
     */
    private Set<UserTask> getAllNonCompletedUserTasks(final List<UserTask> allUserTasks) {
        final Set<UserTask> nonCompletedUserTasks = new LinkedHashSet<>();
        allUserTasks.stream()
                .filter(userTask -> !COMPLETED.getValue().equals(userTask.getStatus())) // non completed
                .forEach(userTask -> {
                    nonCompletedUserTasks.remove(userTask);
                    nonCompletedUserTasks.add(userTask);
                });
        return nonCompletedUserTasks;
    }

    /**
     * Returns the unique list of usertasks by filtering out
     * 1) any non completed usertasks,
     * 2) the tasks which are currently active,
     * 3) the tasks which were completed first and then were canceled due to process instance modification.
     *
     * @param completedUserTasks    the completed usertasks
     * @param activeUserTasks       the active usertasks
     * @param nonCompletedUserTasks the noncompleted or canceled usertasks due to process instance modification.
     * @return the unique list of historic tasks
     */
    private Set<UserTask> filterUserTasks(final List<UserTask> completedUserTasks, final List<UserTask> activeUserTasks, final Set<UserTask> nonCompletedUserTasks) {
        final Set<UserTask> filteredUsertasks = new LinkedHashSet<>();
        completedUserTasks.forEach(task -> {
            final Optional<UserTask> usertask = nonCompletedUserTasks.stream().filter(nTask -> nTask.equals(task)).findFirst();
            boolean canceledOrPreviousCompletedTask = false;
            if (usertask.isPresent()) {
                //either task was not completed or was completed before its non completion/ canceled due to process instance modification.
                canceledOrPreviousCompletedTask = !COMPLETED.getValue().equals(task.getStatus()) || task.getEndTime().before(usertask.get().getEndTime());
            }

            if (!canceledOrPreviousCompletedTask && !activeUserTasks.contains(task)) {
                filteredUsertasks.remove(task);
                filteredUsertasks.add(task); //filter any duplicates, take the latest always so remove and add as the same task might have been completed multiple times.
            }
        });
        return filteredUsertasks;
    }

    @Override
    public void completeUserTask(final String taskId, final Map<String, Object> variables) {
        final Task task = processEngine.getTaskService().createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            LOGGER.debug("User task with id: {} not found.", taskId);
            throw new EntityNotFoundException(TASK_NOT_FOUND, TASK_IS_NOT_FOUND);
        }
        if (CHOOSE_SETUP_USER_TASK.getId().equals(task.getTaskDefinitionKey())) {
            variables.put(FA_INTERNAL_USER_SETUP.getName(), true);
        }

        if (MapUtils.isEmpty(variables)) {
            processEngine.getTaskService().complete(taskId);
        } else {
            processEngine.getTaskService().complete(taskId, variables);
        }
    }

    @Override
    public void completeUserTask(final String taskId, final Map<String, byte[]> userTaskFileInput, final String usertTaskInput) {
        LOGGER.debug("Completing user task: {}", taskId);
        final Task task = getUserTask(taskId);
        final String processInstanceId = flowAutomationBpmnEngineUtil.getParentProcessInstanceId(task.getProcessInstanceId());
        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.getFlowExecution(processInstanceId);
        final String executionPhase = flowExecutionPhase.getExecutionPhase(processInstanceId);

        try {
            if (task.getTaskDefinitionKey().equals(CHOOSE_SETUP_USER_TASK.getId())) {
                final FASchema faSchema = userTaskSchemaFactory.buildSchema(flowExecutionEntity, executionPhase, task);
                String userTaskJsonSchema = null;
                if (faSchema != null) {
                    userTaskJsonSchema = faSchema.toString();
                }
                final Map<String, Object> userTaskVariables = PayloadTransformer.transformJsonPayloadToMap(userTaskJsonSchema, usertTaskInput);
                final Map<String, Object> setupType = (Map<String, Object>) userTaskVariables.get(SETUP_TYPE);
                final Object interactive = setupType.get(INTERACTIVE);
                if (interactive != null && (boolean) interactive) {
                    completeInteractiveSetupUserTask(taskId, task);
                } else {
                    handleNonIntractiveSetupUserTask(taskId, userTaskFileInput, task);
                }
            } else if (task.getTaskDefinitionKey().equals(REVIEW_AND_CONFIRM_EXECUTE_USER_TASK.getId())) {
                processEngine.getRuntimeService().setVariable(processInstanceId, "FAReviewAndConfirmExecuted", true);
                processEngine.getTaskService().complete(taskId);
                LOGGER.debug("Completed the review and confirm user task: {}", taskId);
            } else {
                completeGeneralUserTask(flowExecutionEntity, executionPhase, taskId, userTaskFileInput, usertTaskInput, task);
            }
        } catch (final InvalidPayloadException e) {
            LOGGER.error("completeUserTask() Failed to process: {}", e.getMessage());
            throw new FlowServiceException(e.getErrorCode(), e.getErrorDetail(), e.getCause());
        }
    }

    @Override
    public UserTaskSchema getUserTaskSchema(final String taskId) {
        LOGGER.debug("Get schema for user task: {}", taskId);
        final Task task = getUserTask(taskId);
        final String processInstanceId = flowAutomationBpmnEngineUtil.getParentProcessInstanceId(task.getProcessInstanceId());
        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.getFlowExecution(processInstanceId);
        final String executionPhase = flowExecutionPhase.getExecutionPhase(processInstanceId);
        final FASchema faSchema = userTaskSchemaFactory.buildSchema(flowExecutionEntity, executionPhase, task);
        String userTaskJsonSchema = null;
        if (faSchema != null) {
            SchemaProcessorReport report = faSchema.getReport();
            if(report != null && !report.isSuccess()) {
                report.recordErrors(flowEventRecorderService, flowExecutionEntity.getProcessInstanceBusinessKey());
            }

            userTaskJsonSchema = faSchema.toString();
        }
        return new UserTaskSchema(task.getId(), task.getName(), task.getProcessDefinitionId(), task.getTaskDefinitionKey(),
                ACTIVE, userTaskJsonSchema);
    }

    @Override
    public void deleteExecution(final String flowId, final String flowExecutionName) {
        validateFlowId(flowId);
        final FlowExecutionEntity flowExecution = flowExecutionRepository.getFlowExecution(flowId, flowExecutionName);
        final String processInstanceId = flowExecution.getProcessInstanceId();
        if (!flowExecutionPhase.isExecutionDeletable(processInstanceId)) {
            LOGGER.debug("Deletion of execution: {} with flowId is not allowed: {}", flowId, flowExecutionName);
            throw new OperationNotAllowedException(INVALID_FLOW_EXECUTION_DELETE,
                    format(DELETE_FLOW_EXECUTION_IS_NOT_ALLOWED, flowId, flowExecutionName));
        }

        final String businessKeyId = flowExecution.getId().toString();
        final FlowBusinessKey flowBusinessKey = new FlowBusinessKey(flowId, flowExecutionName, businessKeyId);
        final String businessKey = flowBusinessKey.getCamundaBusinessKey();
        final RuntimeService runtimeService = processEngine.getRuntimeService();

        // 1. delete from Flow Automation tables
        flowExecutionRepository.delete(flowExecution);

        // 2. delete from camunda runtime table only if it exists, otherwise the exception is through from camunda and it also suspends the transaction.
        final List<ProcessInstance> processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey)
                .rootProcessInstances().list();
        processInstance
                .forEach(record -> runtimeService.deleteProcessInstance(record.getProcessInstanceId(), "Delete User Action", true, true, true));

        // 3. delete from camunda history table.
        final HistoryService historyService = processEngine.getHistoryService();
        final List<HistoricProcessInstance> procesInstanceRecords = historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey).list();
        procesInstanceRecords.forEach(record -> historyService.deleteHistoricProcessInstance(record.getId()));
    }

    @Override
    public String getExecutionReport(final String flowId, final String flowExecutionName) {
        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.getFlowExecution(flowId, flowExecutionName);
        return flowExecutionReportBuilder.getFlowExecutionReport(flowExecutionEntity, getFlowExecution(flowExecutionEntity));
    }

    @Override
    public String getExecutionReportSchema(final String flowId, final String flowExecutionName) {
        validateFlowId(flowId);
        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.getFlowExecution(flowId, flowExecutionName);
        return flowExecutionReportBuilder.getFlowExecutionReportSchema(flowExecutionEntity);
    }

    @Override
    public void suspendExecution(final String flowId, final String flowExecutionName) {
        validateFlowId(flowId);
        final FlowExecutionEntity flowExecution = flowExecutionRepository.getFlowExecution(flowId, flowExecutionName);
        suspendExecution(flowExecution);
    }

    @Override
    public void suspendAllExecutionsForFlowVersion(final String flowId, final String flowVersion) {
        validateFlowId(flowId);
        validateFlowVersion(flowId, flowVersion);
        if (flowDetailRepository.isFlowVersionActive(flowId, flowVersion)) {
            LOGGER.debug("Suspension of flow: {} with version: {} is not allowed, as the flow version is active.", flowId, flowVersion);
            throw new OperationNotAllowedException(FLOW_VERSION_IS_ACTIVE,
                    "The suspend on flow version is only allowed if the flow version is inactive");
        }
        LOGGER.info("Suspending all flow executions for flow:[{}.{}]", flowId, flowVersion);
        final List<FlowExecutionEntity> executions = flowExecutionRepository
                .getFlowExecutions(new FlowExecutionFilterBuilder().flowId(flowId).flowVersion(flowVersion).build());

        executions.forEach(execution -> {
            try {
                suspendExecution(execution);
            } catch (final OperationNotAllowedException e) {
                LOGGER.warn("Cannot suspend the instance: {}", e.getMessage());
                throw e;
            } catch (final FlowAutomationException e) {
                LOGGER.warn("Cannot suspend the instance: {}", e.getMessage());
                throw new FlowAutomationException("Cannot suspend the instance: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public void stopExecution(final String flowId, final String flowExecutionName) {
        final FlowExecutionEntity flowExecution = flowExecutionRepository.getFlowExecution(flowId, flowExecutionName);
        final String flowExecutionId = flowExecution.getProcessInstanceId();
        if (!FlowAutomationInternalFlows.isInternalFlow(flowId)) {
            checkFlowExecutionStoppable(flowExecutionId);
        }
        final RuntimeService runtimeService = processEngine.getRuntimeService();
        try {
            final ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(flowExecutionId).singleResult();
            if (processInstance != null) {
                runtimeService.setVariable(flowExecutionId, FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME, STOP.name());
                LOGGER.info("Requested to stop the flow id: {}, flow execution: {} by user: {}", flowId, flowExecutionName, getUserInContext());
            }
        } catch (final ProcessEngineException e) {
            LOGGER.error("Failed to set the status of the execution:{}, Exception: {}", flowExecution, e.getMessage());
            if (!FlowAutomationInternalFlows.isInternalFlow(flowId)) {
                checkFlowExecutionStoppable(flowExecutionId);
            }
            throw new FlowAutomationException("Failed to stop the flow execution: " + flowId + "." + flowExecutionName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String getFlowInputSchemaAndData(final String flowId, final String flowExecutionName) {
        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.getFlowExecution(flowId, flowExecutionName);
        final FlowDetailEntity flowDetailEntity = flowExecutionEntity.getFlowDetailEntity();
        final String version = flowDetailEntity.getVersion();

        if (Objects.isNull(flowDetailEntity.getSetupId())) {
            LOGGER.debug("The flow with id: {}, version: {} does not have setup phase", flowId, version);
            throw new FlowResourceNotFoundException(SETUP_PHASE_NOT_AVAILABLE, format(FLOW_SETUP_PHASE_NOT_AVAILABLE, flowId, version));
        }

        return flowInputSchemaAndDataBuilder.getSerializedFlowInputSchemaWithData(flowExecutionEntity);
    }

    @Override
    public FlowExecutionResource getExecutionResource(final String flowId, final String flowExecutionName, final String resource) {
        validateFlowId(flowId);
        final Instance<FlowExecutionResourceGenerator> selectedInstance = resourceGenerators.select(new FlowExecutionResourceTypeLiteral(resource));
        if (selectedInstance.isUnsatisfied()) {
            LOGGER.info("No FlowExecutionResourceGenerator implementation found to generate resource: {} for the execution: [{}/{}]", resource, flowId,
                    flowExecutionName);
            throw new FlowResourceNotFoundException(INVALID_RESOURCE_DOWNLOAD_REQUEST,
                    String.format("Invalid resource request [%s] for the flow execution [%s/%s].", resource, flowId, flowExecutionName));
        }
        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.getFlowExecution(flowId, flowExecutionName);
        return selectedInstance.get().generate(flowExecutionEntity, getFlowExecution(flowExecutionEntity));
    }

    @Override
    public PaginatedData<FlowExecutionEvent> getExecutionEvents(final FlowExecutionEventFilter flowExecutionEventFilter) {
        flowExecutionRepository.getFlowExecution(flowExecutionEventFilter.getFlowId(), flowExecutionEventFilter.getFlowExecutionName());

        final List<FlowExecutionEventEntity> executionEventEntities = flowExecutionEventRepository.findByFilter(flowExecutionEventFilter);

        final List<FlowExecutionEvent> events = executionEventEntities.stream()
                .map(eventEntity -> new FlowExecutionEvent(eventEntity.getEventTime().toString(),
                        eventEntity.getSeverity().name(),
                        eventEntity.getTarget(),
                        eventEntity.getMessage(),
                        eventEntity.getEventData()))
                .collect(Collectors.toList());

        final long eventsCount = flowExecutionEventRepository.getEventsCount(flowExecutionEventFilter);

        return new PaginatedData<>(eventsCount, events);
    }

    @Override
    public FlowExecutionResource getExecutionReportVariable(final String flowId, final String flowExecutionName, final String variableName) {
        validateFlowId(flowId);
        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.getFlowExecution(flowId, flowExecutionName);

        final ReportVariableGenerator reportVariableGenerator = resourceGenerators.select(ReportVariableGenerator.class).get();
        return reportVariableGenerator.generate(flowExecutionEntity, variableName);
    }

    @Override
    public void revertUserTask(final String taskId) {
        final Task task = getUserTask(taskId);
        final String processInstanceId = flowAutomationBpmnEngineUtil.getParentProcessInstanceId(task.getProcessInstanceId());
        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.getFlowExecution(processInstanceId);
        if (!flowExecutionEntity.getFlowDetailEntity().isBackEnabled()) {
            throw new UserTaskBackException(FLOW_DOES_NOT_SUPPORT_BACK);
        }
        final String userTaskBackTarget = userTaskHelper.getUserTaskBackTarget(flowExecutionEntity.getProcessInstanceId(), task)
                .orElseThrow(() -> new UserTaskBackException(FLOW_EXECUTION_INVALID_BACK_TARGET));

        setEmptyOutputParameterForUserTask(task); //This is required as camunda does not allow to modifiy process instance without resolving output parameters of current usertask.
        clearPreviouslySubmittedDataForUserTask(task);
        setFlowInputPreviousState(task);

        if (Objects.equals(REVIEW_AND_CONFIRM_EXECUTE_USER_TASK.getId(), task.getTaskDefinitionKey())) { // If going back from review and confirm.
            processEngine.getRuntimeService().setVariable(flowExecutionEntity.getProcessInstanceId(), FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME, SETTING_UP.name());
        }
        try {
            String faInternalBackUsertaskDefinitionKeys = "";
            final Object faInternalBackUsertaskDefinitionKeysObj = processEngine.getRuntimeService().getVariable(task.getProcessInstanceId(), FA_INTERNAL_BACK_USERTASK_DEFINITION_KEYS);
            if (faInternalBackUsertaskDefinitionKeysObj != null) {
                faInternalBackUsertaskDefinitionKeys = (String)faInternalBackUsertaskDefinitionKeysObj + ",";
            }
            faInternalBackUsertaskDefinitionKeys += task.getTaskDefinitionKey();
            processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), FA_INTERNAL_BACK_USERTASK_DEFINITION_KEYS, faInternalBackUsertaskDefinitionKeys);

            processEngine.getRuntimeService()
                    .createProcessInstanceModification(task.getProcessInstanceId())
                    .cancelAllForActivity(task.getTaskDefinitionKey())
                    .startBeforeActivity(userTaskBackTarget)
                    .execute();
        } catch (final ProcessEngineException e) {
            LOGGER.warn("Failed to modify the process instance for going back to usertask: {} from usertask: {}. Exception: {}", userTaskBackTarget, task.getTaskDefinitionKey(), e.getMessage());
            throw new UserTaskBackException(FLOW_EXECUTION_MODIFICATION_ERROR, e);
        }
    }

    /**
     * Clears the previously submitted data for the usertask.
     * This is important step so that the task is rendered empty when the usertask is rendered again after going back.
     *
     * @param task the user task
     */
    private void clearPreviouslySubmittedDataForUserTask(final Task task) {
        processEngine.getRuntimeService().setVariableLocal(task.getProcessInstanceId(), task.getTaskDefinitionKey(), emptyMap());
    }

    /**
     * The flowautomation frameworks maintains a copy of flowinput on completing every usertask if the back feature is enabled in a flow.
     * On navigating back the framework takes care of setting the flowInput back to its value at the time of completion of previous usertask.
     * @param task the current active usertask
     */
    private void setFlowInputPreviousState(Task task) {
        final Optional<String> previousCompletedUserTask = userTaskHelper.getPreviousCompletedUserTask(task);
        if(previousCompletedUserTask.isPresent()){
            final VariableInstance flowInputVariable = processEngine.getRuntimeService().createVariableInstanceQuery()
                    .variableName(getFlowInputNameForUserTask(previousCompletedUserTask.get())).processInstanceIdIn(task.getProcessInstanceId()).singleResult();

            if (Objects.nonNull(flowInputVariable)) {
                processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), FLOW_INPUT.getName(), flowInputVariable.getValue());
            }
        }
    }

    private String getFlowInputNameForUserTask(final String userTaskDefinitionKey) {
        return "FAInternal_FLOW_INPUT_" + userTaskDefinitionKey;
    }

    /**
     * Sets the output parameter in I/O mapping to empty value. This is required when going back from a usertask.
     * Assumes the user task's formkey is same as output parameter name.
     *
     * @param task the user task
     */
    private void setEmptyOutputParameterForUserTask(final Task task) {
        final String userTaskPropertyKey = userTaskFormKeyResolver.getFormKeyName(task);
        if (Objects.nonNull(userTaskPropertyKey)) { // the key is not available for review and confirm usertask.
            processEngine.getRuntimeService().setVariablesLocal(task.getExecutionId(), Collections.singletonMap(userTaskPropertyKey, emptyMap()));
        }
    }
    
    private List<UserTask> createActiveUserTasks(final List<Task> activeTasks, final FlowExecutionEntity flowExecutionEntity, final String executionPhase) {

        //Reference for future test: check if the execution is stoppable removed here, ie setup phase
        return activeTasks.stream().map(
            task -> new UserTask(task.getId(), task.getName(), getNameExtraFromSchema(flowExecutionEntity, executionPhase, task), task.getProcessDefinitionId(), task.getTaskDefinitionKey(), ACTIVE))
            .collect(Collectors.toList());
    }

    private void handleNonIntractiveSetupUserTask(final String taskId, final Map<String, byte[]> userTaskFileInput, final Task task) {

        final String fileName = userTaskFileInput.entrySet().iterator().next().getKey();
        if (fileName == null) {
            throw new FlowServiceException(INVALID_FLOW_INPUT, FLOW_EXECUTION_SETUP_INPUT_FILE_NOT_FOUND);
        }

        if (userTaskFileInput.size() > 1) {
            throw new FlowServiceException(INVALID_FLOW_INPUT, FLOW_EXECUTION_SETUP_MORE_THAN_ONE_FILE_UPLOADED);
        }

        if (FLOW_INPUT_FILE.equals(fileName)) { // only flow-input.json is supplied.
            completeNonInteractiveSetupUserTask(taskId, userTaskFileInput, task);
        } else if (FileUtil.isFileTypeZip(fileName)) { //setup zip is provided
            final byte[] fileByteArray = userTaskFileInput.get(fileName);
            final Optional<FlowExecutionErrorCode> violation = Optional.ofNullable(FlowExecutionUtil.validateFlowExecutionSetupInput(fileByteArray));
            if (!violation.isPresent()) {
                completeNonInteractiveSetupUserTask(taskId, getFlowInputFiles(fileByteArray), task);
            } else {
                final String logMessage = violation.get().reason();
                LOGGER.error("Flow execution input file: [{}] validation failed and setup not completed. details: {}", fileName, logMessage);
                throw new FlowServiceException(INVALID_FLOW_INPUT, String.format(violation.get().reason(), taskId));
            }
        } else {
            LOGGER.debug("Invalid flow input file type: {}", taskId);
            throw new FlowServiceException(INVALID_FLOW_INPUT, INVALID_INPUT_FILE_TYPE_UPLOADED);
        }
    }

    private String getNameExtraFromSchema(final FlowExecutionEntity flowExecutionEntity, final String executionPhase, final Task task) {

        //No name extra for review and confirm!!
        if (task == null || REVIEW_AND_CONFIRM_EXECUTE_USER_TASK.getId().equals(task.getTaskDefinitionKey())) {
            return "";
        }

        //This is for performance reasons to avoid building schema over and over for fetching the name extra if its already available in FA_NAME_EXTRA
        final VariableInstance nameExtraVariableInstance = processEngine.getRuntimeService().createVariableInstanceQuery().taskIdIn(task.getId()).variableName(FA_NAME_EXTRA).singleResult();
        if (nameExtraVariableInstance != null) {
            return String.valueOf(nameExtraVariableInstance.getValue());
        }

        final FASchema faSchema = userTaskSchemaFactory.buildSchema(flowExecutionEntity, executionPhase, task);
        String schema = null;
        if (faSchema != null) {
            schema = faSchema.toString();
        }
        String nameExtra = EMPTY;
        try {
            final Map<String, Object> properties = (Map<String, Object>) PayloadTransformer.transformJsonToMap(schema).get(PROPERTIES);
            final Map.Entry<String, Object> property = properties.entrySet().iterator().next();
            final Map<String, Object> taskProperties = (Map<String, Object>) property.getValue();
            for (final Map.Entry<String, Object> prop : taskProperties.entrySet()) {
                if (prop.getKey().equals(NAME_EXTRA)) {
                    nameExtra = (String) prop.getValue();
                    //This is to support name extra in setup phase for usertasks which are already completed
                    if (processEngine.getTaskService().getVariableLocal(task.getId(), FA_NAME_EXTRA) == null) {
                        processEngine.getTaskService().setVariableLocal(task.getId(), FAINTERNAL_TASK_KEY, property.getKey());
                        processEngine.getTaskService().setVariableLocal(task.getId(), FA_NAME_EXTRA, nameExtra);
                        break;
                    }
                }
            }

            if (isNotEmpty(nameExtra)) {
                return nameExtra;
            }
        } catch (final IOException e) {
            LOGGER.error("Cannot convert the schema to map for taskId {}. Exception: {}", task.getId(), e.getMessage());
        }
        return EMPTY;
    }

    private void completeGeneralUserTask(final FlowExecutionEntity flowExecutionEntity, final String executionPhase, final String taskId, final Map<String, byte[]> userTaskFileInput, final String usertTaskInput,
                                         final Task task) {

        final FASchema faSchema = userTaskSchemaFactory.buildSchema(flowExecutionEntity, executionPhase, task);
        final String userTaskSchema = faSchema.toString();
        final boolean flowExecutionInSetupPhaseWithBackEnabled = flowExecutionEntity.getFlowDetailEntity().isBackEnabled() && flowExecutionPhase.isSetupPhase(executionPhase);

        Map<String, Object> userTaskVariables = new HashMap<>();
        if (isNotEmpty(usertTaskInput)) {
            if (flowExecutionInSetupPhaseWithBackEnabled) {
                final Map<String, Object> previouslySubmittedUserTaskData = userTaskHelper.getPreviouslySubmittedUserTaskData(task);
                userTaskVariables = PayloadTransformer.transformJsonPayloadToMap(userTaskSchema, usertTaskInput, userTaskFileInput, previouslySubmittedUserTaskData);
            } else {
                userTaskVariables = PayloadTransformer.transformJsonPayloadToMap(userTaskSchema, usertTaskInput, userTaskFileInput);
            }
        }
        readOnlyUserTaskProcessor.processReadOnlyUserTask(userTaskVariables, task);
        if (flowExecutionInSetupPhaseWithBackEnabled) {
            final VariableInstance flowInputVariable = processEngine.getRuntimeService().createVariableInstanceQuery()
                    .variableName(FLOW_INPUT.getName()).processInstanceIdIn(task.getProcessInstanceId()).singleResult();

            if (Objects.nonNull(flowInputVariable)) {
                processEngine.getRuntimeService().setVariable(task.getProcessInstanceId(), getFlowInputNameForUserTask(task.getTaskDefinitionKey()), flowInputVariable.getValue());
            }

            processEngine.getRuntimeService().setVariableLocal(task.getProcessInstanceId(), task.getTaskDefinitionKey(), userTaskVariables);
        }
        processEngine.getRuntimeService().setVariablesLocal(task.getExecutionId(), userTaskVariables);
        processEngine.getTaskService().complete(taskId);
        LOGGER.debug("Completed the task: {}", taskId);
    }

    private void completeInteractiveSetupUserTask(final String taskId, final Task task) {
        LOGGER.debug("Interactive option is chosen for Choose Setup user task");
        final Map<String, Object> variables = new HashMap<>();
        variables.put(FLOW_INPUT.getName(), new HashMap<String, Object>());
        completeUsertaskWithVariables(taskId, task, variables);
    }

    private void completeNonInteractiveSetupUserTask(final String taskId, final Map<String, byte[]> flowInputFiles, final Task task) {
        final byte[] flowInput = flowInputFiles.get(FLOW_INPUT_FILE);
        if (flowInput == null) {
            LOGGER.debug("Invalid flow input/flow input file not uploaded: {}", taskId);
            throw new FlowServiceException(INVALID_FLOW_INPUT, FLOW_INPUT_FILE_NOT_UPLOADED);
        }
        LOGGER.debug("File input option is chosen for Choose Setup user task");
        final String processInstanceId = task.getProcessInstanceId();
        final FlowExecutionEntity flowExecutionEntity = flowExecutionRepository.getFlowExecution(processInstanceId);
        final FlowDetailEntity flowDetailEntity = flowExecutionEntity.getFlowDetailEntity();
        final String flowInputData = new String(flowInput, StandardCharsets.UTF_8);
        flowInputFiles.remove(FLOW_INPUT_FILE);
        final Map<String, Object> flowInputVariables = getFlowInputVariables(flowDetailEntity.getFlowEntity().getFlowId(),
                flowDetailEntity.getVersion(), flowDetailEntity.getDeploymentId(), flowInputData, flowInputFiles);
        final Map<String, Object> variables = new HashMap<>();
        variables.put(FLOW_INPUT.getName(), flowInputVariables);
        completeUsertaskWithVariables(taskId, task, variables);
    }

    /**
     * Gets the flow execution.
     *
     * @param entity the entity
     * @return the flow execution
     */
    private FlowExecution getFlowExecution(final FlowExecutionEntity entity) {
        final String processInstanceId = entity.getProcessInstanceId();
        final HistoricProcessInstanceQuery historicProcessInstanceQuery = processEngine.getHistoryService().createHistoricProcessInstanceQuery();
        final HistoricProcessInstance historicProcessInstance = historicProcessInstanceQuery.processInstanceId(processInstanceId).singleResult();
        String startTime = null;
        String endTime = null;
        String duration = null;
        if (historicProcessInstance != null) {
            startTime = DateTimeUtil.changeDateTimeFormat(historicProcessInstance.getStartTime().toString(), DATE_TIME_FORMATTER_SQL,
                    DATE_TIME_FORMATTER_WITHOUT_TZ);
            endTime = historicProcessInstance.getEndTime() == null ? null
                    : DateTimeUtil.changeDateTimeFormat(historicProcessInstance.getEndTime().toString(), DATE_TIME_FORMATTER_SQL,
                    DATE_TIME_FORMATTER_WITHOUT_TZ);
            duration = convertMilliSeconds(historicProcessInstance.getDurationInMillis());
        } else {
            LOGGER.error("HistoricProcessInstance {} is unavailable", processInstanceId);
        }

        final Long businessKeyId = entity.getId();
        final FlowBusinessKey flowBusinessKey = new FlowBusinessKey(entity.getFlowDetailEntity().getFlowEntity().getFlowId(),
                entity.getFlowExecutionName(), String.valueOf(businessKeyId));

        final long activeUserTasks = processEngine.getTaskService().createTaskQuery()
                .processInstanceBusinessKey(flowBusinessKey.getCamundaBusinessKey()).active().count();
        return entity.getFlowExecution(startTime, endTime, duration, reportVariableRepository.getSummaryReport(businessKeyId),
                getExecutionPhase(processInstanceId), activeUserTasks);
    }

    /**
     * Gets the execution phase.
     *
     * @param processInstanceId the process instance id
     * @return the execution phase
     */
    private String getExecutionPhase(final String processInstanceId) {

        String executionState = null;
        final HistoricVariableInstanceQuery historicVariableInstanceQuery = processEngine.getHistoryService().createHistoricVariableInstanceQuery();
        final HistoricVariableInstance historicVariableInstance = historicVariableInstanceQuery.processInstanceId(processInstanceId)
                .variableName(FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME).singleResult();
        if (historicVariableInstance != null) {
            executionState = (String) historicVariableInstance.getValue();
        }
        LOGGER.debug("The execution state for processInstanceId: {} is: [{}]", processInstanceId, executionState);
        return executionState;
    }

    private void completeUsertaskWithVariables(final String taskId, final Task task, final Map<String, Object> variables) {
        variables.put(FA_INTERNAL_USER_SETUP.getName(), true);
        processEngine.getRuntimeService().setVariablesLocal(task.getExecutionId(), variables);
        processEngine.getTaskService().complete(taskId);
        LOGGER.debug("Completed the Choose Setup task: {}", taskId);
    }

    private Task getUserTask(final String taskId) {
        final Task task = processEngine.getTaskService().createTaskQuery().taskId(taskId).initializeFormKeys().singleResult();
        if (task == null) {
            LOGGER.debug("User task with id: {} not found.", taskId);
            throw new EntityNotFoundException(TASK_NOT_FOUND, format(TASK_IS_NOT_FOUND, taskId));
        }
        return task;
    }

    private void suspendExecution(final FlowExecutionEntity flowExecution) {
        final String flowExecutionId = flowExecution.getProcessInstanceId();

        checkFlowExecutionSuspendable(flowExecutionId);

        final String flowId = flowExecution.getFlowDetailEntity().getFlowEntity().getFlowId();
        final String flowExecutionName = flowExecution.getFlowExecutionName();
        final String businessKey = new FlowBusinessKey(flowId, flowExecutionName, flowExecution.getId().toString()).getCamundaBusinessKey();

        final RuntimeService runtimeService = processEngine.getRuntimeService();
        final List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).list();

        try {
            processInstances.forEach(processInstance -> runtimeService.suspendProcessInstanceById(processInstance.getProcessInstanceId()));
            runtimeService.setVariable(flowExecutionId, FA_INTERNAL_EXECUTE_STATE_VARIABLE_NAME, SUSPENDED.name());
            LOGGER.info("The flow execution {}.{} is suspended by user: {}", flowId, flowExecutionName, getUserInContext());
        } catch (final ProcessEngineException e) {
            LOGGER.debug("Failed to suspend the flow execution: {}", e.getMessage());
            checkFlowExecutionSuspendable(flowExecutionId);
            throw new FlowAutomationException("Failed to suspend the flow execution: " + flowId + "." + flowExecutionName + ": " + e.getMessage(), e);
        }
    }

    private void checkFlowExecutionSuspendable(final String flowExecutionId) {
        if (!flowExecutionPhase.isExecutionSuspendable(flowExecutionId)) {
            LOGGER.debug("Invalid flow execution phase for execution: {} to suspend.", flowExecutionId);
            throw new OperationNotAllowedException(INVALID_FLOW_EXECUTION_PHASE_FOR_SUSPEND);
        }
    }

    private void checkFlowExecutionStoppable(final String flowExecutionId) {
        if (!flowExecutionPhase.isExecutionStoppable(flowExecutionId)) {
            LOGGER.debug("Invalid flow execution phase for execution: {} to stop.", flowExecutionId);
            throw new OperationNotAllowedException(INVALID_FLOW_EXECUTION_PHASE_FOR_STOP);
        }
    }

    private Map<String, byte[]> getFlowInputFiles(final byte[] fileByteArray) {
        final Map<String, byte[]> flowInputFiles;
        try {
            flowInputFiles = FlowExecutionUtil.extractFlowExecutionSetupInputFiles(fileByteArray);
            return flowInputFiles;
        } catch (final IOException e) {
            throw new FlowServiceException(FLOW_EXECUTION_INPUT_FILE_PARSE_ERROR, e);
        }
    }
}
