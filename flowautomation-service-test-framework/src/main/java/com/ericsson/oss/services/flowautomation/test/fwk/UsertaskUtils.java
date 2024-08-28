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

package com.ericsson.oss.services.flowautomation.test.fwk;

import static com.ericsson.oss.services.flowautomation.test.fwk.BasicUtils.copyList;

import java.util.Collections;
import java.util.List;

/**
 * Usertask utils
 */
public class UsertaskUtils {
    
    private UsertaskUtils() {
        //
    }
    
    public static UsertaskInput findInput(final List<UsertaskInput> inputs, final List<String> basePath, final String optionName) {
        UsertaskInput input = null;

        final List<String> path = copyList(basePath);
        path.add(optionName);
        for (final UsertaskInput inputx : inputs) {
        	if (Collections.indexOfSubList(inputx.getPathAsList(), path) == 0) {
                input = inputx;
                break;
            }
        }

        return input;
    }

}
