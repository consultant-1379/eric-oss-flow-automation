/*******************************************************************************
 * COPYRIGHT Ericsson 2023
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
package com.ericsson.oss.services.flowautomation.flow.usertasks.schema;

import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor.SchemaProcessorReport;

/**
 * This class holds the json schema and the report information from the schema processing.
 *
 */
public class FASchema {

    private String schema;
    private SchemaProcessorReport report;

    public FASchema(final String schema, final SchemaProcessorReport report) {
        this.schema = schema;
        this.report = report;
    }

    public void setReport(final SchemaProcessorReport report) {
        this.report = report;
    }

    public SchemaProcessorReport getReport() {
        return report;
    }

    @Override
    public String toString() {
        return schema;
    }
}
