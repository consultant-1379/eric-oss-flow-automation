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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Simple rendering of report
 */
public class ReportRenderer {

    private ReportRenderer() {
        // needed for sonarcube warning
    }

    public static void renderReport(final String reportSchema, final String report) throws IOException {

        final ObjectMapper objectMapper = new ObjectMapper();
        final Map<String, Object> schemaMap = objectMapper.readValue(reportSchema, Map.class);
        final Map<String, Object> reportMap = objectMapper.readValue(report, Map.class);

        final Map<String, Object> schemaProperties = (Map<String, Object>) schemaMap.get("properties");
        final Map<String, Object> schemaBody = (Map<String, Object>) schemaProperties.get("body");
        final Map<String, Object> reportHeader = (Map<String, Object>) reportMap.get("header");
        final Map<String, Object> reportBody = (Map<String, Object>) reportMap.get("body");

        printReportHeader(reportHeader);
        printReportProperty("", schemaBody, reportBody);
    }

    private static void printReportHeader(final Map<String, Object> reportHeader) {

        if (reportHeader == null) {
            return;
        }

        printLine("Execution Report");
        printLine("Flow Name: " + reportHeader.get("flowName"));
        printLine("Flow Execution Name: " + reportHeader.get("flowExecutionName"));

        printLine("Start Time: " + TestUtils.getDateTimeLocalFormat((String) reportHeader.get("startTime")));
        if (reportHeader.get("endTime") != null) {
            printLine("End Time: " + TestUtils.getDateTimeLocalFormat((String) reportHeader.get("endTime")));
        }
        if (reportHeader.get("startedBy") != null) {
            printLine("Started By: " + reportHeader.get("startedBy"));
        }
        printLine("Status: " + reportHeader.get("status"));
    }

    private static void printReportProperty(final String indent, final Map<String, Object> schemaMap, final Object reportObject) {

        if (schemaMap == null) {
            return;
        }

        final String type = (String) schemaMap.get("type");

        if (isProgressBar(schemaMap)) {
            printReportProgressBarProperty(indent, schemaMap, reportObject);
        } else if (type.equals("object")) {
            printReportObjectProperty(indent, schemaMap, reportObject);
        } else if (type.equals("array")) {
            printReportArrayProperty(indent, schemaMap, reportObject);
        } else {
            printReportSimpleProperty(indent, schemaMap, reportObject);
        }
    }

    private static void printReportObjectProperty(final String indent, final Map<String, Object> schemaMap, final Object reportObject) {

        if (schemaMap.get("name") != null) {
            printLine(indent + schemaMap.get("name"));
        }

        final Map<String, Object> schemaProperties = (Map<String, Object>) schemaMap.get("properties");
        for (final Map.Entry<?, ?> entry : schemaProperties.entrySet()) {
            final String propertyName = (String) entry.getKey();
            final Map<String, Object> propertySchema = (Map<String, Object>) entry.getValue();
            final Object reportProperty = (reportObject != null ? ((Map) reportObject).get(propertyName) : Collections.emptyMap());
            printReportProperty(indent + "  ", propertySchema, reportProperty);
        }
    }

    @SuppressWarnings({"squid:S1643", "squid:S3776"})
    private static void printReportArrayProperty(final String indent, final Map<String, Object> schemaMap, final Object reportObject) {

        if (schemaMap.get("name") != null) {
            printLine(indent + schemaMap.get("name"));
        }

        printLine(indent + "  " + "-----------------------------------------------------------");
        final List<String> columnPropertyNames = new ArrayList<>();
        String titles = "";

        final Map<String, Object> itemsSchema = (Map<String, Object>) schemaMap.get("items");
        final Map<String, Object> itemsPropertiesSchema = (Map<String, Object>) itemsSchema.get("properties");
        int count = 0;
        for (final Map.Entry<?, ?> entry : itemsPropertiesSchema.entrySet()) {
            count++;
            final boolean isLastElement = (count == itemsPropertiesSchema.entrySet().size());
            final String key = (String) entry.getKey();

            columnPropertyNames.add(key);
            final Map<String, Object> itemPropertiesSchema = (Map<String, Object>) entry.getValue();
            String str = String.format("%1$-24s", itemPropertiesSchema.get("name"));
            if (!isLastElement && str.length() > 24) {
                str = str.substring(0, 24);
            }
            titles += str + " ";

        }

        printLine(indent + "  " + replicate("-", titles.length()));
        printLine(indent + "  " + titles);
        printLine(indent + "  " + replicate("-", titles.length()));

        final List rows = reportObject != null ? (List) reportObject : new ArrayList();
        for (final Object row : rows) {
            final Map<String, Object> rowMap = (Map<String, Object>) row;
            String cells = "";
            count = 0;
            for (final String columnPropertyName : columnPropertyNames) {
                count++;
                final boolean isLastElement = (count == columnPropertyNames.size());
                boolean isDefault = false;
                final Map<String, Object> propertySchema = (Map<String, Object>) itemsPropertiesSchema.get(columnPropertyName);
                if (propertySchema != null) {
                    final Object defaultValue = propertySchema.get("default");
                    if (defaultValue != null && defaultValue.equals(rowMap.get(columnPropertyName))) {
                        cells += String.format("%1$-25s", "");
                        isDefault = true;
                    }
                }
                if (!isDefault) {
                    String str = null;
                    if (isProgressBar(propertySchema)) {
                        str = String.format("%1$-24s", getProgressBarValue(rowMap.get(columnPropertyName)));
                    } else if (isDateTime(propertySchema)) {
                        String dateTime = rowMap.get(columnPropertyName) != null ? rowMap.get(columnPropertyName).toString() : null;
                        if (dateTime != null){
                            str = String.format("%1$-24s", TestUtils.getDateTimeLocalFormat(dateTime));
                        }else{
                            str = "";
                        }
                    } else {
                        str = String.format("%1$-24s", rowMap.get(columnPropertyName));
                    }
                    if (!isLastElement && str.length() > 24) {
                        str = str.substring(0, 24);
                    }
                    cells += str + " ";
                }
            }
            printLine(indent + "  " + cells);
        }
    }

    private static String replicate(String s, int length) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < length; i++) {
            str.append(s);
        }
        return str.toString();
    }

    private static void printReportSimpleProperty(final String indent, final Map<String, Object> schemaMap, final Object reportObject) {
        final String propertyName = (String) schemaMap.get("name");
        final String propertyValue = String.valueOf(reportObject);
        printLine(indent + propertyName + ": " + propertyValue);
    }

    private static void printReportProgressBarProperty(final String indent, final Map<String, Object> schemaMap, final Object reportObject) {
        final String propertyName = (String) schemaMap.get("name");
        String propertyValue = getProgressBarValue(reportObject);
        printLine(indent + propertyName + ": " + propertyValue);
    }

    private static boolean isProgressBar(final Map<String, Object> schemaMap) {
        return "object".equals(schemaMap.get("type")) && "progress".equals(schemaMap.get("format"));
    }

    private static boolean isDateTime(final Map<String, Object> schemaMap) {
        return "string".equals(schemaMap.get("type")) && "date-time".equals(schemaMap.get("format"));
    }

    private static String getProgressBarValue(Object reportObject) {
        String retVal = "";
        if (reportObject != null) {
            Map value = (Map) ((Map) reportObject).get("value");
            if (value != null) {
                retVal = value.get("percent") + "%";
            }
        }
        return retVal;
    }

    @SuppressWarnings("squid:S106")
    private static void printLine(final String line) {
        System.out.println("|  " + line);
    }
}
