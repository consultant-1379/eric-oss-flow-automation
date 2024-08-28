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

package com.ericsson.oss.services.flowautomation.test.fwk;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

/**
 * TestUtils tests
 *
 * These tests are useful for manual testing. But need to ignore for automated tests until find way to dynamically discover the test flow jar name
 * Should be easy to do later.
 */
@Ignore
@SuppressWarnings("squid:S1607")
public class TestUtilsTest {

    private static final String TEST_JAR = "../flowautomation-service-test-flows/target/flowautomation-service-test-flows-1.6.8-SNAPSHOT.jar";
    private static final String FLOW = "helloWorld-1.0.1";

    @Test
    public void testGetFlowPackageBytes_Jar() throws Throwable {
        final byte[] packageBytes = TestUtils.getFlowPackageBytesJar("flows", FLOW, TEST_JAR);
        assertNotNull(packageBytes);
        assertTrue(packageBytes.length > 0);
    }

    @Test
    public void testGetFlowdataFileBytes_Jar() throws Throwable {
        final byte[] fileBytes = TestUtils.getFlowdataFileBytesJar(FLOW, "flowInput.json", TEST_JAR);
        assertNotNull(fileBytes);
        assertTrue(fileBytes.length > 0);
    }

}
