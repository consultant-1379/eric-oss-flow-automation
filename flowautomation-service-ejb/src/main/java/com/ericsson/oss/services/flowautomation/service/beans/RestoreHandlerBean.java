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

package com.ericsson.oss.services.flowautomation.service.beans;

import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.EJB_TYPE;
import static com.ericsson.oss.services.flowautomation.annotations.ServiceType.Value.JAR_TYPE;

import com.ericsson.oss.services.flowautomation.annotations.ServiceType;
import com.ericsson.oss.services.flowautomation.brapi.RestoreHandler;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
@ServiceType(EJB_TYPE)
public class RestoreHandlerBean implements RestoreHandler {

    @Inject
    @ServiceType(JAR_TYPE)
    private RestoreHandler restoreHandler;

    /**
     * Default constructor.
     */
    public RestoreHandlerBean() {
        //
    }

    @Override
    public void preRestoreActions() {
        restoreHandler.preRestoreActions();
    }

    @Override
    public void postRestoreActions() {
        restoreHandler.postRestoreActions();
    }
}
