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

package com.ericsson.oss.services.flowautomation.flow.usertasks.schema.builder;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.NO_FLOW_EXECUTION_RESOURCE_AVAILABLE;
import static com.ericsson.oss.services.flowautomation.execution.resource.FlowExecutionResourceCategory.REPORT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ARRAY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.BOOLEAN;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.CHECKBOX;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DEFAULT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.DOWNLOAD_LINK;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.EDIT_TABLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.EMPTY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ENUM;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ENUM_NAMES;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.EXCUTION_SUMMARY;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FILE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FLOW_INPUT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.FORMAT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.HEADER;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.INTEGER;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.ITEMS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.LIST;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PERCENT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROGRESS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.STATUS;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.MESSAGE_ERROR;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.MESSAGE_INFO;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.MESSAGE_WARNING;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.NAME;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.OBJECT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.PROPERTIES;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.RADIO;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SELECT;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SELECT_LIST;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SELECT_TABLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.SHEET;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.STRING;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TABLE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TRUE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.TYPE;
import static com.ericsson.oss.services.flowautomation.flow.usertasks.schema.JsonSchemaKeywords.VALUE;
import static com.ericsson.oss.services.flowautomation.flow.utils.PayloadTransformer.transformJsonToMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.api.FlowExecutionService;
import com.ericsson.oss.services.flowautomation.entities.FlowExecutionEntity;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceEmptyException;
import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;
import com.ericsson.oss.services.flowautomation.flow.setting.FlowInputSchemaAndDataBuilder;
import com.ericsson.oss.services.flowautomation.flow.usertasks.UserTaskHelper;
import com.ericsson.oss.services.flowautomation.model.FlowExecution;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEvent;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionEventFilter;
import com.ericsson.oss.services.flowautomation.model.FlowExecutionResource;

/**
 * The Excel report generator class for setup, Execution report and Events.
 */

public class FlowExecutionExcelReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowExecutionExcelReportGenerator.class);
    final XSSFWorkbook workbook = new XSSFWorkbook();

    @Inject
    private FlowExecutionReportBuilder flowExecutionReportBuilder;

    @Inject
    private FlowInputSchemaAndDataBuilder flowInputSchemaAndDataBuilder;

    @Inject
    UserTaskHelper userTaskHelper;

    @Inject
    @ServiceType(JAR_TYPE)
    private FlowExecutionService flowExecutionService;

    /**
     * Geneartes Excel for setup data, Execution report and Events
     *
     * @param flowExecutionEntity the flow Execution Entity
     * @param flowExecution       the flow Execution
     * @return the FlowExecutionResource which has resource name, resource type and data as byte array
     */
    public FlowExecutionResource generateReportExcel(final FlowExecutionEntity flowExecutionEntity,
                                                     final FlowExecution flowExecution) {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final Map<String, Object> reportData = getFlowExecutionReportData(flowExecutionEntity, flowExecution);
            final Map<String, Object> reportSchema = getReportSchema(flowExecutionEntity);
            final Map<String, Object> flowInputSchemaData = getFlowInputSchemaData(flowExecutionEntity);

            if (MapUtils.isEmpty(reportData) && MapUtils.isEmpty(reportSchema) && MapUtils.isEmpty(flowInputSchemaData)) {
                LOGGER.info("Data is completely empty");
                throw new FlowResourceEmptyException(NO_FLOW_EXECUTION_RESOURCE_AVAILABLE, "Could not generate the Excel, all data is empty.");
            }

            final FlowExecutionEventFilter.Builder builder = new FlowExecutionEventFilter.Builder(flowExecutionEntity.getFlowId(), flowExecutionEntity.getFlowExecutionName());
            final List<FlowExecutionEvent> executionEventList = flowExecutionService.getExecutionEvents(builder.build()).getRecords();

            final XSSFWorkbook xssfWorkbook = createReportWorkBook(reportData, reportSchema, flowInputSchemaData, executionEventList);
            xssfWorkbook.write(baos);
            LOGGER.info("Writing Workbook to ByteArray Output Stream");
            return new FlowExecutionResource(String.format("%s-%s.xlsx", flowExecution.getName(), REPORT.getName()),
                    "report", baos.toByteArray());
        } catch (final IOException | FlowResourceNotFoundException e) {
            LOGGER.error("Error generating the report Excel Workbook: {}", e.getMessage());
            throw new FlowAutomationException(String.format("Could not generate the Excel Workbook: %s.", e.getMessage()), e);
        }

    }

    private XSSFWorkbook createReportWorkBook(final Map<String, Object> reportData, final Map<String, Object> reportSchema,
                                              final Map<String, Object> flowInputSchemaData, final List<FlowExecutionEvent> flowExecutionEventsList) {
        if (MapUtils.isNotEmpty(flowInputSchemaData)) {
            final Map<String, Object> flowInputProperties = (Map<String, Object>) flowInputSchemaData.get(PROPERTIES);
            LOGGER.debug("Creating flow input sheet with sheetName: {}", FLOW_INPUT);
            final XSSFSheet sheet = workbook.createSheet(FLOW_INPUT);
            final AtomicInteger rowNum = new AtomicInteger();
            writeSetupData(flowInputProperties, sheet, rowNum, false);
        }
        if (MapUtils.isNotEmpty(reportSchema) && MapUtils.isNotEmpty(reportData)) {
            final AtomicInteger sheetNum = new AtomicInteger();
            final Map<String, Object> reportSchemaProperties = (Map<String, Object>) reportSchema.get(PROPERTIES);
            reportSchemaProperties.forEach((key, value) -> {
                final Map<String, Object> schemaPropertyMap = (Map<String, Object>) ((Map<String, Object>) value)
                        .get(PROPERTIES);
                final Map<String, Object> dataPropertyMap = (Map<String, Object>) reportData.get(key);
                if (key.equalsIgnoreCase(HEADER)) {
                    final String sheetName = EXCUTION_SUMMARY;
                    writeSummaryProperties(schemaPropertyMap, dataPropertyMap, sheetName);
                } else {
                    writeLoneProperties(schemaPropertyMap, dataPropertyMap, SHEET + sheetNum.incrementAndGet());
                    writeNestedProperties(schemaPropertyMap, dataPropertyMap);
                }
            });
        }
        writeExecutionEvents(flowExecutionEventsList);
        return workbook;
    }

    private void writeExecutionEvents(final List<FlowExecutionEvent> flowExecutionEventsList) {
        final String events = "Events";
        final String type = "Type";
        final String time = "Time";
        final String resource = "Resource";
        final String message = "Message Synopsis";
        final String eventDetails = "Event Details";
        if (null != flowExecutionEventsList && !flowExecutionEventsList.isEmpty()) {
            LOGGER.debug("Creating execution events sheet with sheetName {}", events);
            final XSSFSheet sheet = workbook.createSheet(events);
            final AtomicInteger rowNum = new AtomicInteger();
            final AtomicInteger cellNum = new AtomicInteger();
            final ArrayList<String> eventHeader = new ArrayList<>(Arrays.asList(type, time, resource, message, eventDetails));
            final Row row = sheet.createRow(rowNum.getAndIncrement());
            eventHeader.forEach(event -> {
                final Cell cell = row.createCell(cellNum.getAndIncrement());
                final String cellValue = event;
                cell.setCellValue(cellValue);
                cell.setCellStyle(createCellStyle());
            });
            for (final FlowExecutionEvent flowExecutionEvent : flowExecutionEventsList) {
                final AtomicInteger cellDataNum = new AtomicInteger();
                final Row dataRow = sheet.createRow(rowNum.getAndIncrement());
                String cellValue = flowExecutionEvent.getSeverity();
                dataRow.createCell(cellDataNum.getAndIncrement()).setCellValue(cellValue);
                cellValue = flowExecutionEvent.getEventTime();
                dataRow.createCell(cellDataNum.getAndIncrement()).setCellValue(cellValue);
                cellValue = flowExecutionEvent.getTarget();
                dataRow.createCell(cellDataNum.getAndIncrement()).setCellValue(cellValue);
                cellValue = flowExecutionEvent.getMessage();
                dataRow.createCell(cellDataNum.getAndIncrement()).setCellValue(cellValue);
                cellValue = flowExecutionEvent.getEventData();
                dataRow.createCell(cellDataNum.getAndIncrement()).setCellValue(cellValue);
            }
        }
    }

    @SuppressWarnings("squid:S3776")
    private void writeSetupData(final Map<String, Object> inputSchemaDataMap, final XSSFSheet sheet, final AtomicInteger rowNum, final boolean newSheetRequired) {
        if (MapUtils.isNotEmpty(inputSchemaDataMap)) {
            inputSchemaDataMap.entrySet().stream().forEach(inputProp -> {
                final String format = (String) ((Map<String, Object>) inputProp.getValue()).get(FORMAT);
                final String type = (String) ((Map<String, Object>) inputProp.getValue()).get(TYPE);
                if (isPropertyNotToBeExported(format)) {
                    return;
                }
                final SchemaPropertyWrapper inputPropWrapper = new SchemaPropertyWrapper(inputProp.getKey(), (Map<String, Object>) inputProp.getValue());
                final Row row = sheet.createRow(rowNum.getAndIncrement());
                final AtomicInteger cellNum = new AtomicInteger();
                if (isSimpleProperty(format, type)) {
                    final Cell propertyKeyCell = row.createCell(cellNum.getAndIncrement());
                    final Map<String, Object> propertyMap = ((Map<String, Object>) inputProp.getValue());
                    final String propertyKey = (String) propertyMap.get(NAME);
                    propertyKeyCell.setCellValue(propertyKey);
                    final Cell propertyValueCell = row.createCell(cellNum.getAndIncrement());
                    final String propertyValue = getPropertyValue(propertyMap.get(DEFAULT), propertyMap);
                    if (newSheetRequired && STRING.equals(type) && StringUtils.isNotEmpty(propertyValue)) { // If the data needs to be stored in a separate new sheet inside the workbook.
                        propertyValueCell.setCellValue("View");
                        final String sheetName = inputProp.getKey();
                        final Sheet propertyValueSheet = workbook.createSheet(sheetName);
                        final XSSFHyperlink sheetLink = workbook.getCreationHelper().createHyperlink(HyperlinkType.DOCUMENT);
                        sheetLink.setAddress(String.format("'%s'!A1", sheetName));
                        propertyValueCell.setHyperlink(sheetLink);
                        propertyValueCell.setCellStyle(createHyperlinkStyle());

                        //Process New sheet
                        final String[] propertyValues = propertyValue.split(",");
                        for (int i = 0; i < propertyValues.length; i++) {
                            final Cell cell = propertyValueSheet.createRow(i).createCell((short) 0);
                            cell.setCellValue(propertyValues[i]);
                        }
                    } else {
                        propertyValueCell.setCellValue(propertyValue);
                    }
                    if (type.equalsIgnoreCase(OBJECT) && !FILE.equalsIgnoreCase(format)) {
                        writeSetupData((Map<String, Object>) ((Map<String, Object>) inputProp.getValue()).get(PROPERTIES), sheet, rowNum, isNewSheetRequired(format));
                    }
                } else if (type.equalsIgnoreCase(ARRAY)) {
                    if (LIST.equals(format)) {
                        final Cell propertyKeyCell = row.createCell(cellNum.getAndIncrement());
                        final Map<String, Object> propertyMap = ((Map<String, Object>) inputProp.getValue());
                        final String propertyKey = (String) propertyMap.get(NAME);
                        propertyKeyCell.setCellValue(propertyKey);
                        final Cell propertyValueCell = row.createCell(cellNum.getAndIncrement());
                        final String propertyValue = getPropertyValue(propertyMap.get(DEFAULT), propertyMap);
                        propertyValueCell.setCellValue(propertyValue);
                    } else if (TABLE.equals(format) || SELECT_TABLE.equals(format) || EDIT_TABLE.equals(format)) {
                        final SchemaPropertyWrapper propertiesWrapper = inputPropWrapper.getPropertyWrapper(ITEMS).getPropertyWrapper(PROPERTIES);
                        final List<Map<String, Object>> defaultValues = inputPropWrapper.getDefault();

                        Map<String, List<String>> content = new LinkedHashMap<>();
                        propertiesWrapper.forEach(property -> {
                            final List<String> tableItems = new ArrayList<>();
                            defaultValues.forEach(item -> {
                                tableItems.add(getPropertyValue(item.get(property.getKey()), (Map<String, Object>) propertiesWrapper.getMap().get(property.getKey())));
                            });

                            content.put(property.getName(), tableItems);
                        });

                        TableReport tableReport = new TableReport(inputPropWrapper.getName(), content);
                        writeSetupDataTable(row, tableReport);
                    }
                } else {
                    final Cell keyCell = row.createCell(cellNum.getAndIncrement());
                    final String keyVal = (String) ((Map<String, Object>) inputProp.getValue()).get(NAME);
                    keyCell.setCellValue(keyVal);
                    keyCell.setCellStyle(createCellStyle());
                    writeSetupData((Map<String, Object>) ((Map<String, Object>) inputProp.getValue()).get(PROPERTIES), sheet, rowNum, isNewSheetRequired(format));
                }

            });
        }
    }

    /**
     * Creates the cell style by formatting it with underscore and blue color.
     *
     * @return cell style for a hyperlink
     */
    private CellStyle createHyperlinkStyle() {
        final CellStyle cellStyle = workbook.createCellStyle();
        final Font font = workbook.createFont();
        font.setUnderline(Font.U_SINGLE);
        font.setColor(IndexedColors.BLUE.getIndex());
        cellStyle.setFont(font);
        return cellStyle;
    }

    /**
     * Returns true if the property value needs to be stored in a separate sheet.
     * e.g The properties inside the list are stored in their own sheets.
     *
     * @param format the format of the property
     * @return if a new sheet is required or not
     */
    private boolean isNewSheetRequired(final String format) {
        return LIST.equalsIgnoreCase(format) || TABLE.equalsIgnoreCase(format) || SELECT_TABLE.equalsIgnoreCase(format);
    }

    private boolean isSimpleProperty(final String format, final String type) {
        return isSelectableProperty(format) || FILE.equalsIgnoreCase(format) ||
                STRING.equalsIgnoreCase(type) || BOOLEAN.equalsIgnoreCase(type) || INTEGER.equalsIgnoreCase(type);
    }

    private boolean isPropertyNotToBeExported(final String format) {
        return MESSAGE_ERROR.equalsIgnoreCase(format) || MESSAGE_INFO.equalsIgnoreCase(format) || MESSAGE_WARNING.equalsIgnoreCase(format);
    }

    private boolean isSelectableProperty(final String format) {
        return RADIO.equalsIgnoreCase(format) || SELECT.equalsIgnoreCase(format)
                || CHECKBOX.equalsIgnoreCase(format);
    }

    @SuppressWarnings("squid:S3776")
    private String getPropertyValue(final Object propertyValue, final Map<String, Object> propertyMap) {
        final String format = (null == propertyMap || null == propertyMap.get(FORMAT)) ? EMPTY : (String) propertyMap.get(FORMAT);
        final String type = (String) propertyMap.get(TYPE);
        Object value;
        if (format.equalsIgnoreCase(SELECT) || format.equalsIgnoreCase(SELECT_LIST) || format.equalsIgnoreCase(SELECT_TABLE)) {
            value = getEnumNameForEnum(propertyMap, (String) propertyValue);
        } else if (type.equalsIgnoreCase(STRING) || type.equalsIgnoreCase(BOOLEAN)) {
            value = propertyValue;
        } else if (format.equalsIgnoreCase(RADIO)) {
            value = EMPTY;
        } else if (format.equalsIgnoreCase(CHECKBOX)) {
            value = TRUE;
        } else {
            value = propertyValue;
            final Map<String, Object> subProperties = (Map<String, Object>) propertyMap.get("properties");
            if (subProperties != null && value instanceof Map) {
                final List<String> arrayValues = new ArrayList<>();
                final Map<String, Object> subValues = (Map<String, Object>) value;
                subValues.forEach((subKey, subValue) -> {
                    final Map<String, Object> subProperty = (Map<String, Object>) subProperties.get(subKey);
                    final String tempValue = subProperty != null ? getPropertyValue(subValue, subProperty) : EMPTY;
                    if (!tempValue.isEmpty()) {
                        String tempPropName = (String) subProperty.get(NAME);
                        if (!tempPropName.isEmpty()) {
                            tempPropName = tempPropName + ": ";
                        }
                        arrayValues.add(tempPropName + tempValue);
                    }
                });
                if (!arrayValues.isEmpty()) {
                    value = arrayValues;
                } else {
                    value = EMPTY;
                }
            }
        }
        if (!(value instanceof String)) {
            if (null == value) {
                return EMPTY;
            } else {
                if (value instanceof ArrayList) {
                    return String.join(",", (ArrayList) value);
                } else {
                    return value.toString();
                }
            }
        } else {
            return (String) value;
        }
    }

    private String getEnumNameForEnum(final Map<String, Object> propertySchema, final String value) {
        String name = value;
        final List<String> enums = (List<String>) propertySchema.get(ENUM);
        final List<String> enumNames = (List<String>) propertySchema.get(ENUM_NAMES);
        if (null == enums) {
            return name;
        }
        if (enumNames != null) {
            for (int i = 0; i < enums.size(); i++) {
                if (enums.get(i).equals(value)) {
                    name = enumNames.get(i);
                }
            }
        }
        return name;
    }

    private void writeSummaryProperties(final Map<String, Object> propertiesMap, final Map<String, Object> dataMap,
                                        final String sheetName) {
        LOGGER.debug("Creating execution summary sheet with sheetName {}", sheetName);
        final XSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.setDefaultColumnWidth(20);
        int rownum = 0;
        final AtomicInteger cellnum = new AtomicInteger();
        final Row keyRow = sheet.createRow(rownum);
        rownum++;
        final Row valRow = sheet.createRow(rownum);
        propertiesMap.entrySet().forEach(prop -> {
            if (null != dataMap.get(prop.getKey())) {
                final Cell keyCell = keyRow.createCell(cellnum.get());
                final String key = (String) ((Map<String, Object>) prop.getValue()).get(NAME);
                keyCell.setCellValue(key);
                keyCell.setCellStyle(createCellStyle());
                final Cell valCell = valRow.createCell(cellnum.getAndIncrement());
                final Object value = dataMap.get(prop.getKey());
                if (!(value instanceof String)) {
                    valCell.setCellValue(value.toString());
                } else {
                    valCell.setCellValue((String) value);
                }
            }
        });
    }

    private void writeLoneProperties(final Map<String, Object> propertiesMap, final Map<String, Object> dataMap, final String sheetName) {
        final Map<String, Object> loneProps = new LinkedHashMap<>();
        propertiesMap.forEach((key, value) -> {
            final Map<String, Object> schemaObject = ((Map<String, Object>) value);
            final Map<String, Object> schemaObjectProperties = (Map<String, Object>) schemaObject.get(PROPERTIES);
            if (MapUtils.isEmpty(schemaObjectProperties)) {
                propertiesMap.entrySet().forEach(prop -> {
                    final String propType = (String) ((Map<String, Object>) prop.getValue()).get(TYPE);
                    final String propFormat = (String) ((Map<String, Object>) prop.getValue()).get(FORMAT);
                    if (INTEGER.equalsIgnoreCase(propType) || STRING.equalsIgnoreCase(propType) || PROGRESS.equals(propFormat)) {
                        loneProps.put(prop.getKey(), prop.getValue());
                    }
                });
            }
        });
        if (MapUtils.isNotEmpty(loneProps)) {
            writeSimpleProperties(loneProps, dataMap, sheetName);
        }
    }

    private void writeNestedProperties(final Map<String, Object> propertiesMap, final Map<String, Object> dataMap) {
        propertiesMap.entrySet().forEach(property -> {
            final Map<String, Object> schemaObject = ((Map<String, Object>) property.getValue());
            final Map<String, Object> schemaObjectProperties = (Map<String, Object>) schemaObject.get(PROPERTIES);
            final String sheetName = StringUtils.isNotEmpty((String) ((Map<String, Object>) property.getValue()).get(NAME))
                    ? (String) ((Map<String, Object>) property.getValue()).get(NAME)
                    : (String) ((Map<String, Object>) property.getValue()).get("description");
            if (dataMap.get(property.getKey()) instanceof ArrayList) {
                writeTableProperties((Map<String, Object>) schemaObject.get(ITEMS),
                        (ArrayList<Object>) dataMap.get(property.getKey()), sheetName);
            } else if (!(MapUtils.isEmpty(schemaObjectProperties) || PROGRESS.equals(((Map<String, Object>) property.getValue()).get(FORMAT)))) {
                if (schemaObjectProperties.entrySet().stream()
                        .allMatch(prop -> ((Map<String, Object>) prop.getValue()).get(TYPE).equals(OBJECT))) {
                    writeNestedProperties(schemaObjectProperties, (Map<String, Object>) dataMap.get(property.getKey()));
                } else if (schemaObjectProperties.entrySet().stream()
                        .allMatch(prop -> null != ((Map<String, Object>) prop.getValue()).get(FORMAT))) {
                    writeSimpleProperties(schemaObjectProperties, (Map<String, Object>) dataMap.get(property.getKey()),
                            sheetName);
                } else {
                    writeSummaryProperties(schemaObjectProperties, (Map<String, Object>) dataMap.get(property.getKey()),
                            sheetName);
                }
            }
        });
    }

    private void writeSimpleProperties(final Map<String, Object> propertiesMap, final Map<String, Object> dataMap,
                                       final String sheetName) {
        LOGGER.debug("Creating lone properties sheet with sheetName {}", sheetName);
        final XSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.setDefaultColumnWidth(20);
        final AtomicInteger rowNum = new AtomicInteger();
        propertiesMap.forEach((key, value) -> {
            final Row row = sheet.createRow(rowNum.getAndIncrement());
            final AtomicInteger cellNum = new AtomicInteger();
            final Cell keyCell = row.createCell(cellNum.getAndIncrement());
            keyCell.setCellStyle(createCellStyle());
            final String keyVal = (String) ((Map<String, Object>) value).get(NAME);
            keyCell.setCellValue(keyVal);
            final Cell valCell = row.createCell(cellNum.getAndIncrement());
            final Object valueVal = dataMap.get(key);
            if (DOWNLOAD_LINK.equals(((Map<String, Object>) value).get(FORMAT))) {
                final String variableName = FlowExecutionReportVariableUtil.extractVariableNameFromUrl((String) valueVal);
                final XSSFHyperlink link = workbook.getCreationHelper().createHyperlink(HyperlinkType.FILE);
                link.setAddress(variableName);
                valCell.setHyperlink(link);
                valCell.setCellValue(variableName);
                valCell.setCellStyle(createHyperlinkStyle());
            } else if (PROGRESS.equals(((Map<String, Object>) value).get(FORMAT))) {
                LinkedHashMap progressData = (LinkedHashMap) valueVal;
                boolean found = false;
                if (progressData != null) {
                    String status = (String) progressData.get(STATUS);
                    LinkedHashMap valueData = (LinkedHashMap) progressData.get(VALUE);
                    if (valueData != null) {
                        int percent = (int) valueData.get(PERCENT);
                        valCell.setCellValue(percent + "%");
                        valCell.setCellStyle(createProgressCellStyle(status));
                        found = true;
                    }
                }
                if (!found) {
                    valCell.setCellValue("");
                }
            } else if (!(valueVal instanceof String)) {
                valCell.setCellValue(valueVal.toString());
                } else {
                    valCell.setCellValue((String) valueVal);
                }
        });
    }

    private void writeTableProperties(final Map<String, Object> propertiesMap, final ArrayList<Object> dataArray,
                                      final String sheetName) {
        LOGGER.debug("Creating report table sheet with sheetName {}", sheetName);
        final XSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.setDefaultColumnWidth(20);
        final AtomicInteger rowNum = new AtomicInteger();
        final Row keyRow = sheet.createRow(rowNum.getAndIncrement());
        final Map<String, Object> headerNames = (Map<String, Object>) propertiesMap.get(PROPERTIES);
        final AtomicInteger cellNum = new AtomicInteger();

        // Process the header for the table
        headerNames.entrySet().stream()
                .forEach(entry -> {
                    final Cell keyCell = keyRow.createCell(cellNum.getAndIncrement());
                    final String key = (String) ((Map<String, Object>) entry.getValue()).get(NAME);
                    keyCell.setCellValue(key);
                    keyCell.setCellStyle(createCellStyle());
                });

        //Process the values (rows/columns) in the table
        dataArray.forEach(property -> {
            cellNum.set(0);
            final Row valRow = sheet.createRow(rowNum.getAndIncrement());
            headerNames.entrySet().stream()
                    .forEach(schemaEntry -> {
                        final Cell valCell = valRow.createCell(cellNum.getAndIncrement());
                        Object entry = ((Map<String, Object>) property).get(schemaEntry.getKey());
                        if (entry != null) {
                            if (DOWNLOAD_LINK.equals(((Map<String, Object>) schemaEntry.getValue()).get(FORMAT))) {
                                final String variableName = FlowExecutionReportVariableUtil.extractVariableNameFromUrl(String.valueOf(entry));
                            final XSSFHyperlink link = workbook.getCreationHelper().createHyperlink(HyperlinkType.FILE);
                            link.setAddress(variableName);
                            valCell.setHyperlink(link);
                            valCell.setCellValue(variableName);
                            } else if (PROGRESS.equals(((Map<String, Object>) schemaEntry.getValue()).get(FORMAT))) {
                                LinkedHashMap progressData = (LinkedHashMap) entry;
                                if (progressData != null) {
                                    String status = (String) progressData.get(STATUS);
                                    LinkedHashMap valueData = (LinkedHashMap) progressData.get(VALUE);
                                    if (valueData != null) {
                                        int percent = (int) valueData.get(PERCENT);
                                        valCell.setCellValue(percent + "%");
                                        valCell.setCellStyle(createProgressCellStyle(status));
                                    }
                                }
                        } else {
                                valCell.setCellValue(String.valueOf(entry));
                            }
                        } else {
                            valCell.setCellValue("");
                        }
                    });
        });
    }

    /**
     * The row must be a newly created row
     * @param row
     * @param tableReport
     */
    private void writeSetupDataTable(final Row row, TableReport tableReport) {
        final AtomicInteger cellNum = new AtomicInteger();
        final Cell propertyKeyCell = row.createCell(cellNum.getAndIncrement());
        propertyKeyCell.setCellValue(tableReport.getTableName());

        if (tableReport.hasContent()) {
            final XSSFHyperlink sheetLink = workbook.getCreationHelper().createHyperlink(HyperlinkType.DOCUMENT);
            sheetLink.setAddress(String.format("'%s'!A1", tableReport.getSheetName()));

            final Cell propertyValueCell = row.createCell(cellNum.getAndIncrement());
            propertyValueCell.setCellValue("View");
            propertyValueCell.setHyperlink(sheetLink);
            propertyValueCell.setCellStyle(createHyperlinkStyle());

            writeSetupDataTableProperties(tableReport);
        }
    }

    private void writeSetupDataTableProperties(final TableReport tableReport) {
        final Sheet sheet = workbook.createSheet(tableReport.getSheetName());
        sheet.setDefaultColumnWidth(20);
        final AtomicInteger rowNum = new AtomicInteger();
        final Row headerRow = sheet.createRow(rowNum.getAndIncrement());

        AtomicInteger cellNum = new AtomicInteger(-1);
        tableReport.getHeaders().forEach(header -> {
            Cell headerCell = headerRow.createCell(cellNum.incrementAndGet());
            headerCell.setCellValue(header);
            headerCell.setCellStyle(createCellStyle());

            rowNum.set(0);
            tableReport.getValuesByHeader(header).forEach(value -> {
                Row valueRow = sheet.getRow(rowNum.incrementAndGet());
                if (valueRow == null) {
                    valueRow = sheet.createRow(rowNum.get());
                }
                Cell valCell = valueRow.createCell(cellNum.get());
                valCell.setCellValue(value);
            });
        });
    }

    private XSSFCellStyle createCellStyle() {
        final XSSFCellStyle style = workbook.createCellStyle();
        final XSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private XSSFCellStyle createProgressCellStyle(String status) {
        final XSSFCellStyle style = workbook.createCellStyle();
        final XSSFFont font = workbook.createFont();
        if (status != null) {
            if (status.equals("inProgress")) {
                font.setColor(IndexedColors.BLUE.getIndex());
            } else if (status.equals("success")) {
                font.setColor(IndexedColors.GREEN.getIndex());
            } else if (status.equals("error")) {
                font.setColor(IndexedColors.RED.getIndex());
            }
        }
        style.setFont(font);
        return style;
    }

    private Map<String, Object> getReportSchema(final FlowExecutionEntity flowExecutionEntity) {
        try {
            return transformJsonToMap(
                    flowExecutionReportBuilder.getFlowExecutionReportSchema(flowExecutionEntity));
        } catch (final IOException | FlowResourceNotFoundException e) {
            LOGGER.info("Report Schema not available for the flow: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> getFlowExecutionReportData(final FlowExecutionEntity flowExecutionEntity,
                                                           final FlowExecution flowExecution) {
        try {
            return transformJsonToMap(
                    flowExecutionReportBuilder.getFlowExecutionReport(flowExecutionEntity, flowExecution));
        } catch (final IOException | FlowResourceNotFoundException e) {
            LOGGER.info("Report data not available for the flow: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<String, Object> getFlowInputSchemaData(FlowExecutionEntity flowExecutionEntity) {
        try {
            return flowInputSchemaAndDataBuilder.getFlowInputSchemaData(flowExecutionEntity);
        } catch (final FlowResourceNotFoundException e) {
            LOGGER.info("Setup phase not available. Cannot write setup data: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}