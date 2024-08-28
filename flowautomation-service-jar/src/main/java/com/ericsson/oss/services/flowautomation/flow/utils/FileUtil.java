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

/**
 * The type File util.
 */
public final class FileUtil {

    private static final String ZIP_EXTENSION = ".zip";

    private FileUtil() {
        throw new IllegalAccessError();
    }

    /**
     * get extension of the file
     *
     * @param fileName the file name
     * @return String file extension
     */
    public static String getFileExtension(final String fileName) {
        final int lastIndexOf = fileName.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return fileName.substring(lastIndexOf);
    }


    /**
     * Returns true if the filename ends with .zip extension.
     *
     * @param filename the filename
     * @return the boolean
     */
    public static boolean isFileTypeZip(final String filename) {
        return ZIP_EXTENSION.equals(getFileExtension(filename));
    }
}
