/*******************************************************************************
 * COPYRIGHT Ericsson 2022
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

import static java.util.Arrays.stream;

public enum BackupRestoreErrorCode implements ErrorCode {

    ACTION_NOT_COMPLETED("backup-restore-4000", "Backup Restore action could not be completed.");

    /** The code. */
    private final String code;

    /** The reason. */
    private final String reason;

    BackupRestoreErrorCode(final String code, final String reason) {
        this.code = code;
        this.reason = reason;
    }

    /**
     * Converts string error code into {@code ErrorCode} instance.
     *
     * @param code the code
     * @return the {@code ErrorCode} instance if found or {@code UnknownErrorCode}
     */
    public static ErrorCode from(final String code) {
        return stream((ErrorCode[]) BackupRestoreErrorCode.values())
                .filter(errorCode -> errorCode.code().equals(code))
                .findFirst()
                .orElse(UnknownErrorCode.INSTANCE);
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String reason() {
        return reason;
    }
}