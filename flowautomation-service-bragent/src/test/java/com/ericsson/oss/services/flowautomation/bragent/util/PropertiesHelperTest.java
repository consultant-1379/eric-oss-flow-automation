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

package com.ericsson.oss.services.flowautomation.bragent.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Test;

public class PropertiesHelperTest {

    private List<String> value = new ArrayList<>();

    @After
    public void tearDown() {
        this.value.clear();
    }

    @Test
    public void getProperty_unknownProperty_defaultValue() {
        assertEquals("54321", PropertiesHelper.getProperty("random.property", "54321"));
    }

    @Test
    public void getPropertyValue_exist() {
        PropertiesHelper.loadProperties("./src/test/resources/application.properties");
        this.value = new ArrayList<>(PropertiesHelper.getPropertyValueList("fa.agent.apiVersion", "54321"));
        assertEquals("b", this.value.get(0));
    }

    @Test
    public void getPropertyValueList_valueDoesnotExist() {
        PropertiesHelper.loadProperties("./src/test/resources/application.properties");
        this.value = new ArrayList<>(PropertiesHelper.getPropertyValueList("random.property", "54321"));
        assertEquals("54321", this.value.get(0));
    }

    @Test
    public void loadProperties_notAFile_doesntThrowException() {
        try {
            PropertiesHelper.loadProperties("nothing");
        } catch (final Exception e) {
            fail("No exception should have been thrown");
        }
    }

    @Test
    public void loadProperties_propertiesFilePath_loadsProperties() {
        PropertiesHelper.loadProperties("./src/test/resources/application.properties");

        assertEquals("127.0.0.1", PropertiesHelper.getProperty("orchestrator.host", "127.0.0.1"));
    }

    @Test
    public void loadProperties_propertiesFilePath_setProperties() {
        PropertiesHelper.loadProperties("./src/test/resources/application.properties");

        assertEquals("a", PropertiesHelper.getProperty("fa.agent.id", "a"));

        PropertiesHelper.setProperty("fa.agent.id", "testID");

        assertEquals("testID", PropertiesHelper.getProperty("fa.agent.id", "testID"));

        PropertiesHelper.clear();
    }

}