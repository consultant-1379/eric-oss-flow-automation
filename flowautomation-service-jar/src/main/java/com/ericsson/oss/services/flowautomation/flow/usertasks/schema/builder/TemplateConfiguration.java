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

import freemarker.template.Configuration;
import freemarker.template.Version;

/**
 * The Freemarker Template configuration.
 */
public enum TemplateConfiguration {

    TEMPLATE_CONFIGURATION;

    /**
     * The Configuration.
     */
    private Configuration configuration;

    /**
     * Instantiates a new Template configuration.
     */
    TemplateConfiguration() {
        configuration = new Configuration(new Version("2.3.29"));
        configuration.setDefaultEncoding("UTF-8");
    }

    /**
     * Gets configuration.
     *
     * @return the configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }


}
