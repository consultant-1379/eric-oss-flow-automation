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

package com.ericsson.oss.services.flowautomation.test.template;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TemplateProcessorTest {

    @Test
    public void testClasspathResourcesDefault() throws Exception {
        doTest("templateTester/usertask-nodetype-scripts.ftl", "templateTester/usertask-nodetype-scripts-templateData.json");
    }

    @Test
    public void testClasspathResourcesExplicit() throws Exception {
        doTest("classpath:templateTester/usertask-nodetype-scripts.ftl", "classpath:templateTester/usertask-nodetype-scripts-templateData.json");
    }

// Example of how to use non-classpath files - do not uncomment, will not work in Jenkins
//    @Test
//    public void testFiles() throws Exception {
//        doTest("file:///git/flowautomation-service/flowautomation-service-test-framework/src/test/resources/templateTester/usertask-nodetype-scripts.ftl",
//                "file:///git/flowautomation-service/flowautomation-service-test-framework/src/test/resources/templateTester/usertask-nodetype-scripts-templateData.json");
//    }

    @SuppressWarnings("squid:S106")
    private void doTest(final String templateFilePath, final String templateDataFilePath) throws Exception {
        final TemplateProcessor templateProcessor = new TemplateProcessor();
        final String templatedString = templateProcessor.processTemplate(templateFilePath, templateDataFilePath);
        assertNotNull(templatedString);
        /* Idun-2351
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.readTree(templatedString);
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
        */
    }

}
