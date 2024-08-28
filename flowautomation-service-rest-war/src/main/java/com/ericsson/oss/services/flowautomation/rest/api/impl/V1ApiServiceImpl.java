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

package com.ericsson.oss.services.flowautomation.rest.api.impl;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.EJB_TYPE;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.DATE_TIME_PARSING_FAILED;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_DATE_TIME_FORMAT;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.INVALID_FLOW_INPUT;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowExecutionUtil.extractFlowExecutionSetupInputFiles;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.api.FlowService;
import com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode;
import com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode;
import com.ericsson.oss.services.flowautomation.exception.ValidationException;
import com.ericsson.oss.services.flowautomation.execution.filter.FlowExecutionFilterBuilder;
import com.ericsson.oss.services.flowautomation.flow.utils.FileUtil;
import com.ericsson.oss.services.flowautomation.model.*;
import com.ericsson.oss.services.flowautomation.rest.api.external.V1Api;
import com.ericsson.oss.services.flowautomation.rest.api.external.model.FlowDefinitionModel;
import com.ericsson.oss.services.flowautomation.rest.api.external.model.FlowExecutionModel;
import com.ericsson.oss.services.flowautomation.rest.api.external.model.FlowExecutionNameModel;
import com.ericsson.oss.services.flowautomation.rest.api.external.model.FlowStatusModel;
import com.ericsson.oss.services.flowautomation.rest.api.external.model.FlowVersionModel;
import com.ericsson.oss.services.flowautomation.rest.api.external.model.FlowVersionProcessDetailsModel;
import com.ericsson.oss.services.flowautomation.rest.api.external.model.UserTaskModel;
import com.ericsson.oss.services.flowautomation.rest.api.external.model.UserTaskSchemaModel;
import com.ericsson.oss.services.flowautomation.rest.exception.FlowAutomationRestException;
import com.ericsson.oss.services.flowautomation.rest.resource.impl.FlowForm;
import com.ericsson.oss.services.flowautomation.rest.validator.EntityValidatorBean;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaResteasyEapServerCodegen", date = "2021-09-02T14:26:46.061+01:00[Europe/Dublin]")
public class V1ApiServiceImpl implements V1Api {
    private static final String EQUAL_TO = "=";

    private static final String COMMA = ";";

    private static final String FILENAME = "filename";

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    private static final String USERTASK_INPUT = "usertask-input";

    private static final String USERTASK_INPUT_FILES = "usertask-input-files";

    private static final String FLOW_PACKAGE = "flow-package";

    private static final String DASH = "-";

    private static final String FILE_NAME = "file-name";

    private static final String NAME = "name";

    private static final String FLOW_INPUT = "flow-input";

    @Inject
    private Logger logger;

    @Inject
    private EntityValidatorBean validatorBean;

    @Inject
    @ServiceType(EJB_TYPE)
    private FlowService flowBean;

    @Inject
    @ServiceType(EJB_TYPE)
    private FlowExecutionService flowExecutionsBean;

    public Response activateFlowVersion(String flowId, String flowVersion, FlowStatusModel flowStatusModel, SecurityContext securityContext) {
        logger.debug("Activate/deactivate flow with id: {}, flowVersion: {}, status: {}", flowId, flowVersion, flowStatusModel.getValue());
        flowBean.activateFlowVersion(flowId, flowVersion, flowStatusModel.getValue());
        return Response.noContent().build();
    }

    public Response backUserTask(String taskId, SecurityContext securityContext) {
        flowExecutionsBean.revertUserTask(taskId);
        return Response.noContent().build();
    }

    public Response completeUserTask(MultipartFormDataInput input, String taskId, SecurityContext securityContext) {
        logger.debug("Complete user task with taskId: {} and fileInput: {}", taskId, input);
        final Map<String, List<InputPart>> formDataInputParts = input.getFormDataMap();
        String fileName = null;
        String usertTaskInput = null;
        final Map<String, byte[]> userTaskFileInput = new HashMap<>();
        List<InputPart> inputParts = formDataInputParts.get(USERTASK_INPUT_FILES);

        try {
            if (!CollectionUtils.isEmpty(inputParts)) {
                for (final InputPart inputPart : inputParts) {
                    final MultivaluedMap<String, String> headers = inputPart.getHeaders();
                    fileName = getFileName(headers);
                    final InputStream inputStream = inputPart.getBody(InputStream.class, null);
                    final byte[] bytes = IOUtils.toByteArray(inputStream);
                    userTaskFileInput.put(fileName, bytes);
                }
            }
            inputParts = formDataInputParts.get(USERTASK_INPUT);
            if (!CollectionUtils.isEmpty(inputParts) &&  inputParts.get(0).getBodyAsString().equals("null") ){
                logger.debug("Complete user task with taskId: " + taskId);
                Map<String, Object> variables = new HashMap<>();
                flowExecutionsBean.completeUserTask(taskId, variables);
                return Response.noContent().build();
            }
            else if (!CollectionUtils.isEmpty(inputParts)){
                usertTaskInput = inputParts.get(0).getBodyAsString();
            }
        } catch (final IOException e) {
            throw new FlowAutomationRestException("Error in reading task input", e);
        }
        flowExecutionsBean.completeUserTask(taskId, userTaskFileInput, usertTaskInput);
        return Response.noContent().build();
    }

    public Response deleteExecution(String flowExecutionName, String flowId, SecurityContext securityContext) {
        logger.debug("Delete flow execution with: flowId: {}, flowExecutionName: {}", flowId, flowExecutionName);
        flowExecutionsBean.deleteExecution(flowId, flowExecutionName);
        return Response.noContent().build();
    }

    public Response deleteFlow(String flowId, Boolean force, SecurityContext securityContext) {
        flowBean.deleteFlow(flowId, force);
        return Response.noContent().build();
    }

    public Response downloadExecutionReportVariable(String flowExecutionName, String name, String flowId, SecurityContext securityContext) {
        final FlowExecutionResource executionResource = flowExecutionsBean.getExecutionReportVariable(flowId, flowExecutionName, name);
        return Response.ok(executionResource.getData(), MediaType.APPLICATION_OCTET_STREAM)
                .header(CONTENT_DISPOSITION, "attachment; filename=\"" + executionResource.getName() + "\"")
                .build();
    }

    public Response downloadExecutionResource(String flowExecutionName, String resource, String flowId, SecurityContext securityContext) {
        final FlowExecutionResource executionResource = flowExecutionsBean.getExecutionResource(flowId, flowExecutionName, resource);
        return Response.ok(executionResource.getData(), MediaType.APPLICATION_OCTET_STREAM)
                .header(CONTENT_DISPOSITION, "attachment; filename=\"" + executionResource.getName() + "\"")
                .build();
    }

    public Response enableFlow(String flowId, FlowStatusModel flowStatusModel, SecurityContext securityContext) {
        logger.debug("Enable/disable flow with id: {}, status: {}", flowId, flowStatusModel.getValue());
        flowBean.enableFlow(flowId, flowStatusModel.getValue());
        return Response.noContent().build();
    }

    public Response executeFlow(MultipartFormDataInput input,String flowId,String userID,SecurityContext securityContext) {
        //the UserID is not used here directly, because it is accessed via the SimpleContextService,
        // which accesses the HTTP request.
        final Map<String, List<InputPart>> formDataInputParts = input.getFormDataMap();
        String flowExecutionName = null;
        String flowInputFileName = null;
        final Map<String, byte[]> flowInputFiles = new HashMap<>();
        List<InputPart> inputParts = formDataInputParts.get(FLOW_INPUT);
        FlowExecutionNameModel flowExecutionNameModel = new FlowExecutionNameModel();

        try {
            if (!CollectionUtils.isEmpty(inputParts)) {
                for (final InputPart inputPart : inputParts) {
                    final String fileName = getFileName(inputPart.getHeaders());
                    final byte[] bytes = IOUtils.toByteArray(inputPart.getBody(InputStream.class, null));
                    if (FileUtil.isFileTypeZip(fileName)) {
                        flowInputFiles.putAll(extractFlowExecutionSetupInputFiles(bytes));
                    } else {
                        flowInputFiles.put(fileName, bytes);
                    }
                }
            }
            inputParts = formDataInputParts.get(NAME);
            if (!CollectionUtils.isEmpty(inputParts)) {
                flowExecutionName = inputParts.get(0).getBodyAsString();
                flowExecutionNameModel.setName(flowExecutionName);
            }
            inputParts = formDataInputParts.get(FILE_NAME);
            if (!CollectionUtils.isEmpty(inputParts)) {
                flowInputFileName = inputParts.get(0).getBodyAsString();
            }
            else {
                logger.debug("Executing a flow with id: {} and execution name: {}", flowId, flowExecutionName);
                validatorBean.validate(flowExecutionName, FlowExecutionErrorCode.values());
                flowBean.executeFlow(flowId, flowExecutionName);
                return Response.ok().entity(flowExecutionNameModel).build();
            }
        } catch (final IOException e) {
            throw new FlowAutomationRestException("Error in reading input", e);
        }

        logger.info("Received a flow execution request for flowId: {}, flowExecutionName: {}, flowInputFileName: {}, attached filenames: {}", flowId, flowExecutionName, flowInputFileName, flowInputFiles.keySet());
        if (flowInputFiles.isEmpty()){
            throw new FlowAutomationRestException(BAD_REQUEST, INVALID_FLOW_INPUT);
        }
        final FlowExecutionInput flowExecutionInput = new FlowExecutionInput(flowExecutionName, flowInputFileName, flowInputFiles);
        validatorBean.validate(flowExecutionInput, FlowExecutionErrorCode.values());
        flowBean.executeFlow(flowId, flowExecutionInput);

        return Response.ok().entity(flowExecutionNameModel).build();
    }

    public Response getExecutionReport(String flowExecutionName, String flowId, SecurityContext securityContext) {
        String executionReport = flowExecutionsBean.getExecutionReport(flowId, flowExecutionName);
        return Response.ok().entity(executionReport).build();
    }

    public Response getExecutionReportSchema(String flowExecutionName, String flowId, SecurityContext securityContext) {
        String executionReportSchema = flowExecutionsBean.getExecutionReportSchema(flowId, flowExecutionName);
        return Response.ok().entity(executionReportSchema).build();
    }

    public Response getExecutions(String flowId, String flowExecutionName, String flowVersion, Boolean filterByUser, String userID, SecurityContext securityContext) {
        logger.debug("Get flow executions with: flowId: {}, executionName: {}, flowVersion: {} and filterByUser: {}",
                flowId, flowExecutionName, flowVersion, filterByUser);

        final FlowExecutionFilterBuilder builder = new FlowExecutionFilterBuilder()
                .flowId(flowId)
                .flowExecutionName(flowExecutionName)
                .user(userID);

        if(flowVersion != null) {
                builder.flowVersion(flowVersion);
        }
        if(filterByUser != null){
            builder.filterByUser(filterByUser);
        }

        List<FlowExecution> flowExecutions = flowExecutionsBean.getExecutions(builder.build());

        return Response.ok().entity(toListFlowExecutionModel(flowExecutions)).build();
    }

    public Response getFlow(String flowId, SecurityContext securityContext) {
        logger.debug("Getting summary for flow with id: {}", flowId);
        final FlowDefinition flowDefinition = flowBean.getFlowDefinition(flowId);
        return Response.ok().entity(toFlowDefinitionModel(flowDefinition)).build();
    }

    @Override
    public Response getExecutionEvents(final String flowExecutionName, final String flowId,
                                           final String startDate, final String endDate, final String resource,
                                           final String message, final List<String> severity, final Integer pageNum, final Integer pageSize,
                                           final String sortBy, final String sortOrder, final Integer from, final Integer to, final SecurityContext securityContext) {
        // TODO review datetimes, standardize date handling

        final FlowExecutionEventFilter.Builder builder = new FlowExecutionEventFilter.Builder(flowId, flowExecutionName)
                .target(resource)
                .eventsSeverity(toListFlowExecutionEventSeverity(severity))
                .message(message)
                .startDate(Objects.nonNull(startDate) ? stringTimeToDate(startDate) : null)
                .endDate(Objects.nonNull(endDate) ? stringTimeToDate(endDate) : null);

        //since the sortOrder is string we need to check if it is not null before we converte it
        if (sortBy != null && sortOrder != null) {
            builder.sortData(new SortData(sortBy, toSortOrder(sortOrder)));
        }

        if (pageNum != null && pageSize != null) {
            builder.paginationData(new PaginationData((pageNum - 1) * pageSize, pageSize));
        } else if (from != null && to != null) { // The UI needs this to support virtual scrolling
            builder.paginationData(new PaginationData(from, to - from + 1));
        }

        PaginatedData<FlowExecutionEvent> paginatedData = flowExecutionsBean.getExecutionEvents(builder.build());

        return Response.ok().entity(paginatedData).build();
    }


    public Response getFlowInputSchema(String flowId, String flowVersion, SecurityContext securityContext) {
        logger.debug("Getting flow input schema for flow with id: {}", flowId);
        return Response.ok().entity(flowBean.getFlowInputSchema(flowId, flowVersion)).build();
    }

    public Response getFlowInputSchemaAndData(String flowExecutionName, String flowId, SecurityContext securityContext) {
        String flowInputSchemaAndData = flowExecutionsBean.getFlowInputSchemaAndData(flowId, flowExecutionName);
        return Response.ok().entity(flowInputSchemaAndData).build();
    }

    public Response getFlowVersionProcessDetails(String flowId, String flowVersion, SecurityContext securityContext) {
        logger.debug("Getting process model flow with id: {} and version: {}", flowId, flowVersion);
        final FlowVersionProcessDetails flowVersionProcessDetails = flowBean.getProcessDetailsForFlowVersion(flowId, flowVersion);
        return Response.ok().entity(toFlowVersionProcessDetailsModel(flowVersionProcessDetails)).build();
    }

    public Response getFlows(SecurityContext securityContext) {
        final List<FlowDefinition> flowDefinitions = flowBean.getFlows();
        return Response.ok().entity(toListFlowDefinitionModel(flowDefinitions)).build();
    }

    public Response getUserTaskSchema(String taskId, SecurityContext securityContext) {
        logger.debug("Get schema for taskId: {}", taskId);
        UserTaskSchema userTaskSchema = flowExecutionsBean.getUserTaskSchema(taskId);
        return Response.ok().entity(toUserTaskSchemaModel(userTaskSchema)).build();
    }

    public Response getUserTasks(String flowExecutionName, String flowId, SecurityContext securityContext) {
        logger.debug("Get user task with flowExecutionName: {}", flowExecutionName);
        List<UserTask> userTasks = flowExecutionsBean.getUserTasks(flowId, flowExecutionName);
        return Response.ok().entity(toListUserTaskModel(userTasks)).build();
    }

    public Response importFlow(MultipartFormDataInput input, String userID, SecurityContext securityContext) {
        //the UserID is not used here directly, because it is accessed via the SimpleContextService,
        // which accesses the HTTP request.
        byte[] flowPackage = new byte[0];
        Map<String, List<InputPart>> formDataInputParts = input.getFormDataMap();
        List<InputPart> inputParts = formDataInputParts.get(FLOW_PACKAGE);
        try {
            if (!CollectionUtils.isEmpty(inputParts)) {
                InputStream inputStream = inputParts.get(0).getBody(InputStream.class, null);
                flowPackage = IOUtils.toByteArray(inputStream);
            }
        } catch (final IOException e) {
            throw new FlowAutomationRestException("Error in input when importing flow", e);
        }

        FlowForm flowForm = new FlowForm(flowPackage);

        validatorBean.validate(flowForm, FlowPackageErrorCode.values());
        final FlowDefinition flowDefinition = flowBean.importFlow(flowForm.getFlowPackage());
        return Response.ok().entity(toFlowDefinitionModel(flowDefinition)).build();
    }

    public Response stopExecution(String flowExecutionName, String flowId, SecurityContext securityContext) {
        flowExecutionsBean.stopExecution(flowId, flowExecutionName);
        return Response.noContent().build();
    }

    public Response suspendAllExecutionsForFlowVersion(String flowId, String flowVersion, SecurityContext securityContext) {
        flowExecutionsBean.suspendAllExecutionsForFlowVersion(flowId, flowVersion);
        return Response.noContent().build();
    }

    public Response suspendExecution(String flowExecutionName, String flowId, SecurityContext securityContext) {
        flowExecutionsBean.suspendExecution(flowId, flowExecutionName);
        return Response.noContent().build();
    }

    // TODO move to utils class

    private String getFileName(final MultivaluedMap<String, String> headers) {
        final String[] contentDispositionHeader = headers.getFirst(CONTENT_DISPOSITION).split(COMMA);
        for (final String name : contentDispositionHeader) {
            if ((name.trim().startsWith(FILENAME))) {
                final String[] tmp = name.split(EQUAL_TO);
                return tmp[1].trim().replaceAll("\"", "");
            }
        }
        return null;
    }

    // TODO Bring all below to new convertor class

    private Date stringTimeToDate(final String dateStr){
        try {
            final LocalDateTime dateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
            final Instant instant = Timestamp.valueOf(dateTime).toInstant();
            return Date.from(instant);
        } catch (final DateTimeParseException ex) {
            throw new ValidationException(INVALID_DATE_TIME_FORMAT, DATE_TIME_PARSING_FAILED);
        }
    }

    private static List<FlowExecutionModel> toListFlowExecutionModel (List<FlowExecution> flowExecutions){
        List<FlowExecutionModel> flowExecutionModels = new ArrayList<>();
        for(FlowExecution flowExecution: flowExecutions){
            flowExecutionModels.add(toFlowExecutionModel(flowExecution));
        }

        return flowExecutionModels;
    }

    private static FlowExecutionModel toFlowExecutionModel(FlowExecution flowExecution) {
        FlowExecutionModel flowExecutionModel = new FlowExecutionModel();
        flowExecutionModel.setName(flowExecution.getName());
        flowExecutionModel.setFlowId(flowExecution.getFlowId());
        flowExecutionModel.setFlowName(flowExecution.getFlowName());
        flowExecutionModel.setFlowVersion(flowExecution.getFlowVersion());
        flowExecutionModel.setExecutedBy(flowExecution.getExecutedBy());
        flowExecutionModel.setStartTime(flowExecution.getStartTime());
        flowExecutionModel.setEndTime(flowExecution.getEndTime());
        flowExecutionModel.setDuration(flowExecution.getDuration());
        flowExecutionModel.setState(flowExecution.getState());
        flowExecutionModel.setNumberActiveUserTasks(flowExecution.getNumberActiveUserTasks());
        flowExecutionModel.setFlowSource(flowExecution.getFlowSource());
        flowExecutionModel.setSummaryReport(flowExecution.getSummaryReport());
        flowExecutionModel.setProcessInstanceId(flowExecution.getProcessInstanceId());
        flowExecutionModel.setProcessInstanceBusinessKey(flowExecution.getProcessInstanceBusinessKey());

        return flowExecutionModel;
    }

    private static List<FlowDefinitionModel> toListFlowDefinitionModel(List<FlowDefinition> listFlowDefinitions){
        List<FlowDefinitionModel> listFlowDefinitionModel = new ArrayList<>();
        for (FlowDefinition flowDefinition : listFlowDefinitions){
            listFlowDefinitionModel.add(toFlowDefinitionModel(flowDefinition));
        }

        return listFlowDefinitionModel;
    }

    private static FlowDefinitionModel toFlowDefinitionModel(FlowDefinition flowDefinition){
        FlowDefinitionModel flowDefinitionModel = new FlowDefinitionModel();
        flowDefinitionModel.setId(flowDefinition.getId());
        flowDefinitionModel.setName(flowDefinition.getName());
        flowDefinitionModel.setStatus(flowDefinition.getStatus());
        flowDefinitionModel.setSource(flowDefinition.getSource());
        flowDefinitionModel.setFlowVersions(toListFlowVersionModel(flowDefinition.getFlowVersions()));

        return flowDefinitionModel;
    }

    private static List<FlowVersionModel> toListFlowVersionModel(List<FlowVersion> listFlowVersions){
        List<FlowVersionModel> listFlowVersionModel = new ArrayList<>();
        for (FlowVersion flowVersion: listFlowVersions){
            listFlowVersionModel.add(toFlowVersionModel(flowVersion));
        }

        return listFlowVersionModel;
    }

    private static FlowVersionModel toFlowVersionModel(FlowVersion flowVersion){
        FlowVersionModel flowVersionModel = new FlowVersionModel();
        flowVersionModel.setVersion(flowVersion.getVersion());
        flowVersionModel.setDescription(flowVersion.getDescription());
        flowVersionModel.setActive(flowVersion.isActive());
        flowVersionModel.setCreatedBy(flowVersion.getCreatedBy());
        flowVersionModel.setCreatedDate(flowVersion.getCreatedDate());
        flowVersionModel.setSetupPhaseRequired(flowVersion.isSetupPhaseRequired());
        flowVersionModel.setReportSupported(flowVersion.isReportSupported());

        return flowVersionModel;
    }


    private SortOrder toSortOrder(String enumStringValue){
        return SortOrder.valueOf(enumStringValue);
    }

    private static FlowExecutionEventSeverity toFlowExecutionEventSeverity(String enumStringValue){
        return FlowExecutionEventSeverity.valueOf(enumStringValue);
    }

    private static List<FlowExecutionEventSeverity> toListFlowExecutionEventSeverity (List<String> enumStringValues){
        List<FlowExecutionEventSeverity> flowExecutionEventSeverities = new ArrayList<>();
        for (String enumStringValue: enumStringValues){
            flowExecutionEventSeverities.add(toFlowExecutionEventSeverity(enumStringValue));
        }

        return flowExecutionEventSeverities;
    }

    private static FlowVersionProcessDetailsModel toFlowVersionProcessDetailsModel(FlowVersionProcessDetails flowVersionProcssDetails){
        FlowVersionProcessDetailsModel flowVersionProcessDetailsModel = new FlowVersionProcessDetailsModel();
        flowVersionProcessDetailsModel.setProcessId(flowVersionProcssDetails.getProcessId());
        flowVersionProcessDetailsModel.setSetupProcessId(flowVersionProcssDetails.getSetupProcessId());
        flowVersionProcessDetailsModel.setExecuteProcessId(flowVersionProcssDetails.getExecuteProcessId());

        return flowVersionProcessDetailsModel;
    }

    private static UserTaskSchemaModel toUserTaskSchemaModel(UserTaskSchema userTaskSchema){
        UserTaskSchemaModel userTaskSchemaModel = new UserTaskSchemaModel();
        userTaskSchemaModel.setSchema(userTaskSchema.getSchema());
        userTaskSchemaModel.setId(userTaskSchema.getId());
        userTaskSchemaModel.setName(userTaskSchema.getName());
        userTaskSchemaModel.setNameExtra(userTaskSchema.getNameExtra());
        userTaskSchemaModel.setProcessDefinitionId(userTaskSchema.getProcessDefinitionId());
        userTaskSchemaModel.setTaskDefinitionId(userTaskSchema.getTaskDefinitionId());
        userTaskSchemaModel.setStatus(userTaskSchema.getStatus());

        return userTaskSchemaModel;
    }

    private static List<UserTaskModel> toListUserTaskModel(List<UserTask> userTasks){
        List<UserTaskModel> userTaskModels = new ArrayList<>();
        for(UserTask userTask: userTasks){
            userTaskModels.add(toUserTaskModel(userTask));
        }

        return userTaskModels;
    }

    private static UserTaskModel toUserTaskModel(UserTask userTask){
        UserTaskModel userTaskModel = new UserTaskModel();
        userTaskModel.setId(userTask.getId());
        userTaskModel.setName(userTask.getName());
        userTaskModel.setNameExtra(userTask.getNameExtra());
        userTaskModel.setProcessDefinitionId(userTask.getProcessDefinitionId());
        userTaskModel.setTaskDefinitionId(userTask.getTaskDefinitionId());
        userTaskModel.setStatus(userTask.getStatus());

        return userTaskModel;
    }
}
