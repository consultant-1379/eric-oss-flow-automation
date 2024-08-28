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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.utils.JNDIUtil;

/**
 * The Class JeeHouseKeeperInternal.
 */
public class JeeHouseKeeperInternal implements HouseKeeperInternal {

    private static final Logger LOGGER = LoggerFactory.getLogger(JeeHouseKeeperInternal.class);

    public JeeHouseKeeperInternal() {
        //
    }

    @Override
    public void performHouseKeeping(final String retentionPeriod, final String retentionPeriodTimeUnit) {
        LOGGER.debug("Looking up House Keeper...");
        final HouseKeeperLocal houseKeeperBean =
            (HouseKeeperLocal) JNDIUtil.findLocalServiceImplementationForInterface(HouseKeeperLocal.class.getName());
        houseKeeperBean.performHouseKeeping(retentionPeriod, retentionPeriodTimeUnit);
    }

    @Override
    public void cleanRuntimeStoppedFlowInstances(final String retentionPeriod, final String retentionPeriodTimeUnit) {
        LOGGER.debug("Looking up House Keeper...");
        final HouseKeeperLocal houseKeeperBean =
            (HouseKeeperLocal) JNDIUtil.findLocalServiceImplementationForInterface(HouseKeeperLocal.class.getName());
        houseKeeperBean.cleanRuntimeStoppedFlowInstances(retentionPeriod, retentionPeriodTimeUnit);
    }

    @Override
    public void cleanHistoricStoppedFlowInstances(final String retentionPeriod, final String retentionPeriodTimeUnit) {
        LOGGER.debug("Looking up House Keeper...");
        final HouseKeeperLocal houseKeeperBean =
            (HouseKeeperLocal) JNDIUtil.findLocalServiceImplementationForInterface(HouseKeeperLocal.class.getName());
        houseKeeperBean.cleanHistoricStoppedFlowInstances(retentionPeriod, retentionPeriodTimeUnit);
    }

    @Override
    public void performInternalFlowsHouseKeeping() {
        LOGGER.debug("Looking up House Keeper...");
        final HouseKeeperLocal houseKeeperBean =
            (HouseKeeperLocal) JNDIUtil.findLocalServiceImplementationForInterface(HouseKeeperLocal.class.getName());
        houseKeeperBean.performInternalFlowsHouseKeeping();
    }
}
