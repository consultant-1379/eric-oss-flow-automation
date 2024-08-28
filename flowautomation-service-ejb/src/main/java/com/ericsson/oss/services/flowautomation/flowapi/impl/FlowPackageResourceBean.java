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

package com.ericsson.oss.services.flowautomation.flowapi.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.services.flowautomation.execution.resource.CDIFlowPackageResource;
import com.ericsson.oss.services.flowautomation.execution.resource.FlowPackageResourceLoader;


@Stateless
public class FlowPackageResourceBean implements CDIFlowPackageResource {

    @Inject
    private FlowPackageResourceLoader resourceLoader;

    @Override
    public List<String> list(final String businessKey, final String location) {
        return resourceLoader.list(businessKey, location);
    }

    @Override
    public String get(final String businessKey, final String resourcePath) {
        return resourceLoader.get(businessKey, resourcePath);
    }
}
