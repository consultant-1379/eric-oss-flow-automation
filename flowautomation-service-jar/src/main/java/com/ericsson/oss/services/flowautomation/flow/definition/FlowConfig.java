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

package com.ericsson.oss.services.flowautomation.flow.definition;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * POJO class FlowConfig.
 */
@Getter
@Setter
@ToString
public class FlowConfig {

    @NotNull
    @Size(min = 1, max = 255)
    private String flowId;

    @NotNull
    @Size(min = 1, max = 255)
    private String flowName;

    @NotNull
    @Size(min = 1, max = 11)
    private String flowVersion;

    @NotNull
    @Size(min = 1)
    private String description;

    @Valid
    private FlowSetupConfig setup;

    @Valid
    @NotNull
    private FlowExecuteConfig execute;
}
