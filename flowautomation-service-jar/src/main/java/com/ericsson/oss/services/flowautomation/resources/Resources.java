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

package com.ericsson.oss.services.flowautomation.resources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;

/**
 * The Class Resources.
 */
public class Resources {

    /**
     * Gets the process engine.
     *
     * @return the process engine
     */
    @Produces
    @DefaultProcessEngine
    @ApplicationScoped
    public ProcessEngine getProcessEngine() {
        return ProcessEngines.getProcessEngine("default");
    }
}
