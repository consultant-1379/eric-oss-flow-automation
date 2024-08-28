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

package com.ericsson.oss.services.flowautomation.flow.utils;

import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.EMPTY_FLOW_EXECUTION_INPUT;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_EXECUTION_SETUP_INPUT_FILE_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_INPUT_FILE_NOT_UPLOADED;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.FLOW_INPUT_ZIP_CORRUPT;
import static com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode.SETUP_FOLDER_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.flow.utils.FlowPackageUtil.extractFileBytes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.error.FlowExecutionErrorCode;

public final class FlowExecutionUtil {

    private static final String SETUP_FOLDER = "setup/";
    private static final String FLOW_INPUT_FILE = "flow-input.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowPackageUtil.class);

    private FlowExecutionUtil (){ throw new IllegalAccessError();}

    /**
     * Extracts flow execution setup input files
     * @param zipFile
     * @return
     * @throws IOException
     */
    public static Map<String, byte[]> extractFlowExecutionSetupInputFiles(final byte[] zipFile) throws IOException {
        byte[] fileBytes = null;
        final Map<String, byte[]> flowInputFiles = new HashMap<>();
        try (ZipInputStream stream = new ZipInputStream(new ByteArrayInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                fileBytes = extractFileBytes(stream);
                String fileName = "";
                if(entry.getName().contains(SETUP_FOLDER)) {
                    fileName = entry.getName().substring(6);
                }
                flowInputFiles.put(fileName, fileBytes);
                stream.closeEntry();
            }
        }
        return flowInputFiles;
    }

    /**
     * Validates flow execution setup input files
     * @param file
     * @return
     */
    public static FlowExecutionErrorCode validateFlowExecutionSetupInput(final byte[] file) {
        if (file == null) {
            LOGGER.error("No flow execution input file uploaded");
            return FLOW_EXECUTION_SETUP_INPUT_FILE_NOT_FOUND;
        }
        try (final ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(file))) {
            ZipEntry entry = zipStream.getNextEntry();
            if (entry == null) {
                LOGGER.error("Empty input zip file");
                return EMPTY_FLOW_EXECUTION_INPUT;
            }

            boolean flowInputConfigExists = false;
            boolean setupFolderExists = false;
            while (entry != null) {
                entry.getCrc();
                entry.getCompressedSize();
                final String entryName = entry.getName();

                if (!setupFolderExists && entryName.startsWith(SETUP_FOLDER)) {
                    setupFolderExists = true;
                }

                if (!flowInputConfigExists && entryName.endsWith(FLOW_INPUT_FILE)) {
                    flowInputConfigExists = true;
                }
                zipStream.closeEntry();
                entry = zipStream.getNextEntry();
            }
            if (!flowInputConfigExists) {
                LOGGER.error("No flow-input.json file provided in the input zip file.");
                return FLOW_INPUT_FILE_NOT_UPLOADED;
            }

            if (!setupFolderExists) {
                LOGGER.error("No setup folder in the input zip file");
                return SETUP_FOLDER_NOT_FOUND;
            }

        } catch (final IOException e) {
            LOGGER.error("Failed to process flow input zip due to error: {}", e.getMessage());
            return FLOW_INPUT_ZIP_CORRUPT;
        }
        return null;
    }

    public static boolean isSupportedSchemaType(final Object value){
        return value instanceof Boolean || value instanceof Character || value instanceof Byte
                || value instanceof Short || value instanceof Integer || value instanceof Long
                || value instanceof Float || value instanceof Double || value instanceof String
                || value instanceof List;
    }
}
