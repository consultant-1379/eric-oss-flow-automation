/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

package com.ericsson.oss.services.flowautomation.bragent.agent;


import com.ericsson.adp.mgmt.bro.api.registration.SoftwareVersion;
import com.ericsson.oss.services.flowautomation.bragent.util.PropertiesHelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;


public class SoftwareVersionInformationUtilsTest {

    @BeforeClass
    public static void setUp() {
        PropertiesHelper.loadProperties("src/test/resources/application.properties");
    }

    @Test
    public void getSoftwareVersion_propertiesFile_returnsSoftwareInformation() {
        final SoftwareVersion softwareVersion = SoftwareVersionInformationUtils.getSoftwareVersion();

        assertEquals("d", softwareVersion.getDescription());
        assertEquals("2019-09-13", softwareVersion.getProductionDate());
        assertEquals("f", softwareVersion.getProductName());
        assertEquals("g", softwareVersion.getProductNumber());
        assertEquals("i", softwareVersion.getRevision());
        assertEquals("h", softwareVersion.getType());
    }
}
