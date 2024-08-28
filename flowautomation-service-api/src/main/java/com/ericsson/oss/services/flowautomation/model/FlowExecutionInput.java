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

package com.ericsson.oss.services.flowautomation.model;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.ToString;

/**
 * The Class FlowExecutionInput.
 */
@Getter
@ToString
public class FlowExecutionInput {

    /** The flow execution name. */
    @NotNull(message = "flow-execution-2100")
    @Size(min = 1, message = "flow-execution-2100")
    private final String name;

    /** The flow input file name. */
    @NotNull(message = "flow-execution-2109")
    @Size(min = 1, message = "flow-execution-2109")
    private final String flowInputFileName;

    @NotNull(message = "flow-execution-2106")
    @Size(min = 1, message = "flow-execution-2106")
    private final String flowInput;

    /** The flow input files. */
    @NotNull(message = "flow-execution-2106")
    private final Map<String, byte[]> flowInputFiles;

    /**
     * Instantiates a new flow execution input.
     *
     * @param name              the name
     * @param flowInputFileName the flow input file name
     * @param flowInputFiles    the flow input files
     */
    public FlowExecutionInput(final String name, final String flowInputFileName, final Map<String, byte[]> flowInputFiles) {
        this.name = name;
        this.flowInputFileName = flowInputFileName;
        this.flowInput = new String(flowInputFiles.get(flowInputFileName), StandardCharsets.UTF_8);
        this.flowInputFiles = flowInputFiles;
        this.flowInputFiles.remove(flowInputFileName);
    }
}
