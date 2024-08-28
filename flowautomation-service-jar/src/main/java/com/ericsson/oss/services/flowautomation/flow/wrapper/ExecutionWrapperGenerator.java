/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.services.flowautomation.flow.wrapper;

import static java.lang.String.join;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import static com.ericsson.oss.services.flowautomation.flow.utils.FlowAutomationServiceUtil.isVersionSyntaxValid;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;

import com.ericsson.oss.services.flowautomation.flow.definition.FlowConfig;

/**
 * The Class ExecutionWrapperGenerator.
 */
public final class ExecutionWrapperGenerator {

    /** The Constant VERSION. */
    public static final String VERSION = "version";

    /** The Constant FLOW_ID. */
    public static final String FLOW_ID = "flowId";

    /** The Constant FA_INTERNAL_WRAPPER_PROCESS_RESOURCE. */
    public static final String FA_INTERNAL_WRAPPER_PROCESS_RESOURCE = "flow-execution-wrapper.bpmn";

    /** The Constant FA_INTERNAL_WRAPPER_PROCESS_ID_SUFFIX. */
    public static final String FA_INTERNAL_WRAPPER_PROCESS_ID_SUFFIX = "flow-execution-wrapper";

    /** Pattern to format flow version */
    private static final Pattern FLOW_VERSION_FORMAT = Pattern.compile("^(\\d{3}+\\.\\d{3}\\.\\d{3})$");

    /** The Constant PROCESS_DEFINITION_KEY_FORMAT. */
    private static final Pattern PROCESS_DEFINITION_KEY_FORMAT = Pattern.compile("(.*?)\\.(\\d{3}+\\.\\d{3}\\.\\d{3})$");

    /**
     * Instantiates a new execution wrapper generator.
     */
    private ExecutionWrapperGenerator() {
    }

    /**
     * Generate wrapper process.
     *
     * @param flowConfig
     *            the flow config
     * @return the bpmn model instance
     * @throws IOException
     */
    public static BpmnModelInstance generateWrapperProcess(final FlowConfig flowConfig) throws IOException {
        final BpmnModelInstance modelInstance = getWrapperBpmnModelInstance();

        // Modify process Id
        final Collection<Process> processes = modelInstance.getModelElementsByType(Process.class);
        final String wrappedProcessId = getWrapperProcessId(flowConfig.getFlowId(), flowConfig.getFlowVersion());
        processes.forEach(process -> process.setId(wrappedProcessId));
        return modelInstance;
    }

    /**
     * Gets the wrapper process id.
     *
     * @param flowId
     *            the flow id
     * @param flowVersion
     *            the flow version
     * @return the wrapper process id
     */
    public static String getWrapperProcessId(final String flowId, final String flowVersion) {
        if (isVersionSyntaxValid(flowVersion)) {
            return join(".", flowId, formatFlowVersion(flowVersion));
        }
        return EMPTY;
    }

    public static Map<String, String> getFlowIdFlowVersion(final String processDefinitionKey) {
        final Matcher matcher = PROCESS_DEFINITION_KEY_FORMAT.matcher(processDefinitionKey);
        final Map<String, String> flowIdFlowVersion = new HashMap<>();
        if (matcher.find()) {
            final String flowId = matcher.group(1);
            final String version = matcher.group(2);
            flowIdFlowVersion.put(FLOW_ID, flowId);
            flowIdFlowVersion.put(VERSION, version);
        }
        return flowIdFlowVersion;
    }

    /**
     * Format flow version.
     *
     * @param flowVersion
     *            the flow version
     * @return the formatted flow version
     */
    private static String formatFlowVersion(final String flowVersion) {
        final Matcher matcher = FLOW_VERSION_FORMAT.matcher(flowVersion);
        if (!matcher.find()) {
            final String[] splitVersion = flowVersion.split("\\.");
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < splitVersion.length; i++) {
                final String version = splitVersion[i];
                if (version.length() == 1) {
                    builder.append("0");
                    builder.append("0");
                    builder.append(version);
                }
                else if (version.length() == 2){
                    builder.append("0");
                    builder.append(version);
                } else {
                    builder.append(version);
                }
                if (i != splitVersion.length - 1) {
                    builder.append(".");
                }
            }
            return builder.toString();
        }
        return flowVersion;
    }

    /**
     * Gets the wrapper process resource name.
     *
     * @param flowId
     *            the flow id
     * @return the wrapper process resource name
     */
    public static String getWrapperProcessResourceName(final String flowId) {
        return join(".", flowId, FA_INTERNAL_WRAPPER_PROCESS_RESOURCE);
    }

    /**
     * Gets the wrapper bpmn model instance.
     *
     * @return the wrapper bpmn model instance
     * @throws IOException
     *             if I/O error happens
     */
    private static BpmnModelInstance getWrapperBpmnModelInstance() throws IOException {
        final ClassLoader classLoader = ExecutionWrapperGenerator.class.getClassLoader();
        try (final InputStream wrapperProcessResourceStream = classLoader.getResourceAsStream(FA_INTERNAL_WRAPPER_PROCESS_RESOURCE)) {
            return Bpmn.readModelFromStream(wrapperProcessResourceStream);
        }
    }
}
