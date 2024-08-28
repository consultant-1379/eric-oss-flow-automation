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

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_SETUP_PHASE_NOT_AVAILABLE;
import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.SCHEMA_IS_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_RESOURCE_GENERATION_FAILED;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.NO_FLOW_EXECUTION_RESOURCE_AVAILABLE;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SCHEMA_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SETUP_PHASE_IN_PROGRESS;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.SETUP_PHASE_NOT_AVAILABLE;
import static com.ericsson.oss.services.flowautomation.execution.resource.FlowExecutionResourceCategory.SETUP;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROPERTIES;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader.FLOW_INPUT_SCHEMA_FILE;
import static com.ericsson.oss.services.flowautomation.flow.utils.PayloadTransformer.transformMapToJsonPayload;
import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.slf4j.Logger;

import com.ericsson.oss.services.flowautomation.entities.FlowDetailEntity;
import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.FlowExecutionPhase;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.flow.setting.FlowInputSchemaAndDataBuilder;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowBusinessKey;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowExecutionUtil;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader;
import com.ericsson.oss.services.flowautomation.flow.utils.SchemaUtils;
import com.ericsson.oss.services.flowautomation.flow.wrapper.WrapperFlowElement;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;
import com.ericsson.oss.services.flowautomation.repo.FlowExecutionRepository;
import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This class generates the flow input supplied in the setup phase for for the flow execution.
 */
@FlowExecutionResourceType("flowinput")
public class FlowInputGenerator implements FlowExecutionResourceGenerator {

    private final Map<String, Object> processedFlowInput = new HashMap<>();
    private final Map<String, byte[]> inputFiles = new HashMap<>();

    @Inject
    protected FlowExecutionRepository flowExecutionRepository;

    @Inject
    private FlowInputSchemaAndDataBuilder flowInputSchemaAndDataBuilder;

    @Inject
    private Logger logger;

    @Inject
    private FlowExecutionPhase flowExecutionPhase;

    @Inject
    protected FlowResourceLoader flowResourceLoader;

    @Inject
    @DefaultProcessEngine
    protected ProcessEngine processEngine;

    @Override
    public FlowExecutionResource generate(final FlowExecutionEntity flowExecutionEntity, final FlowExecution flowExecution) {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (final ZipOutputStream zos = new ZipOutputStream(baos)) {
                if (!zipFlowInputResources(generateForZipBundle(flowExecutionEntity, flowExecution), zos)) {
                    logger.info("None of the resources could be zipped for the flow execution: [{}/{}]",
                            flowExecution.getFlowId(), flowExecution.getName());
                    throw new FlowResourceNotFoundException(NO_FLOW_EXECUTION_RESOURCE_AVAILABLE,
                            "Failed to generate the zip of resources as no resource available to zip.");
                }
            } // Closes ZipOutputStream

            return new FlowExecutionResource(String.format("%s-%s.zip", flowExecution.getName(), "setup"), baos.toByteArray());
        } catch (final IOException e) { // closes ByteArrayOutputStream
            logger.error("Failed to build the resources zip, Exception: {}", e.getMessage());
        }

        throw new FlowResourceNotFoundException(FLOW_EXECUTION_RESOURCE_GENERATION_FAILED,
                "Failed to generate the zip of resources.");
    }

    @Override
    public Collection<FlowExecutionResource> generateForZipBundle(final FlowExecutionEntity flowExecutionEntity, final FlowExecution flowExecution) {
        final FlowDetailEntity flowDetailEntity = flowExecutionEntity.getFlowDetailEntity();
        if (flowDetailEntity.getSetupId() == null) { // The flow has no setup phase.
            logger.debug("The flow with id: {}, version: {} does not have setup phase", flowExecution.getFlowId(), flowDetailEntity.getVersion());
            throw new FlowResourceNotFoundException(SETUP_PHASE_NOT_AVAILABLE, format(FLOW_SETUP_PHASE_NOT_AVAILABLE, flowExecution.getFlowId(), flowDetailEntity.getVersion()));
        }

        final Map<String, Object> flowInputData;
        final String executionId = flowExecutionEntity.getProcessInstanceId();
        if (flowExecutionPhase.isExecutionInSetupPhase(executionId)) {
            if (flowExecutionPhase.isExecutionInReviewConfirmPhase(executionId)) { //The flow input is only available to be downloaded from review-confirm phase onwards.
                final FlowBusinessKey flowBusinessKey = new FlowBusinessKey(flowExecutionEntity.getFlowId(), flowExecutionEntity.getFlowExecutionName(), flowExecutionEntity.getId().toString());
                final String businessKey = flowBusinessKey.getCamundaBusinessKey();

                final Task task = processEngine.getTaskService().createTaskQuery()
                        .processInstanceBusinessKey(businessKey)
                        .taskDefinitionKey(WrapperFlowElement.REVIEW_AND_CONFIRM_EXECUTE_USER_TASK.getId())
                        .active()
                        .singleResult();

                flowInputData = flowInputSchemaAndDataBuilder.getFlowInput(task.getProcessInstanceId());
                if(flowInputData.isEmpty()){
                    throw new FlowResourceNotFoundException(NO_FLOW_EXECUTION_RESOURCE_AVAILABLE, "The flow input is not available for the selected flow execution.");
                }
            } else {
                throw new FlowResourceNotFoundException(SETUP_PHASE_IN_PROGRESS, "The flowInput is not available in the setup phase of flow execution");
            }
        } else {
            flowInputData = flowInputSchemaAndDataBuilder.getFlowInputData(executionId);
        }

        final String flowInputSchema = flowResourceLoader.getFlowResourceByDeploymentId(flowExecutionEntity.getFlowDetailEntity().getDeploymentId(), FLOW_INPUT_SCHEMA_FILE)
                .orElseThrow(()-> new FlowResourceNotFoundException(SCHEMA_NOT_FOUND, format(SCHEMA_IS_NOT_FOUND, FLOW_INPUT_SCHEMA_FILE)));
        final Map<String, Object> flowInput = buildFlowInputFromSchemaAndData(flowInputSchema, flowInputData);

        try {
            final Map<String, byte[]> flowInputResources = (Map<String, byte[]>) processFlowInputVariables(flowInput).get("inputFiles");
            final List<FlowExecutionResource> flowExecutionResources = flowInputResources.entrySet().stream()
                    .map(entry -> new FlowExecutionResource(entry.getKey(), SETUP.getName(), entry.getValue()))
                    .collect(Collectors.toList());
            flowExecutionResources.add(new FlowExecutionResource("flow-input.json", SETUP.getName(), transformMapToJsonPayload(flowInput).getBytes(StandardCharsets.UTF_8)));
            return flowExecutionResources;
        } catch (final JsonProcessingException e) {
            logger.error("{}: {}", FLOW_EXECUTION_RESOURCE_GENERATION_FAILED, e.getMessage());
            throw new FlowAutomationException(FLOW_EXECUTION_RESOURCE_GENERATION_FAILED, "Unable to generate flowinput map to the json: " + e.getMessage());
        }
    }

    /**
     * Builds flowInput by including the only properties that are defined in the flow input schema and have a non null data for them.
     * It also takes care of the order in which the properties are defined in the flow input schema.
     * @param flowInputSchema the flow input schema for the flow
     * @param flowInputData the data submitted for the flow input
     * @return the ordered map of flow input
     */
    private Map<String, Object> buildFlowInputFromSchemaAndData(String flowInputSchema, Map<String, Object> flowInputData) {
        final Map<String, Object> flowInputProperties = (Map<String, Object>) SchemaUtils.getSchemaMap(flowInputSchema).get(PROPERTIES);
        return flowInputProperties.keySet().stream()
                .filter(key -> Objects.nonNull(flowInputData.get(key)))
                .collect(Collectors.toMap(key -> key, flowInputData::get, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private boolean zipFlowInputResources(final Collection<FlowExecutionResource> resources, final ZipOutputStream zos) {
        boolean validZip = false;
        for (final FlowExecutionResource resource : resources) {
            try {
                zos.putNextEntry(new ZipEntry(String.join("/", resource.getCategory(), resource.getName())));
                zos.write(resource.getData());
                validZip = true;
            } catch (final IOException e) {
                logger.warn("Failed to add resource: {} in the zip, skipping. Exception: {}", resource.getName(), e.getMessage());
            }
        }
        closeZipEntryQuietly(zos);
        return validZip;
    }

    private Map<String, Object> processFlowInputVariables(final Map<String, Object> flowInput) {
        for (final Map.Entry<String, Object> e : flowInput.entrySet()) {
            if (e.getValue() instanceof Object) {
                if (e.getValue() instanceof FileValue) {
                    final FileValue nestedBlob = (FileValue) e.getValue();
                    updateFlowInputFilesNames(flowInput, nestedBlob, e.getKey());
                } else if (!FlowExecutionUtil.isSupportedSchemaType(e.getValue()) && e.getValue() instanceof Map) {
                    final Map<String, Object> nested = (Map<String, Object>) e.getValue();
                    processFlowInputVariables(nested);
                }
            } else if (e.getValue() instanceof FileValue) {
                final FileValue fileValue = (FileValue) e.getValue();
                updateFlowInputFilesNames(flowInput, fileValue, e.getKey());
            }
        }
        processedFlowInput.put("inputFiles", inputFiles);
        processedFlowInput.put("flowInput", flowInput);
        return processedFlowInput;
    }

    private Map<String, Object> updateFlowInputFilesNames(final Map<String, Object> flowInput,
                                                          final FileValue fileValue, final String key) {
        final String fileName = fileValue.getFilename();
        try (final InputStream inputStream = fileValue.getValue()) {
            final byte[] bytes = IOUtils.toByteArray(inputStream);
            inputFiles.put(fileName, bytes);
        } catch (final IOException e) {
            logger.error("{}: {}", FLOW_EXECUTION_RESOURCE_GENERATION_FAILED, e.getMessage());
            throw new FlowAutomationException(FLOW_EXECUTION_RESOURCE_GENERATION_FAILED, "Unable to get bytes from input steam: " + e.getMessage());
        }
        final Map<String, String> fileNames = new HashMap<>();
        fileNames.put("fileName", fileName);
        flowInput.put(key, fileNames);
        return flowInput;
    }

    /**
     * Closes the entry in the {@link ZipOutputStream} quietly.
     *
     * @param zos the ZipOutputStream to close
     */
    @SuppressWarnings("squid:S00108")
    private void closeZipEntryQuietly(final ZipOutputStream zos) {
        try {
            zos.closeEntry();
        } catch (final IOException ignore) {
            logger.warn("closeZipEntryQuietly() Failed to process: {}", ignore.getMessage());
        }
    }
}
