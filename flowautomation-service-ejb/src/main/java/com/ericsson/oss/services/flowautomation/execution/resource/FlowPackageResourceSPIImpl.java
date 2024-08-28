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

package com.ericsson.oss.services.flowautomation.execution.resource;

import java.util.List;

import com.ericsson.oss.services.utils.JNDIUtil;

public class FlowPackageResourceSPIImpl implements FlowPackageResourceSPI {
    @Override
    public List<String> list(final String businessKey, final String location) {
        return getFlowPackageResourceBean().list(businessKey, location);
    }

    @Override
    public String get(final String businessKey, final String resourcePath) {
        return getFlowPackageResourceBean().get(businessKey, resourcePath);
    }

    /**
     * Using this JNDI lookup, it allows to switch the context from non CDI to CDI.
     *
     * @return the CDIFlowPackageResource instance
     */
    private CDIFlowPackageResource getFlowPackageResourceBean() {
        return (CDIFlowPackageResource) JNDIUtil.findLocalServiceImplementationForInterface(CDIFlowPackageResource.class.getName());
    }
}
