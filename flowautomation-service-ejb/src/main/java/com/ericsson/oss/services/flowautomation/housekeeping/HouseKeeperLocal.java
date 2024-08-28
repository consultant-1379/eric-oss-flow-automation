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

import javax.ejb.Local;

/**
 * The Interface HouseKeepingLocal.
 */
@Local
public interface HouseKeeperLocal {

    /**
     * Perform house keeping.
     *
     * @param retentionPeriod         the retention period
     * @param retentionPeriodTimeUnit the retention period time unit
     */
    void performHouseKeeping(String retentionPeriod, String retentionPeriodTimeUnit);

    /**
     * Clean runtime stopped flow instances.
     *
     * @param retentionPeriod         the retention period
     * @param retentionPeriodTimeUnit the retention period time unit
     */
    void cleanRuntimeStoppedFlowInstances(String retentionPeriod, String retentionPeriodTimeUnit);

    /**
     * Clean historic stopped flow instances.
     *
     * @param retentionPeriod         the retention period
     * @param retentionPeriodTimeUnit the retention period time unit
     */
    void cleanHistoricStoppedFlowInstances(String retentionPeriod, String retentionPeriodTimeUnit);

    /**
     * Perform internal flows house keeping.
     */
    void performInternalFlowsHouseKeeping();
}
