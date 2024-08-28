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
package com.ericsson.oss.services.flowautomation.db;

import org.junit.Test;
import org.junit.Test.None;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SchemaCreatorTest {

    final SchemaCreator schemaCreator = new SchemaCreator();

    @Test(expected = None.class)
    public void whenMigrationIsRequestedThenNoExceptionIsThrown() {
        final String[] array = new String[0];
        schemaCreator.main(array);
    }

}