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
package com.ericsson.oss.services.flowautomation.rest.api.impl.internal;


import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.slf4j.Logger;

import com.ericsson.oss.services.flowautomation.rest.api.internal.InternalApi;
import com.ericsson.oss.services.flowautomation.rest.delegate.InternalApiWithRetriesDelegate;
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaResteasyEapServerCodegen", date = "2022-06-16T17:17:06.562274+01:00[Europe/Dublin]")
public class InternalApiServiceImpl implements InternalApi {
    @Inject
    private Logger logger;

    @Inject
    private InternalApiWithRetriesDelegate internalApiWithRetriesDelegate;

    @Override
    public Response postRestoreActions(SecurityContext securityContext) {
        internalApiWithRetriesDelegate.postRestoreActions();
        return Response.noContent().build();
    }

    @Override
    public Response preRestoreActions(SecurityContext securityContext) {
        internalApiWithRetriesDelegate.preRestoreActions();
        return Response.noContent().build();
    }

}
