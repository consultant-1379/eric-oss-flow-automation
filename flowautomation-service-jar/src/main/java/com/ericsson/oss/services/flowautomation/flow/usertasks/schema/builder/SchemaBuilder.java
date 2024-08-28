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

package com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder;

import static java.lang.String.format;

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.SCHEMA_IS_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SCHEMA_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder.TemplateConfiguration.TEMPLATE_CONFIGURATION;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.cache.SchemaCache.SCHEMA_CACHE;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader.getStringFromClassPath;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.flow.utils.FlowResourceLoader;
import com.ericsson.oss.services.flowautomation.flow.utils.SchemaUtils;
import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * The Class UserTaskSchemaBuilder.
 */
public abstract class SchemaBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaBuilder.class);

    /** The Constant objectMapper. */
    protected static final ObjectMapper objectMapper = new ObjectMapper();

    /** The flow resource loader. */
    @Inject
    protected FlowResourceLoader flowResourceLoader;

    /** The process engine. */
    @Inject
    @DefaultProcessEngine
    protected ProcessEngine processEngine;

    /**
     * Gets the schema map.
     *
     * @param task         the task
     * @param deploymentId the deployment id
     * @param resourceName the resource name
     * @return the schema map
     */
    protected final Map<String, Object> getSchemaMap(final String deploymentId, final String resourceName, Task task) {
        LOGGER.info("Get schema: {} of deployment: {} from cache.", resourceName, deploymentId);
        Map<String, Object> schemaMap = null;
        String schema = SCHEMA_CACHE.get(deploymentId, resourceName);

        if (schema == null) {
            LOGGER.info("No schema: {} of deployment: {} retrieved from cache, hence load from DB.", resourceName, deploymentId);
            final Optional<String> jsonSchema = flowResourceLoader.getFlowResourceByDeploymentId(deploymentId, resourceName);
            if (jsonSchema.isPresent()) {
                LOGGER.info("Schema: {} of deployment: {} retrieved from DB, cache it.", resourceName, deploymentId);
                String actualSchema = jsonSchema.get();
                SCHEMA_CACHE.put(deploymentId, resourceName, actualSchema);
                schemaMap = getSchemaMap(resourceName, task, actualSchema);
            } else {
                LOGGER.error("No schema: {} found of deployment: {}", resourceName, deploymentId);
                throw new FlowResourceNotFoundException(SCHEMA_NOT_FOUND, format(SCHEMA_IS_NOT_FOUND, resourceName));
            }
        } else {
            schemaMap = getSchemaMap(resourceName, task, schema);
        }
        return schemaMap;
    }


    /**
     * Gets schema map.
     *
     * @param deploymentId the deployment id
     * @param resourceName the resource name
     * @return the schema map
     */
    protected final Map<String, Object> getSchemaMap(final String deploymentId, final String resourceName) {
        return getSchemaMap(deploymentId,resourceName,null);
    }

    private Map<String, Object> getSchemaMap(final String resourceName, final Task task, final String actualSchema) {
        Map<String, Object> schemaMap;
        if (task != null && resourceName.endsWith(".ftl")) {
            try {
                Template template = new Template(resourceName, new StringReader(actualSchema), TEMPLATE_CONFIGURATION.getConfiguration());
                Map<String, Object> variables = processEngine.getRuntimeService().getVariablesLocal(task.getExecutionId());
                StringWriter writer = new StringWriter();
                template.process(variables, writer);
                String processedSchema = writer.getBuffer().toString();
                schemaMap = SchemaUtils.getSchemaMap(processedSchema);
            } catch (final IOException | TemplateException e) {
                LOGGER.error("Error in parsing template schema: {}", e.getMessage());
                throw new FlowAutomationException("Error in parsing template schema: " + e.getMessage(), e);
            }

        } else {
            schemaMap = SchemaUtils.getSchemaMap(actualSchema);
        }
        return schemaMap;
    }


    /**
     * Gets the schema.
     *
     * @param resource the resource
     * @return the schema
     */
    public String getInternalSchemaResource(final String resource) {
        final Optional<String> chooseSetupTaskSchema = getStringFromClassPath(resource);
        if (chooseSetupTaskSchema.isPresent()) {
            return chooseSetupTaskSchema.get();
        }
        LOGGER.debug("Schema not found: {}", resource);
        throw new FlowResourceNotFoundException(SCHEMA_NOT_FOUND, format(SCHEMA_IS_NOT_FOUND, resource));
    }
}
