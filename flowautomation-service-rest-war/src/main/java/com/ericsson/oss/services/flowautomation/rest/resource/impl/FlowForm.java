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

package com.ericsson.oss.services.flowautomation.rest.resource.impl;

import com.ericsson.oss.services.flowautomation.rest.validator.ValidPackage;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;

/**
 * The Class FlowForm.
 */
public class FlowForm {

    /** The flow package. */
    @ValidPackage
    private byte[] flowPackage;

    public FlowForm(byte[] flowPackage) {
        this.flowPackage = flowPackage.clone();
    }

    /**
     * Gets the flow package.
     *
     * @return the flow package
     */
    public byte[] getFlowPackage() {
        return flowPackage.clone();
    }

    /**
     * Sets the flow package.
     *
     * @param flowPackage
     *            the new flow package
     */
    @FormParam("flow-package")
    @PartType("application/octet-stream")
    public void setFlowPackage(final byte[] flowPackage) {
        this.flowPackage = flowPackage.clone();
    }
}
