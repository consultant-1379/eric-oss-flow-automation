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

package com.ericsson.oss.services.flowautomation.test.fwk.jse;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;


import com.ericsson.oss.services.utils.SimpleContextService;

public class TestContextServiceImpl implements SimpleContextService {

    @Override
    public void setContextValue(final String contextParameterName, final Serializable contextData) {
        //
    }

    @Override
    public String getContextValue(final String contextParameterName) {
        if (contextParameterName.equals("UserID")) {
            return "#Ericsson";
        }
        return null;
    }

    @Override
    public Map<String, Serializable> getContextData() {
        return (Map<String, Serializable>) Collections.emptySet();
    }
}
