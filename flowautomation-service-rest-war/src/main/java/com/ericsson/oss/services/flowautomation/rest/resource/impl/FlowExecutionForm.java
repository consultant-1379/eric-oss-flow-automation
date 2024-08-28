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

package com.ericsson.oss.services.flowautomation.rest.resource.impl;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * The Class FlowExecutionForm.
 */
@Getter
@Setter
public class FlowExecutionForm {

    /** The flow execution name. */
    @NotNull(message = "flow-execution-2100")
    private String name;

    /** The flow input file name. */
    private String flowInputFileName;

    /** The flow input files. */
    private Map<String, byte[]> flowInputFiles;

}
