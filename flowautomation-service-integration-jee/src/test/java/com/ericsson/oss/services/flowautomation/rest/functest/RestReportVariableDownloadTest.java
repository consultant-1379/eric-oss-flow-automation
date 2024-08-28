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

package com.ericsson.oss.services.flowautomation.rest.functest;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;

import com.ericsson.oss.services.flowautomation.flows.test.ReportVariableDownloadTest;
import com.ericsson.oss.services.flowautomation.test.fwk.TestClientType;

/**
 * File handling flow test cases
 */
@RunAsClient
@RunWith(Arquillian.class)
public class RestReportVariableDownloadTest extends ReportVariableDownloadTest {

    @Override
    protected TestClientType selectClientType() {
        return TestClientType.REST;
    }

}
