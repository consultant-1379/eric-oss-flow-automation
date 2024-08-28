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

package com.ericsson.oss.services.flowautomation.plugin;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Flow automation custom process engine plugin.
 */
public class FlowAutomationCustomProcessEnginePlugin extends AbstractProcessEnginePlugin {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public void preInit(final ProcessEngineConfigurationImpl processEngineConfiguration) {
        logger.info("Adding custom history event handler...");
        processEngineConfiguration.setHistoryEventHandler(new FlowAutomationCustomHistoryEventHandler());
    }
}
