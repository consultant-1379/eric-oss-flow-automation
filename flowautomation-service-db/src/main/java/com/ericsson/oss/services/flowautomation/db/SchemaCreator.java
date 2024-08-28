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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This class is responsible for creating the FlowAutomation and Camunda schemas in the database using Flyway.
 */
@SpringBootApplication
public class SchemaCreator {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(SchemaCreator.class, args);
        ctx.close();
    }
}