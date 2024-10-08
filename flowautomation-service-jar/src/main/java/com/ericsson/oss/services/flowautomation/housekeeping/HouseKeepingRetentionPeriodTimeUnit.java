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

package com.ericsson.oss.services.flowautomation.housekeeping;

/**
 * The Enum HouseKeepingRetentionPeriodTimeUnit.
 */
public enum HouseKeepingRetentionPeriodTimeUnit {
    SECOND("second"), DAY("day");

    private final String value;

    HouseKeepingRetentionPeriodTimeUnit(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
