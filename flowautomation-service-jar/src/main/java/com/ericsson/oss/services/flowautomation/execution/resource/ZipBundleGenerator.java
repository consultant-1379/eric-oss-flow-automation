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

import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_RESOURCE_GENERATION_FAILED;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.NO_FLOW_EXECUTION_RESOURCE_AVAILABLE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceEmptyException;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;

/**
 * The type Zip bundle resource containing all the resources for a flow execution.
 */
@FlowExecutionResourceType("all")
public class ZipBundleGenerator implements FlowExecutionResourceGenerator {

    @Any
    @Inject
    private Instance<FlowExecutionResourceGenerator> resourceGenerators;

    @Inject
    private Logger logger;

    @Override
    public FlowExecutionResource generate(final FlowExecutionEntity flowExecutionEntity, final FlowExecution flowExecution) {
        final AtomicBoolean emptyBundle = new AtomicBoolean(true);
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (final ZipOutputStream zos = new ZipOutputStream(baos)) {
                resourceGenerators.forEach(generator -> {
                    if (!(generator instanceof ZipBundleGenerator)) {
                        try {
                            for (final FlowExecutionResource resource : generator.generateForZipBundle(flowExecutionEntity, flowExecution)) {
                                zos.putNextEntry(new ZipEntry(String.join("/", resource.getCategory(), resource.getName())));
                                zos.write(resource.getData());
                            }
                            emptyBundle.set(false);
                        } catch (final FlowAutomationException | IOException e) {
                            logger.warn("Failed to generate flow execution resource, skipping... exception: {}", e.getMessage());
                        } finally {
                            closeZipEntryQuietly(zos);
                        }
                    }
                });
            } //Closes ZipOutputStream
            if (emptyBundle.get()) {
                logger.info("None of the resources could be zipped for the flow execution: [{}/{}]", flowExecution.getFlowId(), flowExecution.getName());
                throw new FlowResourceEmptyException(NO_FLOW_EXECUTION_RESOURCE_AVAILABLE, "Failed to generate the zip of resources as no resource available to zip.");
            }
            return new FlowExecutionResource(String.format("%s-resources.zip", flowExecution.getName()), baos.toByteArray());
        } catch (final IOException e) { // closes ByteArrayOutputStream
            logger.error("Failed to build the resources zip, cause: {}", e.getMessage());
        }

        throw new FlowResourceNotFoundException(FLOW_EXECUTION_RESOURCE_GENERATION_FAILED, "Failed to generate the zip of resources.");
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
