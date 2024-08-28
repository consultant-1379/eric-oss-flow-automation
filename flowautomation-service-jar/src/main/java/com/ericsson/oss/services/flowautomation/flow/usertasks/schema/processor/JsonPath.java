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

package com.ericsson.oss.services.flowautomation.flow.usertasks.schema.processor;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class abstracts the JSON path of a schema property and helps to handle common Json path operations.
 */
public class JsonPath {

    private Path path;

    public JsonPath(final String rootKey) {
        this.path = Paths.get(rootKey);
    }

    private JsonPath(final Path path) {
        this.path = path;
    }

    /**
     * Given the following Json Path: #/property1/property2/property3, this method will return #/property1/property2
     *
     * @return The parent of the current json property
     */
    public String getParent() {
        if (path.getParent() == null) {
            return null;
        }
        return path.getParent().toString();
    }

    /**
     * Given the following Json Path: #/property1/property2/property3, this method will return property3
     *
     * @return the current property of the json path
     */
    public String getCurrent() {
        if (path.getFileName() == null) {
            return null;
        }
        return path.getFileName().toString();
    }

    /**
     * Given the following Json Path: #/property1/property2/property3,
     * calling this method like the following: resolve("property4"),
     * this method will return #/property1/property2/property3/property4
     *
     * @return A new Json path with the new property appended to the current path.
     */
    public JsonPath resolve(final String key) {
        return new JsonPath(path.resolve(key));
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
