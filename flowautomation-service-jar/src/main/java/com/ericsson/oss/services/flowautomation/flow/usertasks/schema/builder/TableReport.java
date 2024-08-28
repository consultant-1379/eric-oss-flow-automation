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

package com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableReport {

    private final String sheetName;
    private final String tableName;
    private final Map<String, List<String>> content;

    public TableReport(String sheetName, String tableName, Map<String, List<String>> content) {
        // Excel does not allow a sheet name to have more than 31 characters. If that happens, the name will be truncated.
        this.sheetName = sheetName;
        this.tableName = tableName;
        this.content = content;
    }

    /**
     * This constructor should be used if the table name should be used as the sheet name
     * @param tableName
     * @param content
     */
    public TableReport(String tableName, Map<String, List<String>> content) {
        this(tableName, tableName, content);
    }

    public String getSheetName() {
        return this.sheetName;
    }

    public String getTableName() {
        return this.tableName;
    }

    public List<String> getHeaders() {
        return new ArrayList<>(content.keySet());
    }

    public List<String> getValuesByHeader(String header) {
        return content.get(header);
    }

    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }
}
