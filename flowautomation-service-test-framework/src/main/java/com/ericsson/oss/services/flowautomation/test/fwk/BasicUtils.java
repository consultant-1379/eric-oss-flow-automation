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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Basic utilities
 */
public class BasicUtils {
    private static final Logger logger = LoggerFactory.getLogger(BasicUtils.class);

    private BasicUtils() {
    }

    public static List<String> copyList(final List<String> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream().collect(Collectors.toList());
    }

    public static String niceJson(final String json) {
        final ObjectMapper mapper = new ObjectMapper();
        String niceJson = "";
        try {
            final Object jsonObject = mapper.readValue(json, Object.class);
            niceJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            logger.error("BasicUtils encountered exception: {}",e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return niceJson;
    }

}
