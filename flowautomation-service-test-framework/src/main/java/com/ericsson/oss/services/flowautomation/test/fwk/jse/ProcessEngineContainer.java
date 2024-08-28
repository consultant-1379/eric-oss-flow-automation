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

package com.ericsson.oss.services.flowautomation.test.fwk.jse;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.engine.ProcessEngine;

import com.ericsson.oss.services.flowautomation.resources.DefaultProcessEngine;

@Singleton
public class ProcessEngineContainer {

    /** The process engine. */
    @Inject
    @DefaultProcessEngine
    protected ProcessEngine processEngine;

    public ProcessEngine getProcessEngine() {
        return processEngine;
    }
}
