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

package com.ericsson.oss.services.flowautomation.flow.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;

/**
 * The Class FlowResourceLoader is to load flow resources like flow-input-schema.json, FAInternal-flow-report-schema.json
 */
@ApplicationScoped
public class FlowResourceLoader {

    public static final String FLOW_INPUT_SCHEMA_FILE = "setup/flow-input-schema.json";

    public static final String FLOW_REPORT_SCHEMA_FILE = "report/flow-report-schema.json";

    public static final String FA_INTERNAL_REPORT_SCHEMA_RESOURCE = "FAInternal-flow-report-schema.json";

    public static final String FA_INTERNAL_SETUP_TYPE_USERTASK_SCHEMA_RESOURCE = "FAInternal-setup-type-usertask-schema.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowResourceLoader.class);

    private static final String NEW_LINE_SEPARATOR = "\n";

    @Inject
    @DefaultProcessEngine
    private ProcessEngine processEngine;

    public FlowResourceLoader() {
        //
    }

    /**
     * Gets the string from class path.
     *
     * @param classpathResourceName the classpath resource name
     * @return the string from class path
     */
    public static Optional<String> getStringFromClassPath(final String classpathResourceName) {
        final ClassLoader classLoader = FlowResourceLoader.class.getClassLoader();
        String resource = null;
        try (final InputStream resourceStream = classLoader.getResourceAsStream(classpathResourceName)) {
            resource = getStringFromInputStream(resourceStream);
        } catch (final IOException e) {
            throw new FlowAutomationException("Failed to read flow resource file: " + e.getMessage(), e);
        }
        return Optional.of(resource);
    }

    /**
     * Gets the flow resource by deployment id.
     *
     * @param deploymentId the deployment id
     * @param resourceName the resource name
     * @return the flow resource by deployment id
     */
    public Optional<String> getFlowResourceByDeploymentId(final String deploymentId, final String resourceName) {
        if (isResourceExistsInDeployment(deploymentId, resourceName)) {
            try {
                final InputStream flowResourceStream = processEngine.getRepositoryService().getResourceAsStream(deploymentId, resourceName);
                return Optional.of(getStringFromInputStream(flowResourceStream));
            } catch (final FlowAutomationException e) {
                LOGGER.error("Failed to load flow resource: {}. Exception: {}", resourceName, e.getMessage());
            }
        }
        return Optional.empty();
    }

    public Optional<String> getUTF8FlowResourceByDeploymentId(final String deploymentId, final String resourceName) {
        if (isResourceExistsInDeployment(deploymentId, resourceName)) {
            try {
                final InputStream flowResourceStream = processEngine.getRepositoryService().getResourceAsStream(deploymentId, resourceName);
                return Optional.of(getUTF8StringFromInputStream(flowResourceStream));
            } catch (final FlowAutomationException e) {
                LOGGER.error("Failed to load flow resource: {}. Exception: {}", resourceName, e.getMessage());
            }
        }
        return Optional.empty();
    }

    /**
     * Lists the resources in the location specified.
     *
     * @param deploymentId the deployment if of the flow package
     * @param location     the location within the flow package
     * @return the list of names of the flow package resources
     */
    public List<String> listResources(final String deploymentId, final String location) {
        return processEngine.getRepositoryService().getDeploymentResourceNames(deploymentId).stream()
                .filter(resource -> resource.contains(location))
                .collect(Collectors.toList());
    }

    /**
     * Gets the string from input stream.
     *
     * @param flowResourceStream the flow resource stream
     * @return the string from input stream
     */
    private static String getStringFromInputStream(final InputStream flowResourceStream) {
        try (final BufferedReader buffer = new BufferedReader(new InputStreamReader(flowResourceStream, StandardCharsets.UTF_8))) {
            return buffer.lines().collect(Collectors.joining(NEW_LINE_SEPARATOR));
        } catch (final IOException e) {
            throw new FlowAutomationException("Failed in get string from input stream: " + e.getMessage(), e);
        }
    }

    private static String getUTF8StringFromInputStream(final InputStream flowResourceStream) {
        try (final BufferedReader buffer = new BufferedReader(new InputStreamReader(flowResourceStream, StandardCharsets.UTF_8))) {
            return buffer.lines().collect(Collectors.joining(NEW_LINE_SEPARATOR));
        } catch (final IOException e) {
            throw new FlowAutomationException("Failed in get utf8 string from input stream: " + e.getMessage(), e);
        }
    }

    private boolean isResourceExistsInDeployment(final String deploymentId, final String resourceName) {
        return processEngine.getRepositoryService().getDeploymentResourceNames(deploymentId).contains(resourceName);
    }
}
