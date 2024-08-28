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

import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Utility class, to avoid instantiation.
     */
    private JsonParser() {
        throw new IllegalAccessError();
    }

    /**
     * Convert to type.
     *
     * @param <T>
     *            the generic type
     * @param jsonString
     *            the json string
     * @param type
     *            the type
     * @return the t
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static <T> T convertToType(final String jsonString, final Class<T> type) throws IOException {
        return mapper.readValue(jsonString, type);
    }


    /**
     * Convert to type t.
     *
     * @param <T>           the type parameter
     * @param jsonString    the json string
     * @param typeReference the type reference
     * @return the t
     * @throws IOException the io exception
     */
    public static <T> T convertToType(final String jsonString, final TypeReference typeReference) throws IOException {
        return (T) mapper.readValue(jsonString, typeReference);
    }
}
