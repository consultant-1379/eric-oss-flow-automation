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

package com.ericsson.oss.services.flowautomation.bragent.agent;

import com.ericsson.adp.mgmt.bro.api.registration.SoftwareVersion;
import com.ericsson.oss.services.flowautomation.bragent.util.PropertiesHelper;

/**
 * Provides Software Version Information
 */
public class SoftwareVersionInformationUtils {

    private static final String SOFTWARE_VERSION_DESCRIPTION_PROPERTY = "fa.agent.softwareVersion.description";
    private static final String SOFTWARE_VERSION_PRODUCTION_DATE_PROPERTY = "fa.agent.softwareVersion.productionDate";
    private static final String SOFTWARE_VERSION_PRODUCT_NAME_PROPERTY = "fa.agent.softwareVersion.productName";
    private static final String SOFTWARE_VERSION_PRODUCT_NUMBER_PROPERTY = "fa.agent.softwareVersion.productNumber";
    private static final String SOFTWARE_VERSION_TYPE_PROPERTY = "fa.agent.softwareVersion.type";
    private static final String SOFTWARE_VERSION_REVISION_PROPERTY = "fa.agent.softwareVersion.revision";

    private static final String DEFAULT_SOFTWARE_VERSION_DESCRIPTION = "Sidecar Flow Automation brAgent, this agent will respond to BRO control plane life cycle and it will be responsible to trigger an internal endpoint, that will prepare the FA Service for the restore";
    private static final String DEFAULT_SOFTWARE_VERSION_PRODUCTION_DATE = "2022";
    private static final String DEFAULT_SOFTWARE_VERSION_TYPE = "type";
    private static final String DEFAULT_SOFTWARE_VERSION_PRODUCT_NAME = "Flow Automation brAgent";
    private static final String DEFAULT_SOFTWARE_VERSION_PRODUCT_NUMBER = "CXC 201 0001";
    private static final String DEFAULT_SOFTWARE_VERSION_REVISION = "Nope";

    /**
     * To hide the implicit public constructor
     */
    private SoftwareVersionInformationUtils() {}

    /**
     * Provides Software Version Information
     * @return softwareVersion information
     */
    public static SoftwareVersion getSoftwareVersion() {
        final SoftwareVersion softwareVersion = new SoftwareVersion();
        softwareVersion.setDescription(PropertiesHelper.getProperty(SOFTWARE_VERSION_DESCRIPTION_PROPERTY, DEFAULT_SOFTWARE_VERSION_DESCRIPTION));
        softwareVersion
        .setProductionDate(PropertiesHelper.getProperty(SOFTWARE_VERSION_PRODUCTION_DATE_PROPERTY, DEFAULT_SOFTWARE_VERSION_PRODUCTION_DATE));
        softwareVersion.setProductName(PropertiesHelper.getProperty(SOFTWARE_VERSION_PRODUCT_NAME_PROPERTY, DEFAULT_SOFTWARE_VERSION_PRODUCT_NAME));
        softwareVersion
        .setProductNumber(PropertiesHelper.getProperty(SOFTWARE_VERSION_PRODUCT_NUMBER_PROPERTY, DEFAULT_SOFTWARE_VERSION_PRODUCT_NUMBER));
        softwareVersion.setType(PropertiesHelper.getProperty(SOFTWARE_VERSION_TYPE_PROPERTY, DEFAULT_SOFTWARE_VERSION_TYPE));
        softwareVersion.setRevision(PropertiesHelper.getProperty(SOFTWARE_VERSION_REVISION_PROPERTY, DEFAULT_SOFTWARE_VERSION_REVISION));
        return softwareVersion;
    }
}
