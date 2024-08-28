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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords;
import com.ericsson.oss.services.flowautomation.model.UserTask;
import com.ericsson.oss.services.flowautomation.model.UserTaskSchema;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Simple rendering of usertask
 */
@SuppressWarnings("unchecked")
public class UsertaskRenderer {

    private static final String DEFAULT = "default";
    private static final String EDIT_TABLE = "edit-table";
    private static final String ENUM = "enum";
    private static final String ENUM_NAMES = "enumNames";
    private static final String FORMAT = "format";
    private static final String FORMAT_OPTIONS = "formatOptions";
    private static final String NAME = "name";
    private static final String OBJECT = "object";
    private static final String PROPERTIES = "properties";
    private static final String REQUIRED = "required";
    private static final String TYPE = "type";
    private static final String GROUPS = "groups";
    private static final String DEFAULT_PLACEHOLDER = "#";
    private static final String NO_DEFAULT_PLACEHOLDER = "______";
    private static final String REQUIRED_PLACEHOLDER = "*";
    private static final String SELECT_PLACEHOLDER = ">";
    private static final String LINK_TEXT = "linkText";
    private static final String UNGROUPED = "Ungrouped";

    private UsertaskRenderer() {
        // needed for sonarcube warning
    }

    public static void renderUsertask(final UserTask usertask, final UserTaskSchema schema)
            throws IOException {
        renderUsertask(usertask, schema, null);
    }

    public static void renderUsertask(final UserTask usertask, final UserTaskSchema schema,
                                      final String userTaskInputString) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Map<String, Object> usertaskSchemaMap =  objectMapper.readValue(schema.getSchema(), Map.class);

        Map<String, Object> usertaskObjectMap = null;
        if (userTaskInputString != null) {
            usertaskObjectMap = objectMapper.readValue(userTaskInputString, Map.class);
        }
        final boolean submit = (userTaskInputString != null);

        final String taskName = usertask.getName();
        if (StringUtils.isNotEmpty(usertask.getNameExtra())) {
            printLine("Task: " + taskName + " - " + usertask.getNameExtra());
        } else {
            printLine("Task: " + taskName);
        }


        final boolean informational = usertaskSchemaMap.get(FORMAT) != null && usertaskSchemaMap.get(FORMAT).equals("informational");

        final Map<String, Object> schemaPropertiesMap = (Map<String, Object>) usertaskSchemaMap.get(PROPERTIES);
        final List<String> requiredSet = (List<String>) usertaskSchemaMap.get(REQUIRED);

        boolean hideName = true;
        if (schemaPropertiesMap.size() > 1) {
            hideName = false;
        }

        printUsertaskProperties("", hideName, informational, schemaPropertiesMap, requiredSet, usertaskObjectMap, submit);

        printLine("");

        final List<String> actions = (List<String>) usertaskSchemaMap.get("actions");
        final String formattedActions = actions.stream()
                .map(action -> {
                    if ("back".equalsIgnoreCase(action)) {
                        return "< <-Modify Previous Task >";
                    } else if ("continue".equalsIgnoreCase(action)) {
                        return "< Continue-> >";
                    } else if("preview".equalsIgnoreCase(action)){
                        return "< Check Previous Task Inputs >";
                    }
                    return String.format("< %s >", action);
                })
                .collect(Collectors.joining("  "));
        printLine(formattedActions);
        printLine("");
    }

    private static void printUsertaskProperties(final String indent, final boolean hideName, final boolean informational,
                                                final Map<String, Object> schemaPropertiesMap, final List<String> requiredSet,
                                                final Object usertaskObject, final boolean submit) {
        for (final Map.Entry<?, ?> entry : schemaPropertiesMap.entrySet()) {
            final Map<String, Object> propertySchema = (Map<String, Object>) entry.getValue();

            boolean required = false;
            if (requiredSet != null && requiredSet.contains(entry.getKey())) {
                required = true;
            }

            Object propertyObject = null;
            if (usertaskObject != null) {
                propertyObject = ((Map<String, Object>) usertaskObject).get(entry.getKey());
            }

            printUsertaskProperty(indent, hideName, informational, propertySchema, required, propertyObject, submit);
        }
    }

    private static void printUsertaskProperty(final String indent, final boolean hideNameIfObject, final boolean informational,
                                              final Map<String, Object> propertySchema, final boolean required,
                                              final Object usertaskObject, final boolean submit) {
        String name = (String) propertySchema.get("name");
        final String nameExtra = (String) propertySchema.get("nameExtra");
        if (nameExtra != null && !nameExtra.isEmpty()) {
            name += " - " + nameExtra;
        }
        final String type = (String) propertySchema.get("type");
        final String format = (String) propertySchema.get(FORMAT);
        final boolean readOnly = propertySchema.get("readOnly") != null && (Boolean) propertySchema.get("readOnly");
        if (type.equals("object")) {
            printUsertaskObjectProperty(indent, hideNameIfObject, informational, propertySchema, required, usertaskObject, name, submit);
        } else if (type.equals("array") && (format != null && format.contains("table"))) {
            printUsertaskArrayProperty(indent, propertySchema, required, usertaskObject, readOnly, submit);
        } else if (format != null && (format.equals("select") || format.equals("select-list") || format.equals("combo"))) {
            printUsertaskSelectAndComboProperty(indent, informational, propertySchema, required, usertaskObject, readOnly, submit);
        } else if (format != null && format.equals("email")) {
            printEmailProperty(indent, propertySchema, required, usertaskObject, readOnly, submit);
        } else {
            printUsertaskSimpleProperty(indent, informational, propertySchema, required, usertaskObject, readOnly, submit);
        }
    }

    private static void printUsertaskArrayProperty(final String indent, final Map<String, Object> schemaMap, final boolean required, final Object usertaskObject, final boolean readOnly, final boolean submit) {
        final List defaultRows = (List) schemaMap.get(DEFAULT);
        final String arrayFormat = (String) schemaMap.get(FORMAT);
        boolean createRow = false;
        final Map<String, Object> formatOptions = (Map<String, Object>) schemaMap.get(FORMAT_OPTIONS);
        if (formatOptions != null && formatOptions.containsKey("createRow") && (boolean) formatOptions.get("createRow")) {
            createRow = true;
        }
        if (arrayFormat.equals("informational-table") && (submit || (defaultRows == null || defaultRows.isEmpty()))) {
            return;
        }

        printLine(indent + schemaMap.get("name") + printRequired(required, submit) + ":");

        printLine(indent + "   " + "-----------------------------------------------------------");
        final List<String> columnPropertyNames = new ArrayList<>();
        String titles = "";

        final Map<String, Object> itemsSchema = (Map<String, Object>) schemaMap.get("items");
        final Map<String, Object> itemsPropertiesSchema = (Map<String, Object>) itemsSchema.get(PROPERTIES);
        int count = 0;
        for (final Map.Entry<?, ?> entry : itemsPropertiesSchema.entrySet()) {
            count++;
            final boolean isLastColumn = (count == itemsPropertiesSchema.entrySet().size());
            final String key = (String) entry.getKey();

            columnPropertyNames.add(key);
            final Map<String, Object> itemPropertiesSchema = (Map<String, Object>) entry.getValue();
            titles += formatValueForCell(itemPropertiesSchema.get("name"), isLastColumn);

        }

        printLine(indent + "   " + titles);
        printLine(indent + "   " + "-----------------------------------------------------------");

        if (!readOnly && usertaskObject != null) {
            final List submittedRows = (List) usertaskObject;
            printArrayRows(submittedRows, itemsPropertiesSchema, columnPropertyNames, arrayFormat, true, defaultRows, submit, indent);
        } else {
            if (defaultRows != null) {
                List selectableRows = (List) schemaMap.get("selectableItems");
                if (selectableRows == null) {
                    selectableRows = defaultRows;
                }
                printArrayRows(selectableRows, itemsPropertiesSchema, columnPropertyNames, arrayFormat, false, defaultRows, submit, indent);
            } else {
                printLine(indent + "   " + NO_DEFAULT_PLACEHOLDER);
            }
        }
        if (createRow && !readOnly && arrayFormat.equals(EDIT_TABLE)) {
            printLine(indent + "   < Add Row >");
        }
    }

    private static void printArrayRows(final List rows, final Map<String, Object> itemsPropertiesSchema, final List<String> columnPropertyNames, final String arrayFormat, final boolean handleDefault, final List defaultRows, final boolean submit, final String indent) {
        int count;
        for (final Object row : rows) {
            final Map<String, Object> rowMap = (Map<String, Object>) row;
            String cells = "";
            count = 0;
            for (final String columnPropertyName : columnPropertyNames) {
                count++;
                final boolean isLastColumn = (count == columnPropertyNames.size());
                final Map<String, Object> propertySchema = (Map<String, Object>) itemsPropertiesSchema.get(columnPropertyName);
                if (propertySchema != null) {
                    if (arrayFormat.equals(EDIT_TABLE) && OBJECT.equals(propertySchema.get(TYPE)) && propertySchema.get(PROPERTIES) != null) {
                        String value = "";
                        final Map<String, Object> groupedPropertiesSchema = (Map<String, Object>) propertySchema.get(PROPERTIES);
                        final Map<String, Object> groupedRowMap = (Map<String, Object>) rowMap.get(columnPropertyName);
                        for (final String groupedPropertyName : groupedPropertiesSchema.keySet()) {
                            if (groupedRowMap != null && groupedRowMap.get(groupedPropertyName) != null) {
                                final Map<String, Object> groupedPropertySchema = (Map<String, Object>) groupedPropertiesSchema.get(groupedPropertyName);
                                if (groupedPropertySchema != null) {
                                    final String propertyValue = "" + (groupedPropertiesSchema.get(ENUM) == null ? groupedRowMap.get(groupedPropertyName) : getEnumNameForEnum(groupedPropertySchema, "" + groupedRowMap.get(groupedPropertyName)));
                                    value += (value.isEmpty() ? "" : ", ") + groupedPropertySchema.get(NAME) + ":" + propertyValue;
                                }
                            }
                        }
                        cells += formatValueForCell(value, isLastColumn);

                    } else {
                        final Object defaultValue = propertySchema.get(DEFAULT);
                        final String format = (String) propertySchema.get(FORMAT);
                        if (handleDefault && !arrayFormat.equals(EDIT_TABLE) && defaultValue != null && defaultValue.equals(rowMap.get(columnPropertyName))) {
                            cells += String.format("%1$-25s", "");
                        } else {
                            String propertyValue = "";
                            if (format != null && format.equalsIgnoreCase("date-time")) {
                                String dateTime = rowMap.get(columnPropertyName) != null ? rowMap.get(columnPropertyName).toString() : null;
                                if (dateTime != null){
                                    propertyValue = TestUtils.getDateTimeLocalFormat(dateTime);
                                }else{
                                    propertyValue = "";
                                }
                            } else {
                                propertyValue = "" + (propertySchema.get(ENUM) == null ? rowMap.get(columnPropertyName) : getEnumNameForEnum(propertySchema, "" + rowMap.get(columnPropertyName)));
                            }
                            cells += formatValueForCell(propertyValue, isLastColumn);
                        }
                    }
                }
            }
            printLine(indent + getTableValuesDefaultString(rowMap, defaultRows, submit) + cells);
        }
    }

    private static String formatValueForCell(Object value, boolean isLastColumn) {
        String retVal = String.format("%1$-24s", value);
        if (!isLastColumn && retVal.length() > 24) {
            retVal = retVal.substring(0, 24);
        }
        return retVal + " ";
    }

    private static String getTableValuesDefaultString(final Map<String, Object> row, final List defaultRows, final boolean submit) {
        if (submit || row == null || defaultRows == null || !defaultRows.contains(row)) {
            return "   ";
        } else {
            return " " + DEFAULT_PLACEHOLDER + " ";
        }
    }

    private static void printUsertaskObjectProperty(final String indent, final boolean hideName, final boolean informational, final Map<String, Object> propertySchema,
                                                    final boolean required, final Object usertaskObject, final String name, final boolean submit) {
        if (propertySchema.get("oneOf") != null) {
            printLine(indent + name + printRequired(required, submit));
            printUsertaskOneOfObjectProperty(indent + "  ", propertySchema, usertaskObject, submit);
        } else {
            final String format = (String) propertySchema.get(FORMAT);
            if (format != null && format.equals("file")) {
                printUsertaskObjectFileProperty(indent, hideName, propertySchema, required, usertaskObject, name, submit);
            } else if (format != null && format.equals("checkbox")) {
                printUsertaskObjectCheckboxProperty(indent, informational, propertySchema, required, usertaskObject, name, submit);
            } else {
                if (!hideName) {
                    printLine(indent + name);
                }

                final Map<String, Object> schemaProperties = (Map<String, Object>) propertySchema.get(PROPERTIES);
                final List<String> requiredSet = (List<String>) propertySchema.get(REQUIRED);
                printUsertaskProperties(indent + "  ", false, informational, schemaProperties, requiredSet, usertaskObject, submit);
            }
        }
    }

    private static void printUsertaskObjectCheckboxProperty(final String indent, final boolean informational,
                                                            final Map<String, Object> propertySchema, final boolean required,
                                                            final Object usertaskObject, final String name, final boolean submit) {
        if (informational) {
            if (propertySchema.get(PROPERTIES) == null || ((Map<String, Object>) propertySchema.get(PROPERTIES)).isEmpty()) {
                printLine(indent + name + ": Not selected");
            } else {
                printLine(indent + name);

                final Map<String, Object> schemaProperties = (Map<String, Object>) propertySchema.get(PROPERTIES);
                final List<String> requiredSet = (List<String>) propertySchema.get(REQUIRED);

                printUsertaskProperties(indent + "  ", false, true, schemaProperties, requiredSet, usertaskObject, submit);
            }
        } else {
            if (usertaskObject == null) {
                printLine(indent + "[ ] " + name);
            } else {
                printLine(indent + "[x] " + name);

                final Map<String, Object> schemaProperties = (Map<String, Object>) propertySchema.get(PROPERTIES);
                final List<String> requiredSet = (List<String>) propertySchema.get(REQUIRED);
                printUsertaskProperties(indent + "  ", false, true, schemaProperties, requiredSet, usertaskObject, submit);
            }
        }
    }

    private static void printUsertaskObjectFileProperty(final String indent, final boolean hideName, final Map<String, Object> propertySchema, final boolean required,
                                                        final Object usertaskObject, final String name, final boolean submit) {
        if (!hideName) {
            printLine(indent + name + printRequired(required, submit) + ":");
        }
        if (usertaskObject != null) {
            final String fileName = (String) ((Map<String, Object>) usertaskObject).get("fileName");
            printLine(indent + "  File Name: " + fileName);
        } else {
            final String fileName = (String) propertySchema.get(DEFAULT);
            printLine(indent + "  File Name: " + (fileName != null ? fileName : NO_DEFAULT_PLACEHOLDER));
        }
    }

    private static void printUsertaskOneOfObjectProperty(final String indent, final Map<String, Object> propertySchema,
                                                         final Object usertaskObject, final boolean submit) {
        final List<Object> options = (List<Object>) propertySchema.get("oneOf");

        final String defaultPropertyName = (String) propertySchema.get(DEFAULT);
        for (final Object option : options) {
            final Map<String, Object> optionProperties = (Map<String, Object>) ((Map<String, Object>) option).get(PROPERTIES);

            final Map.Entry<?, ?> entry = optionProperties.entrySet().iterator().next();
            final String propertyName = (String) entry.getKey();

            final Map<String, Object> propertyOptionSchema = (Map<String, Object>) entry.getValue();

            printUsertaskOneOfObjectSingleProperty(indent, usertaskObject, defaultPropertyName, propertyName, propertyOptionSchema, submit);
        }
    }

    private static void printUsertaskOneOfObjectSingleProperty(final String indent, final Object usertaskObject,
                                                               final String defaultPropertyName, final String propertyName,
                                                               final Map<String, Object> propertyOptionSchema, final boolean submit) {
        boolean checked = false;
        Object propertyObject = null;
        if (usertaskObject != null) {
            propertyObject = ((Map<String, Object>) usertaskObject).get(propertyName);

            if (propertyObject != null) {
                checked = true;
            }
        } else {
            if (propertyName.equals(defaultPropertyName)) {
                checked = true;
            }
        }
        if (checked) {
            printLine(indent + "(x) " + propertyOptionSchema.get("name"));
            if (!propertyOptionSchema.get("type").equals("boolean")) {
                printUsertaskProperty(indent + "  ", true, false, propertyOptionSchema, false, propertyObject, submit);
            }
        } else {
            printLine(indent + "( ) " + propertyOptionSchema.get("name"));
        }
    }

    private static void printUsertaskSelectAndComboProperty(final String indent, final boolean informational,
                                                            final Map<String, Object> propertySchema, final boolean required,
                                                            final Object usertaskObject, final boolean readOnly, final boolean submit) {
        String propertyName = (String) propertySchema.get("name");
        final String nameExtra = (String) propertySchema.get("nameExtra");
        if (nameExtra != null && !nameExtra.isEmpty()) {
            propertyName += " - " + nameExtra;
        }

        String defaultValue = null;
        if (propertySchema.get(DEFAULT) != null) {
            defaultValue = (String) propertySchema.get(DEFAULT);
        }

        if (readOnly || usertaskObject == null) {
            if (informational) {
                String value = defaultValue;
                if (propertySchema.get("enum") != null) {
                    value = getEnumNameForEnum(propertySchema, defaultValue);
                }

                printLine(indent + propertyName + printRequired(required, submit) + ": " + value);
            } else {
                printUsertaskSelectPropertyRegular(indent, propertySchema, required, propertyName, submit);
            }
        } else {
            final String value = getEnumNameForEnum(propertySchema, (String) usertaskObject);
            printLine(indent + propertyName + printRequired(required, submit) + ": " + value);
        }
    }

    private static String getEnumNameForEnum(final Map<String, Object> propertySchema, final String value) {
        final List<String> enums = (List<String>) propertySchema.get("enum");
        final List<String> enumNames = (List<String>) propertySchema.get("enumNames");

        String name = value;
        for (int i = 0; i < enums.size(); i++) {
            if (enums.get(i).equals(value)) {
                if (enumNames != null) {
                    name = enumNames.get(i);
                }
                break;
            }
        }

        return name;
    }

    private static void printUsertaskSelectPropertyRegular(final String indent, final Map<String,
            Object> propertySchema, final boolean required, final String propertyName, final boolean submit) {
        final List<String> enums = (List<String>) propertySchema.get("enum");
        final List<String> enumValues = (List<String>) propertySchema.get("enumNames");
        final String format = (String) propertySchema.get(FORMAT);
        final List<String> selectValues;
        String defaultEnumName = null;
        int enumValueIndex = 0;

        if (enumValues != null) {
            selectValues = enumValues;
        } else {
            selectValues = enums;
        }

        if (format.equals("select-list")) {
            printLine(indent + "----------------------------------");
            printLine(indent + propertyName + printRequired(required, submit) + ":");
            printLine(indent + "----------------------------------");
            printSelectListDefaultValues(indent, propertySchema);
        } else {
            printLine(indent + propertyName + printRequired(required, submit) + ":");
        }

        if (selectValues != null && !selectValues.isEmpty()) {
            if (enumValues != null && propertySchema.get(DEFAULT) != null) {
                for (int x = 0; x < enums.size(); x++) {
                    if (propertySchema.get(DEFAULT).equals(enums.get(x))) {
                        enumValueIndex = x;
                        defaultEnumName = enumValues.get(enumValueIndex);
                        break;
                    }
                }
            }
        }

        printValues(indent, format, selectValues, enumValueIndex, propertySchema, defaultEnumName);
    }

    private static void printSelectListDefaultValues(final String indent, final Map<String, Object> propertySchema) {
        if (propertySchema.get(DEFAULT) != null) {
            printLine(indent + " " + DEFAULT_PLACEHOLDER + " " + propertySchema.get(DEFAULT));
        }
    }

    private static void printValues(final String indent, final String format, final List<String> selectValues,
                                    final int enumValueIndex, final Map<String, Object> propertySchema,
                                    final String defaultEnumName) {
        if (format.equals("select")) {
            printLine(indent + "   " + "------");
            printLine(indent + "   " + "Select");
            printLine(indent + "   " + "------");
        } else if (!format.equals("combo") && propertySchema.get(DEFAULT) != null) {
            selectValues.remove(enumValueIndex);
        }

        for (String value : selectValues) {
            if (value.equals(defaultEnumName) || value.equals(propertySchema.get(DEFAULT))) {
                printLine(indent + " " + SELECT_PLACEHOLDER + " " + value);
            } else {
                printLine(indent + "   " + value);
            }
        }
    }

    private static void printUsertaskSimpleProperty(final String indent, final boolean informational,
                                                    final Map<String, Object> propertySchema, final boolean required,
                                                    final Object usertaskObject, final boolean readOnly, final boolean submit) {
        String propertyName = (String) propertySchema.get("name");
        final String nameExtra = (String) propertySchema.get("nameExtra");
        if (nameExtra != null && !nameExtra.isEmpty()) {
            propertyName += ": " + nameExtra;
        }
        final String propertyType = (String) propertySchema.get("type");
        final String propertyFormat = (String) propertySchema.get(FORMAT);

        if (propertyType.equals("integer")) {
            printUsertaskIntegerProperty(indent, propertySchema, required, usertaskObject, propertyName, submit);
        } else if (propertyType.equals("boolean")) {
            printUsertaskBooleanProperty(indent, informational, propertySchema, usertaskObject, propertyName, submit);
        } else if (propertySchema.get("items") != null) {
            printUsertaskItemsProperty(indent, propertySchema, required, usertaskObject, propertyName, readOnly, submit);
        } else if (propertyFormat != null && (propertyFormat.equals("informational") || propertyFormat.startsWith("message-"))) {
            if (submit) {
                return;
            }
            if (propertySchema.get(DEFAULT) != null) {
                printLine(indent + propertyName + ": " + propertySchema.get(DEFAULT).toString());
            } else if (propertyFormat.equals("message-info")) {
                printLine(indent + "{i} " + propertyName);
            } else if (propertyFormat.equals("message-warning")) {
                printLine(indent + "{!} " + propertyName);
            } else if (propertyFormat.equals("message-error")) {
                printLine(indent + "{x} " + propertyName);
            } else {
                printLine(indent + propertyName);
            }
        } else if ("informational-link".equals(propertyFormat)) {
            if (submit) {
                return;
            }
            printInformationalLink(indent, propertyName, propertySchema);
        } else if ("date-time".equals(propertyFormat)) {
            printDateTimeProperty(indent, propertySchema, required, usertaskObject, propertyName, readOnly, submit);
        } else {
            printUsertaskGenericProperty(indent, propertySchema, required, usertaskObject, propertyName, readOnly, submit);
        }
    }

    private static void printEmailProperty(String indent, Map<String, Object> propertySchema, final boolean required,
                                           Object usertaskObject, boolean readOnly, final boolean submit) {
        String propertyName = (String) propertySchema.get("name");
        final Map<String, Object> itemsSchema = (Map<String, Object>) propertySchema.get("items");
        final String nameExtra = (String) propertySchema.get("nameExtra");
        if (nameExtra != null && !nameExtra.isEmpty()) {
            propertyName += ": " + nameExtra;
        }

        List<String> emailAddresses = null;
        if (!readOnly && usertaskObject != null) {
            emailAddresses = (List<String>) usertaskObject;
        } else {
            if (propertySchema.get(DEFAULT) != null) {
                emailAddresses = (List<String>) propertySchema.get(DEFAULT);
            } else if (itemsSchema != null && itemsSchema.get(DEFAULT) != null) {
                emailAddresses = (List<String>) itemsSchema.get(DEFAULT);
            }
        }

        String csvEmailAddresses = NO_DEFAULT_PLACEHOLDER;
        if (emailAddresses != null) {
            csvEmailAddresses = String.join(",", emailAddresses);
        }
        printLine(indent + propertyName + printRequired(required, submit) + ": " + csvEmailAddresses);
    }

    private static void printInformationalLink(final String indent, final String propertyName, final Map<String, Object> propertySchema) {
        if (propertySchema.get(DEFAULT) != null && propertySchema.get(LINK_TEXT) != null) {
            final String propertyValue = propertySchema.get(DEFAULT).toString();
            final String linkText = propertySchema.get(LINK_TEXT).toString();
            printLine(String.format("%s: [%s|%s]", indent + propertyName, linkText, propertyValue));
        }
    }

    private static void printUsertaskGenericProperty(final String indent, final Map<String, Object> propertySchema, final boolean required,
                                                     final Object usertaskObject, final String propertyName, final boolean readOnly, final boolean submit) {
        String propertyValue = NO_DEFAULT_PLACEHOLDER;
        if (!readOnly && usertaskObject != null) {
            propertyValue = usertaskObject.toString();
        } else if (propertySchema.get(DEFAULT) != null) {
            propertyValue = propertySchema.get(DEFAULT).toString();
        }
        if (propertyName != null) {
            printLine(indent + propertyName + printRequired(required, submit) + ": " + propertyValue);
        } else {
            printLine(indent + propertyValue);
        }
    }

    private static void printUsertaskItemsProperty(final String indent, final Map<String, Object> propertySchema, final boolean required,
                                                   final Object usertaskObject, final String propertyName, final boolean readOnly, final boolean submit) {
        final Map<String, Object> items = (Map<String, Object>) propertySchema.get("items");
        printLine(indent + propertyName + printRequired(required, submit) + ":");

        final String propertyType = (String) items.get("type");

        if ("string".equals(propertyType)) {
            List<String> defaultValues = (List<String>) propertySchema.get(DEFAULT);
            if (defaultValues == null) {
                defaultValues = (List<String>) items.get(DEFAULT);
            }
            List<String> values;
            if (!readOnly && usertaskObject != null) {
                values = (List<String>) usertaskObject;
            } else {
                values = defaultValues;
            }
            if (values != null) {
                for (String value : values) {
                    printLine(indent + getItemsValuesDefaultString(value, defaultValues, submit) + value);
                }
            } else {
                printLine(indent + "   " + NO_DEFAULT_PLACEHOLDER);
            }
        } else if ("object".equals(propertyType)) {
            final Map<String, Object> itemProperties = (Map<String, Object>) items.get(PROPERTIES);
            List values;
            if (!readOnly && usertaskObject != null) {
                values = (List) usertaskObject;
            } else {
                values = (List) propertySchema.get(DEFAULT);
            }
            for (final Object o : values) {
                final Map<String, Object> value = (Map<String, Object>) o;
                for (final Map.Entry<String, Object> entry : itemProperties.entrySet()) {
                    final Map<String, Object> propertyOptionSchema = (Map<String, Object>) entry.getValue();
                    propertyOptionSchema.put(DEFAULT, value.get(entry.getKey()));
                    printUsertaskProperty(indent + "   ", false, false, propertyOptionSchema, false, null, submit);
                }
            }
        }
    }

    private static String getItemsValuesDefaultString(final String value, final List<String> values, final boolean submit) {
        if (submit || values == null || value == null || !values.contains(value)) {
            return "   ";
        } else {
            return " " + DEFAULT_PLACEHOLDER + " ";
        }
    }

    private static void printUsertaskBooleanProperty(final String indent, final boolean informational,
                                                     final Map<String, Object> propertySchema,
                                                     final Object usertaskObject, final String propertyName, final boolean submit) {
        final String propertyValue;
        Boolean booleanValue;
        if (usertaskObject != null) {
            booleanValue = (Boolean) usertaskObject;
        } else {
            booleanValue = (Boolean) propertySchema.get(DEFAULT);
        }

        if (booleanValue != null && booleanValue) {
            propertyValue = "Yes";
        } else {
            propertyValue = "No";
        }

        if (informational) {
            printLine(indent + propertyName + ": " + propertyValue);
        } else {
            if (propertyValue.equals("Yes")) {
                printLine(indent + "[x] " + propertyName);
            } else {
                printLine(indent + "[ ] " + propertyName);
            }
        }
    }

    private static void printUsertaskIntegerProperty(final String indent, final Map<String, Object> propertySchema, final boolean required,
                                                     final Object usertaskObject, final String propertyName, final boolean submit) {
        final String propertyValue;
        if (usertaskObject != null) {
            propertyValue = usertaskObject.toString();
        } else if (propertySchema.get(DEFAULT) != null) {
            propertyValue = propertySchema.get(DEFAULT).toString();
        } else {
            propertyValue = "0";
        }
        printLine(indent + propertyName + printRequired(required, submit) + ": " + propertyValue);
    }

    @SuppressWarnings("squid:S106")
    private static void printLine(final String line) {
        System.out.println("|  " + line);
    }

    private static String printRequired(final boolean required, final boolean submit) {
        if (required && !submit) {
            return " " + REQUIRED_PLACEHOLDER;
        }
        return "";
    }

    /*Display  the  date-time widget output on the DSL Tests Visualisation*/
    private static void printDateTimeProperty(final String indent, final Map<String, Object> propertySchema, final boolean required,
                                              final Object usertaskObject, final String propertyName, final boolean readOnly, final boolean submit) {

        String propertyValue = NO_DEFAULT_PLACEHOLDER;

        if (!readOnly && usertaskObject != null) {
            propertyValue = usertaskObject.toString();
        } else if (propertySchema.get(DEFAULT) != null) {
            propertyValue = propertySchema.get(DEFAULT).toString();
        }

        if (propertyName != null) {
            if (!propertyValue.equals(NO_DEFAULT_PLACEHOLDER)) {
                propertyValue = TestUtils.getDateTimeLocalFormat(propertyValue);
            }
            printLine(indent + propertyName + printRequired(required, submit) + ": " + propertyValue);
        } else {
            printLine(indent + propertyValue);
        }
    }

    private static String getRowSpacing(int priorityRowLength) {
        final String SPACING = StringUtils.repeat(" ", 23);
        int indent = "Priority Order".length() + SPACING.length() - priorityRowLength;
        String spacingIndent = "";
        for (int i = 0; i < indent; i++) {
            spacingIndent = spacingIndent + " ";
        }
        return spacingIndent;
    }

}