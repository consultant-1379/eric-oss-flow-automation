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

import static com.ericsson.oss.services.flowautomation.error.ErrorMessageContants.FLOW_VERSION_SYNTAX_IS_INVALID;
import static com.ericsson.oss.services.flowautomation.error.FlowPackageErrorCode.FLOW_VERSION_SYNTAX_INVALID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.flowautomation.flow.exception.FlowServiceException;

/**
 * Utility class for Flow Automation Service.
 */
public final class FlowAutomationServiceUtil {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowAutomationServiceUtil.class);

    private static final Pattern FLOW_VERSION_PATTERN = Pattern.compile("^(\\d{1,3}+\\.\\d{1,3}\\.\\d{1,3})$");

    // NB: If this delimiter is changed it is necessary to update also FlowBusinessKey. This is due
    // to fact that the delimiter may contain special regexp chars.
    public static final String CAMUNDA_ID_DELIMITER = ".-.";

    public static final String VARIABLE_EXPRESSION_DELIMITER = ".";
    public static final String VARIABLE_EXPRESSION_DELIMITER_SPLIT = "\\.";
    public static final String REGEX_VALID_FILE_NAME = "([^*]+\\.[a-zA-Z0-9]*$)";

    private FlowAutomationServiceUtil() {
        //
    }

    /**
     * Checks if version syntax is valid.
     *
     * @param flowVersion
     *         the flow version
     * @return true, if version syntax is valid
     */
    public static boolean isVersionSyntaxValid(final String flowVersion) {
        final Matcher match = FLOW_VERSION_PATTERN.matcher(flowVersion);
        if (!match.find()) {
            LOGGER.error("Flow version syntax is Invalid");
            throw new FlowServiceException(FLOW_VERSION_SYNTAX_INVALID, FLOW_VERSION_SYNTAX_IS_INVALID);
        }
        return true;
    }

    public static String convertMilliSeconds(final Long duration) {
        if (duration != null) {
            final long seconds = ((duration / 1000) % 60);
            final long minutes = ((duration / (1000 * 60)) % 60);
            final long hours = ((duration / (1000 * 60 * 60)) % 24);

            final String hours1 = (hours < 10) ? "0" + hours : String.valueOf(hours);
            final String minutes1 = (minutes < 10) ? "0" + minutes : String.valueOf(minutes);
            final String seconds1 = (seconds < 10) ? "0" + seconds : String.valueOf(seconds);

            return hours1 + ":" + minutes1 + ":" + seconds1;

        }
        return null;
    }


    public static boolean isInvalidFileNameString(String fileName) {

        if (fileName == null || fileName.isEmpty() || fileName.length() > 255) {
            return true;
        }
        return !fileName.matches(REGEX_VALID_FILE_NAME);
    }
}
