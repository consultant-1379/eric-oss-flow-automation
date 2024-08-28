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

package com.ericsson.oss.services.flowautomation.error;

import java.io.Serializable;

/**
 * The Interface ErrorCode used for capturing all the error codes that needs to be sent to the client/UI for i18n.
 */
public interface ErrorCode extends Serializable {

    /**
     * The Enum UnknownErrorCode for representing unknown error.
     */
    enum UnknownErrorCode implements ErrorCode {

        INSTANCE;

        @Override
        public String code() {
            return "unknown";
        }

        @Override
        public String reason() {
            return "Unknown Error. Please check server logs for details.";
        }
    }

    /**
     * Returns the error code.
     *
     * @return the string that represents an error code
     */
    String code();

    /**
     * Returns the reason for error.
     *
     * @return the description of the error
     */
    String reason();
}
