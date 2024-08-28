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

import static java.lang.String.format;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

    private static final String JAR_FILE = "jar:file:";
    private static final String JAR = "jar:";
    private static final String FLOWS = "flows";
    private static final String INTERNAL_FLOWS = "internalFlows";
    private static final String TESTFWK_FLOWS = "testfwk-flows";
    private static final char MARK = '!';

    public static final DateTimeFormatter FA_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final DateTimeFormatter ISO_RFC822_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    public static final DateTimeFormatter DATE_TIME_FORMATTER_SQL = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy");
    private static final String LITERAL_Z = "Z";

    private TestUtils() {
        //
    }

    /**
     * Gets the file bytes for the flow package
     * @param flowPackageName
     * @param fileName
     * @return
     */
    public static byte[] getFlowdataFileBytes(final String flowPackageName, final String fileName) {
        try {
            final URL flowsResourceUrl = Thread.currentThread().getContextClassLoader().getResource(FLOWS);
            if (flowsResourceUrl.toString().startsWith(JAR)) {
                final String flowsResource = flowsResourceUrl.toString();
                String jarFilePath = flowsResource.substring(0, flowsResource.lastIndexOf('!'));
                jarFilePath = jarFilePath.substring(JAR_FILE.length());
                return getFlowdataFileBytesJar(flowPackageName, fileName, jarFilePath);
            } else {
                return getFlowdataFileBytesFile(flowPackageName, fileName);
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("getFlowdataFileBytes() Failed to process: {}", e.getMessage()); //OWASP10
            fail("getFlowdataFileBytes() Failed to process: " + e.getMessage());
            return new byte[]{};
        }
    }

    /**
     * Gets file bytes from flow data Jar package
     * @param flowPackageName
     * @param fileName
     * @param flowsJarFilePath
     * @return
     * @throws IOException
     */
    public static byte[] getFlowdataFileBytesJar(final String flowPackageName, final String fileName, final String flowsJarFilePath)
            throws IOException {
        final String filePath = format("flowdata/%s/%s", flowPackageName, fileName);
        byte[] fileBytes = null;

        try (final ZipInputStream flowsJarZipInputStream = new ZipInputStream(new FileInputStream(flowsJarFilePath))) {
            ZipEntry entry = flowsJarZipInputStream.getNextEntry();
            while (entry != null) {
                if (entry.getName().equals(filePath)) {
                    fileBytes = extractFile(flowsJarZipInputStream);
                    break;
                }
                flowsJarZipInputStream.closeEntry();
                entry = flowsJarZipInputStream.getNextEntry();
            }
        }

        return fileBytes;
    }

    private static byte[] getFlowdataFileBytesFile(final String flowPackageName, final String fileName) throws IOException, URISyntaxException {
        final String filePath = format("flowdata/%s/%s", flowPackageName, fileName);
        return Files.readAllBytes(Paths.get(Thread.currentThread().getContextClassLoader().getResource(filePath).toURI()));
    }

    /**
     * Gets flow package bytes
     * @param flowPackageName
     * @return
     */
    public static byte[] getFlowPackageBytes(final String flowPackageName) {
        byte[] packageBytes = null;
        packageBytes = getFlowPackageBytes(FLOWS, flowPackageName);

        if (packageBytes != null && packageBytes.length == 0) {
            packageBytes = getFlowPackageBytes(INTERNAL_FLOWS, flowPackageName);
        }
        if (packageBytes != null && packageBytes.length == 0) {
            packageBytes = getFlowPackageBytes(TESTFWK_FLOWS, flowPackageName);
        }

        return packageBytes;
    }

    public static byte[] getFlowPackageBytes(final String flowLocation, final String flowPackageName) {
        final URL flowsResourceUrl = Thread.currentThread().getContextClassLoader().getResource(flowLocation);
        byte[] packageBytes = null;

        try {
            if (flowsResourceUrl.toString().startsWith(JAR)) {
                final String flowsResource = flowsResourceUrl.toString();
                String jarFilePath = flowsResource.substring(0, flowsResource.lastIndexOf(MARK));
                jarFilePath = jarFilePath.substring(JAR_FILE.length());
                packageBytes = getFlowPackageBytesJar(flowLocation, flowPackageName, jarFilePath);
            } else {
                packageBytes = getFlowPackageBytesFile(flowLocation, flowPackageName);
            }
        } catch (IOException e) {
            LOGGER.error("getFlowPackageBytes Failed to process: {}", e.getMessage()); //OWASP10
            fail("getFlowPackageBytes Failed to process: " + e.getMessage());
        }
        return packageBytes;
    }

    /**
     * Gets flow package bytes from Jar package
     * @param flowLocation
     * @param flowPackageName
     * @param flowsJarFilePath
     * @return
     * @throws IOException
     */
    @SuppressWarnings("squid:S1075")
    public static byte[] getFlowPackageBytesJar(final String flowLocation, final String flowPackageName, final String flowsJarFilePath)
            throws IOException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);
        boolean emptyPackage = true;

        try (ZipInputStream flowsJarZipInputStream = new ZipInputStream(new FileInputStream(flowsJarFilePath))) {
            ZipEntry entry = flowsJarZipInputStream.getNextEntry();
            while (entry != null) {
                final String sourceFileName = entry.getName();
                final String initialPath = flowLocation + "/" + flowPackageName + "/";

                if (!entry.isDirectory() && sourceFileName.startsWith(initialPath)) {
                    final String destPathName = sourceFileName.substring(initialPath.length());
                    zos.putNextEntry(new ZipEntry(destPathName));

                    final byte[] fileBytes = extractFile(flowsJarZipInputStream);
                    zos.write(fileBytes);
                    zos.closeEntry();
                    emptyPackage = false;
                }
                flowsJarZipInputStream.closeEntry();
                entry = flowsJarZipInputStream.getNextEntry();
            }
        } finally {
            zos.close();
        }
        return emptyPackage ? new byte[0] : baos.toByteArray();
    }

    private static byte[] extractFile(final ZipInputStream zipInputStream) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final byte[] bytesIn = new byte[1024];
        int read = 0;
        while ((read = zipInputStream.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();

        return bos.toByteArray();
    }

    /**
     * Gets flow package bytes from file
     * @param flowLocation
     * @param flowPackageName
     * @return
     */
    public static byte[] getFlowPackageBytesFile(final String flowLocation, final String flowPackageName) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);

        try {
            final Path dir = Paths.get(Thread.currentThread().getContextClassLoader().getResource(flowLocation + "/" + flowPackageName).toURI());
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    final String path = dir.relativize(file).toString().replace("\\", "/");

                    zos.putNextEntry(new ZipEntry(path));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
            zos.close();
        } catch (final Exception e) {
            LOGGER.info("getFlowPackageBytesFile() flowPackageName {} not found at location {}. Error: {}", flowPackageName, flowLocation, e.getMessage());
            return new byte[0];
        }
        return baos.toByteArray();
    }

    /**
     * Copies files from the given location and returns the destination path
     * @param srcPath
     * @return
     */
    public static String copyFileFromResources(final String srcPath) {
        try {
            Path destPath = Files.createTempFile("", ".sh");
            Files.copy(Paths.get(Thread.currentThread().getContextClassLoader().getResource(srcPath).toURI()), destPath, REPLACE_EXISTING);
            new File(destPath.toAbsolutePath().toString()).deleteOnExit();
            return destPath.toAbsolutePath().toString();
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("copyFileFromResources() Failed to process: {}", e.getMessage()); //OWASP10
            fail("copyFileFromResources() Failed to process: " + e.getMessage());
        }
        return null;
    }

    /**
     * Sets the given posix permissions on the file
     * @param filePath
     * @param permissions
     */
    public static void setPosixFilePermissions(final String filePath, final String permissions) {
        try {
            Files.setPosixFilePermissions(Paths.get(filePath), PosixFilePermissions.fromString(permissions));
        } catch (IOException e) {
            LOGGER.error("setPosixFilePermissions() Failed to process: {}", e.getMessage()); //OWASP10
            fail("setPosixFilePermissions() Failed to process: " + e.getMessage());
        }
    }

    /**
     * sleeps for given milli seconds
     * @param ms
     */
    @SuppressWarnings("squid:S2925")
    public static void realSleepMs(final long ms) {
        try {
            Thread.sleep(ms);
        } catch (final Exception e) {
            LOGGER.error("Failed to wait for task, due to: {}", String.valueOf(e));
        }
    }

    public static String getDateTimeLocalFormat(String stringDate) {

        if (stringDate == null || stringDate.isEmpty()) {
            return stringDate;
        }

        LocalDateTime ldt;
        boolean isEndWithZ = stringDate.endsWith(LITERAL_Z);

        try {
            if (isEndWithZ) {
                ldt = LocalDateTime.parse(stringDate, FA_DATE_TIME_FORMATTER);
            } else {
                //This condition is required because some NBI responses use the "yyyy-MM-dd'T'HH:mm:ss.SSSZ" pattern for backward compatibility reasons.
                ldt = LocalDateTime.parse(stringDate, ISO_RFC822_DATE_TIME_FORMATTER);
            }

            return ldt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
        } catch (DateTimeParseException e) {
            LOGGER.warn(e.getMessage());
            return "NaN-NaN-NaN NaN:NaN:NaN";
        }
    }
}
