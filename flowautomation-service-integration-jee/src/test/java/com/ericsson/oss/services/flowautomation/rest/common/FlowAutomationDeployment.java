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

package com.ericsson.oss.services.flowautomation.rest.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updated: Jenkins job is now setup to do the deployment before running the integration tests. So doing the deployment again through this arquillian suite file caused the duplicate deployment exception and then the first testcase always fails.
 * Keeping this class commented here so that if something changes again in the jenkin setup and we might need to reintroduce.
 * <p>
 * This class will be used as a template for all other test scenarios. Deploy will occur only once on first test which requires Arquillian.
 */
//@ArquillianSuiteDeployment
public class FlowAutomationDeployment {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowAutomationDeployment.class);

    /**
     * This method is the deployment producer and is run before running the integration tests. It resolves the flow automation ear from the maven
     * pom.xml dependencies and creates the {@code EnterpriseArchive} instance.
     */
 /*   @Deployment(name = "flow-automation", managed = true, order = 1)
    public static EnterpriseArchive deployFlowAutomation() {
        LOGGER.info("Deploying flowautomation ear..");
        final PomEquippedResolveStage mavenResolver = Maven.resolver().loadPomFromFile("pom.xml");
        return createFromZipFile(EnterpriseArchive.class, mavenResolver
                .resolve("com.ericsson.oss.services.flowautomation:flowautomation-service-ear:ear:?").withoutTransitivity().asSingleFile());
    }*/
}
