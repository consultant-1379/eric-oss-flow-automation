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

import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.EXECUTION_PHASE_MISSING_IN_FLOW_PACKAGE;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_DEFINITION_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_PACKAGE_CORRUPT;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_PACKAGE_EMPTY;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_PACKAGE_NOT_FOUND;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_DEFINITION_ZIPFILE_PRESENT;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_DEFINITION_MAX_FILEPATH_LENGTH_EXCEEDED;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_DEFINITION_MAX_FILES_NUMBER_EXCEEDED;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.ericsson.oss.services.flowautomation.flow.exception.FlowResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode;

/**
 * This is a utility class to read the resources inside zip file.
 */
public final class FlowPackageUtil {

    public static final String FORWARD_SLASH = "/";

    private static final String BPMN_SUFFIX = ".bpmn";

    private static final String EXECUTE_FOLDER = "execute/";

    public static final String FLOW_DEFINITION_FILE_NAME = "flow-definition.json";

    public static final String FLOW_REPORT_SCHEMA_FILE_NAME = "report/flow-report-schema.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowPackageUtil.class);

     /* Max number of files */
    private static final int MAX_FILES_NUMBER = 1024;

    /* Max length of file path */
    private static final int MAX_FILEPATH_LENGTH = 255;

    /**
     * Utility class, to avoid instantiation.
     */
    private FlowPackageUtil() {
        throw new IllegalAccessError();
    }

    /**
     * Extract file.
     *
     * @param zipInputStream the zip input stream
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static byte[] extractFileBytes(final ZipInputStream zipInputStream) throws IOException {
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            final byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = zipInputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            return bos.toByteArray();
        }
    }

    /**
     * This method just simply checks if the zipFile passed in is null
     * If it is null, will return true,
     *
     * @param zipFile the zip input
     * @return true if zipFile is null, false if it is not empty
     */
    public static boolean checkIfZipIsNull(final byte[] zipFile) {
        return (zipFile == null);
    }

    /**
     * This method just simply checks if the zipFile passed in is empty
     * If it is empty, will return true,
     *
     * @param zipFile the zip input
     * @return true if zipFile is empty, false if it is not empty
     */
    public static boolean checkIfZipIsEmpty(final byte[] zipFile) {
        return (zipFile.length == 0);
    }

    /**
     * This method iterates through all entries in the zip archive. Each entry is checked against the predicate (filter) that is passed to the method.
     * If the filter returns true, the entry is expanded to extract the file content otherwise returns null. If the predicate matches more than one
     * files inside the {@code zipFile}, the first match will be returned.
     *
     * @param zipFile the zip input
     * @param filter  the predicate used to compare each entry against
     * @return the optional
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static Optional<byte[]> extractFile(final byte[] zipFile, final Predicate<ZipEntry> filter) throws IOException {
        byte[] fileBytes = null;
        try (ZipInputStream stream = new ZipInputStream(new ByteArrayInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                if (filter.test(entry)) {
                    fileBytes = extractFileBytes(stream);
                    break;
                }
                stream.closeEntry();
            }
        }
        return Optional.ofNullable(fileBytes);
    }

    /**
     * Validates the flow package.
     *
     * @param value the value
     * @return the flow package error code
     */
    public static FlowPackageErrorCode validate(final byte[] value) {
        if (value == null) {
            LOGGER.error("No flow package uploaded to perform validation.");
            return FLOW_PACKAGE_NOT_FOUND;
        }

        try (final ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(value))) {
            ZipEntry entry = zipStream.getNextEntry();
            if (entry == null) {
                LOGGER.error("Empty flow package uploaded.");
                return FLOW_PACKAGE_EMPTY;
            }

            int entries = 0;
            boolean flowDefinitionExists = false;
            boolean executePhaseExists = false;
            while (entry != null) {
                entry.getCrc();
                entry.getCompressedSize();

                final String entryName = entry.getName();

                // TODO we need to add tests for these new use cases, also note there was some issues with JSE tests as is
                // TODO may need to look at explicitly throwing the relevant exception
                //FileUtils.sizeOfDirectory(entry.);
                if (entryName.toLowerCase().endsWith(".zip")) {
                    LOGGER.error("No zip file should present inside the flow package zip file");
                    return FLOW_DEFINITION_ZIPFILE_PRESENT;
                }

                if (entryName.length() > MAX_FILEPATH_LENGTH) {
                    LOGGER.error("File path length should not exceed 255");
                    return FLOW_DEFINITION_MAX_FILEPATH_LENGTH_EXCEEDED;
                }

                if (!flowDefinitionExists && FLOW_DEFINITION_FILE_NAME.equals(entryName)) {
                    flowDefinitionExists = true;
                } else if (!executePhaseExists && entryName.startsWith(EXECUTE_FOLDER) && entryName.endsWith(BPMN_SUFFIX)) {
                    executePhaseExists = true;
                }

                zipStream.closeEntry();
                entries++;
                if (entries > MAX_FILES_NUMBER) {
                    LOGGER.error("Number of entries in the flow package should not exceed 1024");
                    return FLOW_DEFINITION_MAX_FILES_NUMBER_EXCEEDED;
                }

                entry = zipStream.getNextEntry();
            }
            if (!flowDefinitionExists) {
                LOGGER.error("No flow definition file provided in flow package.");
                return FLOW_DEFINITION_NOT_FOUND;
            }

            if (!executePhaseExists) {
                LOGGER.error("No execution phase bpmn files provided in flow package.");
                return EXECUTION_PHASE_MISSING_IN_FLOW_PACKAGE;
            }

        } catch (final IOException e) {
            LOGGER.error("Failed to process flow package due to error: {}", e.getMessage());
            return FLOW_PACKAGE_CORRUPT;
        }

        return null;
    }

    public static boolean checkZipForFlowReportSchema(ZipInputStream zipInputStream) {
        try {
            ZipEntry entry;
            LOGGER.info("Parsing zip-stream for 'report/flow-report-schema.json'");
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().equals(FLOW_REPORT_SCHEMA_FILE_NAME)) {
                    return true;
                }
                zipInputStream.closeEntry();
            }
        } catch (Exception e) {
            throw new FlowResourceNotFoundException(e);
        } finally {
            try {
                zipInputStream.close();
                LOGGER.info("Zip-stream closed");
            } catch (Exception e) {
                LOGGER.error("Error closing zip-stream");
            }
        }
        return false;
    }
}