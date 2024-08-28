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

package com.ericsson.oss.services.flowautomation.deployment.internalflows;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flow.definition.FlowConfig;
import com.ericsson.oss.services.flowautomation.flow.definition.FlowDefinitionParser;
import com.ericsson.oss.services.flowautomation.internalflows.FlowAutomationInternalFlows;
import com.ericsson.oss.services.flowautomation.exception.FlowAutomationException;

/**
 * The Class InternalFlowsDeployer.
 */
public class InternalFlowsDeployer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalFlowsDeployer.class);

    private static final String INTERNAL_FLOWS_LOCATION = "internalFlows/";

    private static final String JAR_FILE = "jar:file:";
    private static final String JAR = "jar:";
    private static final String VFS_PROTOCOL = "vfs:";
    private static final char MARK = '!';

    private final Map<String, byte[]> unProcessedInternalFlows = new HashMap<>();

    public InternalFlowsDeployer() {
        //
    }

    /**
     * Load flow automation internal flows.
     */
    public void loadFlowAutomationInternalFlows() {
        LOGGER.info("Loading internal flows..");
        try {
            final URL internalFlowsUrl = Thread.currentThread().getContextClassLoader().getResource(INTERNAL_FLOWS_LOCATION);
            if (internalFlowsUrl.toString().startsWith(JAR)) {
                final String flowsResource = internalFlowsUrl.toString();
                String jarFilePath = flowsResource.substring(0, flowsResource.lastIndexOf(MARK));
                jarFilePath = jarFilePath.substring(JAR_FILE.length());
                getFlowPackageBytesJar(INTERNAL_FLOWS_LOCATION, jarFilePath);
            } else if (internalFlowsUrl.toString().startsWith(VFS_PROTOCOL)) {
                LOGGER.info("Getting internal flows from location: {}, vfs protocol.", INTERNAL_FLOWS_LOCATION);
                loadFlowAutomationInternalFlowsWithVfsProtocol();
            } else {
                LOGGER.error("Invalid protocol: {} for FA internal flows", internalFlowsUrl);
            }
        } catch (final IOException e) {
            LOGGER.error("IO error while loading internal flows from location: {}. Exception: {}", INTERNAL_FLOWS_LOCATION, e.getMessage());
            throw new FlowAutomationException("IO error while loading internal flows from location. " + e.getMessage(), e);
        }
    }

    /**
     * Internal flows available.
     *
     * @return true, if there is at least one internal flow
     */
    public boolean internalFlowsAvailable() {
        return unProcessedInternalFlows.size() > 0;
    }

    /**
     * Gets the internal flows.
     *
     * @return the internal flows
     */
    public Map<String, byte[]> getInternalFlows() {
        return unProcessedInternalFlows;
    }

    /**
     * Gets the flow package bytes jar.
     *
     * @param flowPackageName  the flow package name
     * @param flowsJarFilePath the flows jar file path
     * @return the flow package bytes jar
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @SuppressWarnings("squid:S1075")
    private void getFlowPackageBytesJar(final String flowPackageName, final String flowsJarFilePath) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);

        try (final ZipInputStream flowsJarZipInputStream = new ZipInputStream(new FileInputStream(flowsJarFilePath))) {
            ZipEntry entry = flowsJarZipInputStream.getNextEntry();
            while (entry != null) {
                final String sourceFileName = entry.getName();
                final String initialPath = INTERNAL_FLOWS_LOCATION + flowPackageName + "/";
                if (!entry.isDirectory() && sourceFileName.startsWith(initialPath)) {
                    final String destPathName = sourceFileName.substring(initialPath.length());
                    zos.putNextEntry(new ZipEntry(destPathName));

                    final byte[] fileBytes = extractFile(flowsJarZipInputStream);
                    zos.write(fileBytes);
                    zos.closeEntry();
                }
                flowsJarZipInputStream.closeEntry();
                entry = flowsJarZipInputStream.getNextEntry();
            }
        } finally {
            zos.close();
        }

        cacheInternalFlow(baos, flowPackageName);
    }

    /**
     * Extract file.
     *
     * @param zipInputStream the zip input stream
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
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
     * Load flow automation internal flows with vfs protocol.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void loadFlowAutomationInternalFlowsWithVfsProtocol() throws IOException {

        final Enumeration<URL> internalFlowsURLs = getClass().getClassLoader().getResources(INTERNAL_FLOWS_LOCATION);
        while (internalFlowsURLs.hasMoreElements()) {
            final URL internalFlowsURL = internalFlowsURLs.nextElement();
            final URLConnection conn = internalFlowsURL.openConnection();
            final VirtualFile virtualFile = (VirtualFile) conn.getContent();
            cacheInternalFlows(virtualFile);
        }
    }

    /**
     * Gets the internal flows.
     *
     * @param virtualFile the virtual file
     * @return the internal flows
     */
    private void cacheInternalFlows(final VirtualFile virtualFile) {

        if (virtualFile.isDirectory()) {
            LOGGER.info("Loading internal flows from location: {}", virtualFile.getName());
            final URL dir;
            VirtualFile file = null;
            try {
                dir = virtualFile.asDirectoryURL();
                file = VFS.getChild(dir.toURI());
            } catch (final MalformedURLException | URISyntaxException e) {
                LOGGER.error("Error in accessing internal flow root directory. Exception: {}", e.getMessage());
                throw new FlowAutomationException("Error in accessing internal flow root directory. " + e.getMessage(), e);
            }

            if (file != null) {
                final List<VirtualFile> internalFlowsVirtualFile = file.getChildren();
                for (final VirtualFile internalFlowVirtualFile : internalFlowsVirtualFile) {
                    try {
                        readAndCacheInternalFlow(internalFlowVirtualFile);
                    } catch (final URISyntaxException | IOException e) {
                        LOGGER.error("Error to load internal flow: {}. Exception: {}", internalFlowVirtualFile.getName(), e.getMessage());
                        throw new FlowAutomationException("Error to load internal flow. " + e.getMessage(), e);
                    }
                }
            } else {
                LOGGER.warn("No Internal flows available.");
            }
        } else {
            LOGGER.error("Internal flows root directory missing.");
        }
    }

    /**
     * Read and cache the internal flow.
     *
     * @param internalFlowVirtualFile the internal flow virtual file
     * @return the internal flow
     * @throws URISyntaxException the URI syntax exception
     * @throws IOException        Signals that an I/O exception has occurred.
     */
    private void readAndCacheInternalFlow(final VirtualFile internalFlowVirtualFile) throws URISyntaxException, IOException {

        if (internalFlowVirtualFile.isDirectory()) {
            LOGGER.info("Loading internal flow: {}", internalFlowVirtualFile.getName());
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            final URL dir = internalFlowVirtualFile.asDirectoryURL();
            final VirtualFile file = VFS.getChild(dir.toURI());
            final List<VirtualFile> internalFlowContentVirtualFiles = file.getChildren();

            readVirtualFiles(internalFlowContentVirtualFiles, internalFlowVirtualFile.getPathName(), zos);
            zos.closeEntry();
            zos.close();

            cacheInternalFlow(baos, internalFlowVirtualFile.getName());
        }
    }

    /**
     * Cache virtual files.
     *
     * @param baos Byte Array Output Stream for the flow
     * @param name Flow package name
     */
    private void cacheInternalFlow(final ByteArrayOutputStream baos, final String name) {

        final byte[] byteArray = baos.toByteArray();
        unProcessedInternalFlows.put(name, byteArray);
        final FlowConfig flowConfig = FlowDefinitionParser.parseFlowDefinitionJson(byteArray);
        final String flowId = flowConfig.getFlowId();
        final String flowVersion = flowConfig.getFlowVersion();

        FlowAutomationInternalFlows.cacheInternalflowIdAndVersion(flowId, flowVersion);
    }

    /**
     * Read virtual files.
     *
     * @param internalFlowContentVirtualFiles the internal flow content virtual files
     * @param internalFlowLocation            the internal flow location
     * @param zos                             the zos
     * @throws IOException        Signals that an I/O exception has occurred.
     * @throws URISyntaxException the URI syntax exception
     */
    private void readVirtualFiles(final List<VirtualFile> internalFlowContentVirtualFiles, final String internalFlowLocation,
                                  final ZipOutputStream zos) throws IOException, URISyntaxException {
        for (final VirtualFile internalFlowContentVirtualFile : internalFlowContentVirtualFiles) {
            if (internalFlowContentVirtualFile.isFile()) {
                readFile(internalFlowContentVirtualFile, internalFlowLocation, zos);
            }

            if (internalFlowContentVirtualFile.isDirectory()) {
                final URL dir = internalFlowContentVirtualFile.asDirectoryURL();
                final VirtualFile file = VFS.getChild(dir.toURI());
                final List<VirtualFile> childVirtualFiles = file.getChildren();
                readVirtualFiles(childVirtualFiles, internalFlowLocation, zos);
            }
        }
    }

    /**
     * Read file.
     *
     * @param file                 the file
     * @param internalFlowLocation the internal flow location
     * @param zos                  the zos
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void readFile(final VirtualFile file, final String internalFlowLocation, final ZipOutputStream zos) throws IOException {
        final byte[] buffer = new byte[1024];
        final String fileName = file.getPathName().substring(internalFlowLocation.length() + 1, file.getPathName().length());
        final ZipEntry ze = new ZipEntry(fileName);
        zos.putNextEntry(ze);

        InputStream stream = null;
        try {
            stream = file.openStream();
            int len;
            while ((len = stream.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        } catch (final IOException e) {
            throw new IOException(String.format("Could not read file %s. Reason: %s.", file.getName(), e.getMessage()), e);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
}
